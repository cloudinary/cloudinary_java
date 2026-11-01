#!/usr/bin/env bash
new_version=$1

current_version=`sed -n 's/.*VERSION = "\([0-9.]*\)".*/\1/p' cloudinary-core/src/main/java/com/cloudinary/Cloudinary.java`
if [ -z "$current_version" ]; then
    echo "Could not read VERSION from cloudinary-core/src/main/java/com/cloudinary/Cloudinary.java"
    exit 1
fi
current_version_re=${current_version//./\\.}
echo "Current version is $current_version"
if [ -n "$new_version" ]; then
    echo "New version will be $new_version"
    echo "Pattern used: $current_version_re"
    for file in cloudinary-core/src/main/java/com/cloudinary/Cloudinary.java README.md gradle.properties; do
        sed -i.bak -e "s/${current_version_re}/${new_version}/g" "$file" && rm "$file.bak"
    done
    git changelog -t $new_version
else
    echo "Usage: $0 <new version>"
    echo "For example: $0 1.9.2"
fi
