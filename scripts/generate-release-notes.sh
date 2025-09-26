#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1" >&2
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1" >&2
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

# Function to get the current version from version.properties
get_current_version() {
    if [ ! -f "version.properties" ]; then
        print_error "version.properties file not found in current directory"
        exit 1
    fi

    source version.properties
    echo "${major}.${minor}.${patch}"
}

# Function to get the latest release tag
get_latest_release_tag() {
    git fetch --tags --quiet 2>/dev/null || true
    git tag --sort=-version:refname | head -1 2>/dev/null || echo ""
}

# Function to get the previous release tag (excluding current if it exists)
get_previous_release_tag() {
    local current_tag="$1"
    git fetch --tags --quiet 2>/dev/null || true

    if [ -n "$current_tag" ]; then
        git tag --sort=-version:refname | grep -v "^${current_tag}$" | head -1 2>/dev/null || echo ""
    else
        git tag --sort=-version:refname | head -1 2>/dev/null || echo ""
    fi
}

# Function to check if current commit has a version tag
get_current_commit_tag() {
    git tag --points-at HEAD 2>/dev/null | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | head -1 || echo ""
}

# Function to categorize and sort commits by conventional commit prefixes
categorize_commits() {
    local commits="$1"

    # Initialize arrays for each category
    local feat_commits=()
    local fix_commits=()
    local other_commits=()
    local no_prefix_commits=()

    # Read commits line by line and categorize them
    while IFS= read -r line; do
        if [[ -z "$line" ]]; then
            continue
        fi

        if [[ "$line" =~ ^-[[:space:]]*feat ]]; then
            feat_commits+=("$line")
        elif [[ "$line" =~ ^-[[:space:]]*fix ]]; then
            fix_commits+=("$line")
        elif [[ "$line" =~ ^-[[:space:]]*[a-zA-Z]+: ]]; then
            # Has a prefix but not feat/fix (includes refactor and others)
            other_commits+=("$line")
        else
            # No conventional commit prefix
            no_prefix_commits+=("$line")
        fi
    done <<< "$commits"

    # Sort other_commits by prefix
    if [ ${#other_commits[@]} -gt 0 ]; then
        IFS=$'\n' other_commits=($(printf '%s\n' "${other_commits[@]}" | sort))
    fi

    # Build output
    local output=""

    # Features
    if [ ${#feat_commits[@]} -gt 0 ]; then
        if [ "$FORMAT" = "markdown" ]; then
            output+="### ✨ User facing changes"$'\n'$'\n'
        fi
        for commit in "${feat_commits[@]}"; do
            output+="$commit"$'\n'
        done
        output+=$'\n'
    fi

    # Fixes
    if [ ${#fix_commits[@]} -gt 0 ]; then
        if [ "$FORMAT" = "markdown" ]; then
            output+="### 🐛 Fixes"$'\n'$'\n'
        fi
        for commit in "${fix_commits[@]}"; do
            output+="$commit"$'\n'
        done
        output+=$'\n'
    fi

    # Other changes (with prefixes but not feat/fix + no prefix)
    local all_other=("${other_commits[@]}" "${no_prefix_commits[@]}")
    if [ ${#all_other[@]} -gt 0 ]; then
        if [ "$FORMAT" = "markdown" ]; then
            output+="### 📦 Other"$'\n'$'\n'
        fi
        for commit in "${all_other[@]}"; do
            output+="$commit"$'\n'
        done
    fi

    # Remove trailing newline
    echo -n "${output%$'\n'}"
}

# Function to generate release notes between two references
generate_release_notes() {
    local from_ref="$1"
    local to_ref="$2"
    local range_description="$3"

    print_info "Generating release notes $range_description"

    local raw_commits
    if [ -n "$from_ref" ]; then
        print_info "Range: $from_ref..$to_ref"
        raw_commits=$(git log --pretty=format:"- %s (%h)" "${from_ref}..${to_ref}" 2>/dev/null | grep -v "chore: prepare next development iteration")
    else
        print_info "No previous release found, showing last 10 commits"
        raw_commits=$(git log --pretty=format:"- %s (%h)" --max-count=10 "$to_ref" 2>/dev/null | grep -v "chore: prepare next development iteration")
    fi

    categorize_commits "$raw_commits"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Generate release notes for SeviBus Android project.

This script handles two scenarios:
1. Release commit: When run on a commit tagged with a release version,
   generates notes since the previous release tag.
2. Development: When run on any other commit, generates notes since
   the last release tag to current HEAD.

OPTIONS:
    -h, --help          Show this help message
    -v, --verbose       Enable verbose output
    --from TAG          Override the start tag/commit for release notes
    --to TAG            Override the end tag/commit for release notes (default: HEAD)
    --format FORMAT     Output format: 'markdown' (default) or 'plain'

EXAMPLES:
    # Generate notes for current development state
    $0

    # Generate notes between specific tags
    $0 --from v5.4.2 --to v5.4.3

    # Generate notes with custom range
    $0 --from v5.4.2 --to HEAD

    # Generate plain text output (no markdown formatting)
    $0 --format plain

EOF
}

# Parse command line arguments
VERBOSE=false
FROM_TAG=""
TO_TAG="HEAD"
FORMAT="markdown"

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_usage
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        --from)
            FROM_TAG="$2"
            shift 2
            ;;
        --to)
            TO_TAG="$2"
            shift 2
            ;;
        --format)
            FORMAT="$2"
            if [ "$FORMAT" != "markdown" ] && [ "$FORMAT" != "plain" ]; then
                print_error "Invalid format: $FORMAT. Must be 'markdown' or 'plain'"
                exit 1
            fi
            shift 2
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Main logic
main() {
    # Check if we're in a git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not in a git repository"
        exit 1
    fi

    # Get current information
    current_version=$(get_current_version)
    current_commit_tag=$(get_current_commit_tag)

    if [ "$VERBOSE" = true ]; then
        print_info "Current version from version.properties: $current_version"
        print_info "Current commit tag: ${current_commit_tag:-'(none)'}"
    fi

    # Determine the range for release notes
    if [ -n "$FROM_TAG" ] && [ -n "$TO_TAG" ]; then
        # Manual override
        from_ref="$FROM_TAG"
        to_ref="$TO_TAG"
        range_desc="from $FROM_TAG to $TO_TAG"
    elif [ -n "$current_commit_tag" ]; then
        # We're on a release commit
        from_ref=$(get_previous_release_tag "$current_commit_tag")
        to_ref="$current_commit_tag"
        range_desc="for release $current_commit_tag"
        if [ "$VERBOSE" = true ]; then
            print_info "Detected release commit with tag: $current_commit_tag"
        fi
    else
        # We're on a development commit
        from_ref=$(get_latest_release_tag)
        to_ref="HEAD"
        range_desc="since last release"
        if [ "$VERBOSE" = true ]; then
            print_info "Generating development release notes"
        fi
    fi

    if [ "$VERBOSE" = true ]; then
        print_info "Previous release tag: ${from_ref:-'(none)'}"
        print_info "Target reference: $to_ref"
    fi

    # Generate the release notes
    commits=$(generate_release_notes "$from_ref" "$to_ref" "$range_desc")

    if [ -z "$commits" ]; then
        print_warning "No commits found for the specified range"
        exit 0
    fi

    # Output in the requested format
    if [ "$FORMAT" = "markdown" ]; then
        echo "## Changes in this release:"
        echo ""
        echo "$commits"
    else
        echo "$commits"
    fi
}

# Run main function
main "$@"