#!/bin/bash
set -e

if [ ! -f "version.properties" ]; then
    echo "Error: version.properties file not found"
    exit 1
fi

source version.properties

if [ "$snapshot" != "false" ]; then
    echo "Error: Not a release version (current: ${major}.${minor}.${patch})"
    exit 1
fi

# Always bump patch and add snapshot for next development version
next_patch=$((patch + 1))

# Write next development version
cat > version.properties << EOF
major=$major
minor=$minor
patch=$next_patch
snapshot=true
EOF

echo "Prepared next development version: ${major}.${minor}.${next_patch}-snapshot"