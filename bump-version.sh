#!/bin/bash

# Paths to versions
VERSION_FILE="algotrade4j-api/src/main/resources/version.json"
ROOT_POM_FILE="pom.xml"

increment_version() {
    local version=$1
    local part=$2
    IFS='.' read -ra VERSION_PARTS <<< "$version"

    case $part in
        major)
            VERSION_PARTS[0]=$((VERSION_PARTS[0] + 1))
            VERSION_PARTS[1]=0
            VERSION_PARTS[2]=0
            ;;
        minor)
            VERSION_PARTS[1]=$((VERSION_PARTS[1] + 1))
            VERSION_PARTS[2]=0
            ;;
        patch)
            VERSION_PARTS[2]=$((VERSION_PARTS[2] + 1))
            ;;
        *)
            echo "Invalid version part specified. Use 'major', 'minor', or 'patch'."
            exit 1
            ;;
    esac

    echo "${VERSION_PARTS[0]}.${VERSION_PARTS[1]}.${VERSION_PARTS[2]}"
}

# Check if version part is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <major|minor|patch>"
    exit 1
fi

VERSION_PART=$1

# Get the current commit hash before making any changes
CURRENT_COMMIT=$(git rev-parse --short HEAD)

# Read current version from version.json
current_version=$(grep -o '"version": "[^"]*"' $VERSION_FILE | cut -d'"' -f4)

# Increment version
new_version=$(increment_version $current_version $VERSION_PART)

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
git commit -m "[bump-version.sh] Bump $VERSION_PART version to $new_version"

echo "Updated $VERSION_PART version to $new_version and set commit to $CURRENT_COMMIT"

# Verify the last few commits
echo "Recent commits:"
git log -n 5 --oneline