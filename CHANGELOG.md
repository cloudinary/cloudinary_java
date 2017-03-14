
1.9.1 / 2017-03-14
==================

  * Add expires at to generate archive (#68)
  * Add `skip_transformation_name` parameter to generate archive. (#67)
  * Fix variables.
    * Fix variable regex.
    * Make Expression.serialize return normalized expression
    * Fix variable sorting.
    * Add tests for variable order.
  * Avoid normalizing negative numbers.
  * Normalize effect parameter
  * Remove duplicate quality parameter line.

1.9.0 / 2017-03-08
==================

New functionality
-----------------

  * Support **User defined variables** and **expressions**.
  * Add `async` parameter to upload params(#63)
  * Add `expired_at` parameter to private download. (#60)
  * Add `moderation` parameter in explicit call (#59)

Other changes
-------------

  * Fix double encoding for commas and slashes in text layers (#66)
  * Add artistic filter test (#65)
  * Add gravity-auto test (#64)
  * Fix `OutOfMemoryError` when uploading large files in android. Fixes #55 (#57)
  * Fix encoding error in api update resource (#61)

1.8.1 / 2017-02-22
==================

  * Add support for URL authorization token.
  * Refactor AuthToken.
  * Refactor tests for stability
  * Support nested objects in CLOUDINARY_URL. e.g. foo[bar]=100.
  * Add maven items to `.gitignore`.

1.8.0 / 2017-02-08
==================

New functionality
-----------------

  * Access mode API

Other changes
-------------

  * Fix listing direction test.
  * Refactor `multi` test

1.7.0 / 2017-01-30
==================

New functionality
-----------------

  * Add Akamai token generator

Other changes
-------------

  * Fix "multi" test

1.6.0 / 2017-01-08
==================

  * Add Search by context API

1.5.0 / 2016-11-19
==================

New functionality
-----------------

  * Add context API
  * Escape `\` and `=` in context
  * Add `removeAllTags` API

1.4.6 / 2016-10-27
====================================

  * Add streaming profiles API

1.4.5 / 2016-09-16
====================================
  * Better handling of missing/unreadable local files

1.4.4 / 2016-09-15
====================================
  * Fix issue when uploading URL with \n

1.4.3 / 2016-09-09
====================================

New functionality
-----------------

  * New Admin API `Publish`.
  * Support `to_type` in `rename`.
  * Add `skip_transformation_name` and `expires_at` to archive parameters.
  * Support Client Hints.

Other changes
-------------

  * Add deprecation message to `Layer` classes.
  * Define `MockableTest`
  * Add static import of `asMap` and `emptyMap`. Suppress deprecation warnings for backward compatibility tests.
  * Refactor Quality and Width tests.
  * Update Junit version and add JUnitParams.
  * Add Hamcrest tests.
  * Add tests for auto width and original width and height ( `ow`, `oh`) values
  * Add `timeout`, `connect_timeout` and `connection_request_timeout` to HTTP43 and HTTP44 Api.

1.4.2 / 2016-05-16
====================================

  * Sent params as entities for PUT, POST
  * Use "_method" with "delete" instead of HttpDelete.
  * Add `next_cursor` to `Api#transformation()`
  * Update Google App Engine demo
  * Add script to create unsigned upload preset for the Android test
  * Use dynamic tag in sprites test
  * Add SDK_TEST_TAG to all resources being created.
  * Remove API limits test

1.4.1 / 2016-03-23
====================================
  * Rename conditional parameters `faces` and `pages` to `faceCount` and `pageCount`


1.4.0 / 2016-03-19
====================================
  * Add Condition builder for faces
  * Modify explicit test - don't use twitter
  * Modify categorization test result value
  * Add Conditional Transformations
  * Cleanup Whitespace 
  * Use variables for public_id's in rename tests
  * Fix uploadLarge to use X-Unique-Upload-Id instead of updating params. Solves #18
  * Fix support for non-ascii chars in upload URL

1.3.0 / 2016-01-19
====================================

  * Add `responsive_breakpoints` paramater
  * Use `TextLayer` instead of `TextLayerBuilder`. Use `getThis()` instead of `self()`.
  * Use constant and meaningful name for upload preset. Rearrange imports.
  * Update SDK versions in Android projects.
  * Support cloudinary credentials URL that has an API_KEY but no API_SECRET
  * Remove redundant `deleteConflictingFiles`.
  * Merge branch 'master' of github.com:cloudinary/cloudinary_java
  * support createArchive
  * line spacng support in text overlay
  * Create separate test class for Layer
  * Rename Layer classes. Rename self() to getThis() to match the pattern.
  * Merge branch 'master' of github.com:cloudinary/cloudinary_java
  * change user agent - remove spaces. stricter layer parameter check. fix underlay method signature
  * Merge branch 'master' of github.com:cloudinary/cloudinary_java
  * Fix Android complex filename test

cloudinary-parent-1.2.2 / 2016-01-19
====================================

  * Fix Android tests
  * Enable apache http 4.3 strategy
  * Support easy overlay/underlay construction
  * Support upload mappings api. add missing restore test
  * Support the restore api
  * Normalize user agent
  * Add invalidate flag to rename and explicit
  * Support aspect ratio transformation param
  * Add filename and complex filename test
  * Fix encoding issues when JVM default encoding is not UTF-8
  * Revent timeout exception change
  * Support filename in upload options. close response objects in http44
  * Update README. Fixes #28
  * Merge pull request #26 from wagaun/master
  * Fixing typo on exception
  * Update README.md

cloudinary-parent-1.2.1 / 2015-06-18
====================================

  * Disable java8 doclint
  * Fix references to 1.1.4-SNAPSHOT. Fix wrong URLs in README.md
  * Fix documentation and imports
  * Modify exception message to say that Admin API is not supported.
  * Fix HTML escaping (fixes upload tags)
  * Allow android unsigned upload without api_key
  * Fix http44 response closing.

cloudinary-parent-1.2.2 / 2015-10-11
====================================

  * Support apache http 4.3 strategy
  * Support easy overlay/underlay construction
  * Support upload mappings api
  * Support the restore api
  * Normalize user agent
  * Add invalidate flag to rename and explicit
  * Support aspect ratio transformation param
  * Fix encoding issues when JVM default encoding is not UTF-8
  * Support filename in upload options
  * Close response objects in http44.

cloudinary-parent-1.2.0 / 2015-04-13
====================================

  * Support httpcomponents 4.4
  * Support for video tag and transformations
  * Add video transformation parameters and zoom transformation
  * Support ftp url upload
  * Support eager_async in explicit
  * Fix UTF-8 issues in API
  * Add support for video tag. refactor Url based tags
  * Scrub UrlBuilderStrategy
  * Enable crippled core mode without loading strategies
  * Move core test to core
  * Use URLEncoder instead of AbstractUrlBuilderStrategy. 
  * Use upload_chuncked endpoint for upload large
  * Improved parameter support for upload_large.
  * support byte[] file input for upload

cloudinary-parent-1.1.3 / 2015-02-24
====================================

  * Fix test after file name change
  * Added timeout parameter to admin api and Fixed test and configuration issues

cloudinary-parent-1.1.2 / 2015-01-15
====================================

  * Fix support for string eager parameters e.g. for safe mobile flow
  * Merge pull request #17 from cloudinary/eager_upload_params
  * merged android signature fix
  * eager upload params can be both string or List<Transformation>

cloudinary-parent-1.1.1 / 2014-12-22
====================================

  * Support secure domain sharding
  * Don't sign version component
  * Support url suffix and use root path
    * renamed urlSuffix to suffix
  * Support tags in upload large.
  * Change log and version update
  * added new options to url tag
  * added invalidate to bulk deletes
  * Add missing tests in adnroid-test. fix signing tests in android-test. be more specific with exception class in http42 Cloudinary tests.
  * updated Url.generate method (b4 tests)
  * bug fixes

cloudinary-parent-1.1.0 / 2014-11-18
====================================

  * Merge branch 'globalize' of github.com:codeinvain/cloudinary_java
  * Remove redundant depndencies
  * - changed org.json to org.cloudinary.json due to Android optimization issues . - removed dependency on SimpleJSON from tablib
  * Update CHANGES.txt
  * Merge branch 'globalize'
  * Fix documentation. Fix dependencies
  * Fix modules artifactId
  * promoted minor version (1.0.x -> 1.1.x) & fixed documentation external links
  * added deprecated asMap method to Cloudinary (support old api)
  * updated documentation , fixed sample projects
  * promoted minor version (1.0.x -> 1.1.x) & fixed documentation external links
  * added deprecated asMap method to Cloudinary (support old api)
  * updated documentation , fixed sample projects
  * add support for signed urls in tag helpers (image and url)
  * Git ignore cloudinary-android-test/src/main/AndroidManifest.xml. Fix tag lib dependency
  * Remove httpclient dependencies from cloudinary-core. Use main version in both http42 and android versions. Remove getRawResponse from ApiResponse
  * Merge branch 'globalize' of github.com:codeinvain/cloudinary_java
  * merged config & builder
  * Update README.md
  * cloudinary credentials removed
  * http42 + android tests pass
  * changed architecture to core + strategies
  * removed shared classes
  * android jar
  * maven build , project dependency core -> http42 -> taglib
  * unified Java API and created basic implementation
  * custom StringUtils
  * support folder listing API

cloudinary-parent-1.0.14 / 2014-07-29
=====================================

  * Add background_removal
  * Support return_delete_token in upload/update params
  * Support responsive and hidpi
  * Support custom coordinates.

cloudinary-parent-1.0.13 / 2014-04-29
=====================================

  * Add support for opacity
  * Support upload_presets
  * Support unsigned uploads
  * Support start_at for resource listing
  * Support phash for upload and resource details
  * Support rate limit header in Api calls
  * Initial commit Google App engine sample
  * Merge remote master
  * Allow passing ClientConnectionManager

cloudinary-parent-1.0.12 / 2014-03-04
=====================================

  * Increment version to 1.0.12
  * Fix uploader API calls handling of non-string parameters e.g. Booleans

cloudinary-parent-1.0.11 / 2014-03-04
=====================================

  * Document releases in CHANGES.txt
  * Fix test - raw upload parts must be > 5m
  * better large raw upload support
  * Merge branch 'master' of https://github.com/cloudinary/cloudinary_java
  * new update method
  * Add listing by moderation kind and status
  * Add moderation status in listing
  * Add moderation flag in upload
  * Add moderation_status in update
  * Add ocr, raw_conversion, categorization, detection, similarity_search and auto_tagging parameters in update and upload
  * Add support for uploading large raw files

cloudinary-parent-1.0.10 / 2014-01-27
=====================================

  * add discard_original_filename upload flag. Formatting in tests
  * support setting context in explicit
  * Add direction support to resource listing.

cloudinary-parent-1.0.9 / 2014-01-10
====================================

  * remove delete_all from tests. fix face coordinates in explicit
  * Merge branch 'master' of https://github.com/cloudinary/cloudinary_java
  * add user agent. fix api test
  * refactor Map encoding for upload
  * Merge branch 'signedurl'
  * Update README.md
  * Merge branch 'master' of https://github.com/cloudinary/cloudinary_java
  * support multiple face coordinates in upload and explicit. optionaly use Coordinates as a wrapper of multiple rectangles
  * add support for overwrite in taglib
  * add support for overwrite boolean in upload
  * support signed urls
  * delete all + cursors, tag and context flags in lists, list by public ids, add support in upload for: face_coordinates, alowed_formats, context
  * change dependency to published 1.0.8 and change installation instructions accordingly

cloudinary-parent-1.0.8 / 2013-12-20
====================================

  * Fix implementation of SmartUrlEncoder in case of non-ascii characters
  * fix callback when servlet is not at root
  * better handling of raw files
  * add subsections in README. Add this to memeber assignments
  * move most of stored file logic to core. support stored file in url and url and image tags. add a readme to the sample project
  * add support for named transformations as tag attribute
  * add support for local secure (and implicit from request)  and cdn_subdomain
  * cleanup and upload parameters completeness
  * change images to use inline transformations when possible. fix image link in list
  * fix inline transformation in image. add inline trnaformation in url
  * initial commit of photo album sample. added additional or modified existing tag helpers to taglib to enable more robust transformations and to allow cloufdinary URLs outside of images and to allow specifying images from facebook/twitter and support jQuery direct upload.

cloudinary-parent-1.0.7 / 2013-11-02
====================================

  * Support the color parameter
  * Merge branch 'master' of https://github.com/cloudinary/cloudinary_java
  * add support for unique_filename and added a test for use_filename
  * Merge pull request #9 from AssuredLabor/transformationAttr
  * add transformation attribute to cloudinary upload tag
  * Fix handling of boolean parameters on upload

cloudinary-parent-1.0.6 / 2013-08-07
====================================

  * Rename prepareUploadTagParams to uploadTagParams
  * Escape all public_ids including non-http ones.
  * Merge pull request #7 from AssuredLabor/extractUploadParams
  * Updated so we don't escapeHTML unless necessary for the server side. This allows the client-side to receive a JS hash / object directly. This is useful, depending on how the input is rendered.
  * Extracted upload tagParams and upload url functionality into their functions, this will facilitate frameworks like Angular fetching the server-side params

cloudinary-parent-1.0.5 / 2013-07-31
====================================

  * Support folder and proxy upload parameters
  * Fix string comparison of secureDistribution
  * Change secure urls to use *res.cloudinary.com
  * Support Admin API ping
  * Support generateSpriteCss

cloudinary-parent-1.0.4 / 2013-07-15
====================================

  * Issue #6 - add instructions on using as a maven dependency
  * Support raw data URI
  * Support zipDownload. Cleanup signing code
  * Support s3 and data:uri urls

cloudinary-parent-1.0.3 / 2013-06-04
====================================

  * Cleanup pom.xml, Fix imageUploadTag test, Fix imports
  * Introduced a new image tag for jsps, you can use it like this:
  * don't track eclipse resources
  * Add the callback and the signature to the image tag
  * In the tag lib, use the Uploader's tag generator * Allow null file parameters
  * enhancements to the HTML processing
  * cleaned up the tag rendering. There is some more flexibility that needs to be added to the tag, but it looks like the core of it is working ok.
  * correctly located the cloudinary tld and updated to use the new classname of the tag Added a singleton manager to ease spring support.
  * renamed tag to make more sense
  * First pass at an upload tag and support code
  * Refactored Cloudinary Java into multiple modules without breaking the module naming convention already established. * Created a -taglib module to support constructing file input tags on the server side, since it requires some server side API signing. * Separate modules allow users who are writing stand-alone applications (not depending on the Servlet API) not to have a dependency on it.
  * Fixing code sample, referencing Android

cloudinary-1.0.2 / 2013-04-08
=============================

  * Upgrade version to 1.0.2-SNAPSHOT
  * Don't fail api tests if api_secret is not given
  * Don't fail api tests if api_secret is not given
  * pom fixes
  * Preparation for Maven repository submission
  * Merge Maven preperation by shakiba
  * Missing file for rename test
  * Invalidate flags in upload and destroy
  * Private download link generator
  * Support for short urls for image/upload
  * Support for folders
  * Support rename
  * Support unsafe transformation update
  * Fix tags api support of multiple public ids
  * ready for maven central
  * Fixing URLs in readme
  * Support akamai
  * Support for sprite genreation, multi and explode. Support new async/notification flags
  * Merge git://github.com/andershedstrom/cloudinary_java
  * Support for usage API call
  * Support image_metadata flag in upload and API
  * Update README.md
  * fixed regexp bug, regexp didn't work
  * Updated pom.xml to handle custom src and test-src directories
  * Allow giving pages flag to resource details API
  * Fix check for limit. Fix htmlWidth visibility
  * Support for info flags in upload
  * Support for transformation flags
  * Support deleteResourcesByTag Support keep_original in resource deletion
  * Uploader.imageUploadTag - helper for create input tag for direct upload to Cloudinary via JS
  * Added README
  * Java naming conventions. Map utility methods
  * Initial commit
