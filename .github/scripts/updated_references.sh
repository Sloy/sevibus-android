#!/usr/bin/env bash
# Prints markdown with the before/after images of the screenshot references changed between two commits.
# Usage: updated_references.sh <base-sha> <head-sha>
set -euo pipefail

base="$1"
head="$2"
raw="https://raw.githubusercontent.com/$GITHUB_REPOSITORY"

echo "| Screenshot | Before | After |"
echo "|---|---|---|"
git diff --no-renames --name-status "$base" "$head" -- app/src/screenshotTestDebug/reference debug-menu/src/screenshotTestDebug/reference | while read -r status file; do
  name="${file%%/*}/$(basename "$(dirname "$file")")/$(basename "$file" .png)"
  case "$status" in
    A) before="New" ;;
    *) before="<img src=\"$raw/$base/$file\" width=\"250\">" ;;
  esac
  case "$status" in
    D) after="Deleted" ;;
    *) after="<img src=\"$raw/$head/$file\" width=\"250\">" ;;
  esac
  echo "| \`$name\` | $before | $after |"
done
