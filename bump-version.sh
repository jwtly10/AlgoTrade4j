#!/bin/bash

# Path to root POM file
ROOT_POM_FILE="pom.xml"

increment_version() {
    local version=$1
    local part=$2
    IFS='.' read -ra VERSION_PARTS <<< "$version"

    case $part in
        major)
            VERSION_PARTS[0]=$((VERSION_PARTS[0] + 1))
            VERSION_PARTS[1]=0
            ;;
        minor)
            VERSION_PARTS[1]=$((VERSION_PARTS[1] + 1))
            ;;
        *)
            echo "Invalid version part specified. Use 'major' or 'minor'."
            exit 1
            ;;
    esac

    echo "${VERSION_PARTS[0]}.${VERSION_PARTS[1]}"
}

# Check if version part is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <major|minor>"
    exit 1
fi

VERSION_PART=$1

# Get the current commit hash before making any changes
CURRENT_COMMIT=$(git rev-parse --short HEAD)

# Read current version from pom.xml
current_version=$(grep -m 1 "<revision>" $ROOT_POM_FILE | sed -n 's/.*<revision>\(.*\)<\/revision>.*/\1/p')

# Increment version
new_version=$(increment_version $current_version $VERSION_PART)

# Update root pom.xml using the ${revision} property
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/<revision>.*<\/revision>/<revision>$new_version<\/revision>/" $ROOT_POM_FILE
else
    # Linux
    sed -i "s/<revision>.*<\/revision>/<revision>$new_version<\/revision>/" $ROOT_POM_FILE
fi

# Commit the version changes
git add $ROOT_POM_FILE
git commit -m "[bump-version.sh] Bump $VERSION_PART version to $new_version"

echo "Updated $VERSION_PART version to $new_version"
echo "Current commit: $CURRENT_COMMIT"

# Verify the last few commits
echo "Recent commits:"
git log -n 5 --oneline