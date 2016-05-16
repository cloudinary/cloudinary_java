#!/usr/bin/env bash

# Create the unsigned upload preset required for tests
# Currently only required for the Android test since Android API cannot create the preset

UNSIGNED_PRESET="cloudinary_java_test"
SDK_TEST_TAG="cloudinary_java_test"

if [ -z ${CLOUDINARY_URL+x} ]
    then echo "The variable CLOUDINARY_URL must be set!"
else

    API_CRED=${CLOUDINARY_URL%@*}
    API_CRED=${API_CRED#*//}
    if curl -s "https://${API_CRED#*//}@api.cloudinary.com/v1_1/${CLOUDINARY_URL#*@}/upload_presets/${UNSIGNED_PRESET}" | \
       grep --quiet "Can't find upload preset named"
       then curl --data "name=${UNSIGNED_PRESET}&unsigned=true&tags=${TAG}" \
           "https://${API_CRED#*//}@api.cloudinary.com/v1_1/${CLOUDINARY_URL#*@}/upload_presets"
           echo
       else
           echo "Preset already exists"
    fi
fi