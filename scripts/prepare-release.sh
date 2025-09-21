#!/bin/bash
set -e

RELEASE_TYPE=${1:-patch}

# Read current version
if [ ! -f "version.properties" ]; then
    echo "Error: version.properties file not found"
    exit 1
fi

source version.properties

if [ "$snapshot" != "true" ]; then
    echo "Error: Not a snapshot version (current: ${major}.${minor}.${patch})"
    exit 1
fi

# Calculate release version based on type
case $RELEASE_TYPE in
    "patch")
        # Keep current version, just remove snapshot
        release_major=$major
        release_minor=$minor
        release_patch=$patch
        ;;
    "minor")
        # Bump minor, reset patch, remove snapshot
        release_major=$major
        release_minor=$((minor + 1))
        release_patch=0
        ;;
    "major")
        # Bump major, reset minor and patch, remove snapshot
        release_major=$((major + 1))
        release_minor=0
        release_patch=0
        ;;
    *)
        echo "Error: Invalid release type: $RELEASE_TYPE. Must be one of: patch, minor, major"
        exit 1
        ;;
esac

# Write release version
cat > version.properties << EOF
major=$release_major
minor=$release_minor
patch=$release_patch
snapshot=false
EOF

echo "Prepared release version: ${release_major}.${release_minor}.${release_patch} (type: $RELEASE_TYPE)"