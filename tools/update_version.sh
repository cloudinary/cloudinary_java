#!/usr/bin/env bash
new_version=$1

current_version=`grep -oP "(?<=VERSION \= \")([0-9.]+)(?=\")" cloudinary-core/src/main/java/com/cloudinary/Cloudinary.java`
current_version_re=${current_version//./\\.}
echo "Current version is $current_version"
if [ -n "$new_version" ]; then
    echo "New version will be $new_version"
    echo "Pattern used: $current_version_re"
    sed -e "s/${current_version_re}/${new_version}/g" -i "" cloudinary-core/src/main/java/com/cloudinary/Cloudinary.java
    sed -e "s/${current_version_re}/${new_version}/g" -i "" README.md
    sed -e "s/${current_version_re}/${new_version}/g" -i "" gradle.properties
    git changelog -t $new_version
else
    echo "Usage: $0 <new version>"
    echo "For example: $0 1.9.2"
fi
