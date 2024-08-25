#!/bin/bash

# Paths to versions
VERSION_FILE="algotrade4j-api/src/main/resources/version.json"
ROOT_POM_FILE="pom.xml"

increment_minor_version() {
    local version=$1
    local major=$(echo $version | cut -d. -f1)
    local minor=$(echo $version | cut -d. -f2)
    local patch=$(echo $version | cut -d. -f3)

    minor=$((minor + 1))
    patch=0  # Reset patch to 0 when incrementing minor

    echo "$major.$minor.$patch"
}

# Get the current commit hash before making any changes
CURRENT_COMMIT=$(git rev-parse --short HEAD)

# Read current version from version.json
current_version=$(grep -o '"version": "[^"]*"' $VERSION_FILE | cut -d'"' -f4)

# Increment version
new_version=$(increment_minor_version $current_version)

# Update version.json
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/\"version\": \"$current_version\"/\"version\": \"$new_version\"/" $VERSION_FILE
    sed -i '' "s/\"commit\": \"[^\"]*\"/\"commit\": \"$CURRENT_COMMIT\"/" $VERSION_FILE
else
    # Linux
    sed -i "s/\"version\": \"$current_version\"/\"version\": \"$new_version\"/" $VERSION_FILE
    sed -i "s/\"commit\": \"[^\"]*\"/\"commit\": \"$CURRENT_COMMIT\"/" $VERSION_FILE
fi

# Update root pom.xml using the ${revision} property
sed -i.bak "s/<revision>.*<\/revision>/<revision>$new_version<\/revision>/" $ROOT_POM_FILE && rm $ROOT_POM_FILE.bak

# Commit the version changes
git add $VERSION_FILE $ROOT_POM_FILE
git commit -m "[bump-version.sh] Bump version to $new_version"

echo "Updated version to $new_version and set commit to $CURRENT_COMMIT"

# Verify the last few commits
echo "Recent commits:"
git log -n 5 --oneline