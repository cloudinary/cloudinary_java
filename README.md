[![Build Status](https://travis-ci.org/cloudinary/cloudinary_java.svg?branch=master)](https://travis-ci.org/cloudinary/cloudinary_java)

Cloudinary
==========

## About
The Cloudinary Java SDK allows you to quickly and easily integrate your application with Cloudinary.
Effortlessly optimize and transform your cloud's assets.

### Additional documentation
This Readme provides basic installation and usage information.
For the complete documentation, see the [Java SDK Guide](https://cloudinary.com/documentation/java_integration).

## Table of Contents
- [Key Features](#key-features)
- [Version Support](#Version-Support)
- [Installation](#installation)
- [Usage](#usage)
    - [Setup](#Setup)
    - [Transform and Optimize Assets](#Transform-and-Optimize-Assets)
    - [File upload](#File-upload)

## Key Features
- [Transform](https://cloudinary.com/documentation/java_video_manipulation) and [optimize](https://cloudinary.com/documentation/java_image_manipulation#image_optimizations) assets (links to docs).
- [Upload assets to cloud](https://cloudinary.com/documentation/java_image_and_video_upload)

## Version Support
| SDK Version    | Java 6+ |
|----------------|---------|
| 1.1.0 - 1.33.0 | V       |

## Installation
The cloudinary_java library is available in [Maven Central](https://mvnrepository.com/artifact/com.cloudinary/cloudinary-core). To use it, add the following dependency to your pom.xml :

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.33.0</version>
</dependency>
```

Alternatively, download cloudinary_java from [here](https://repo1.maven.org/maven2/com/cloudinary/cloudinary-core/1.30.0/cloudinary-core-1.30.0.jar) and [here](https://repo1.maven.org/maven2/com/cloudinary/cloudinary-http44/1.30.0/cloudinary-http44-1.30.0.jar)
and see [build.gradle](https://github.com/cloudinary/cloudinary_java/blob/master/cloudinary-http44/build.gradle) for library dependencies.

## Usage
### Setup

Each request for building a URL of a remote cloud resource must have the `cloud_name` parameter set.
Each request to our secure APIs (e.g., image uploads, eager sprite generation) must have the `api_key` and `api_secret` parameters set.
See [API, URLs and access identifiers](https://cloudinary.com/documentation/solution_overview#account_and_api_setup) for more details.

Setting the `cloud_name`, `api_key` and `api_secret` parameters can be done either directly in each call to a Cloudinary method,
by when initializing the Cloudinary object, or by using the CLOUDINARY_URL environment variable / system property.

The entry point of the library is the Cloudinary object.
```java
Cloudinary cloudinary = new Cloudinary();
```

Here's an example of setting the configuration parameters programatically:

```java
Map config = new HashMap();
config.put("cloud_name", "n07t21i7");
config.put("api_key", "123456789012345");
config.put("api_secret", "abcdeghijklmnopqrstuvwxyz12");
Cloudinary cloudinary = new Cloudinary(config);
```

Another example of setting the configuration parameters by providing the CLOUDINARY_URL value to the constructor:

    Cloudinary cloudinary = new Cloudinary("cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@n07t21i7");

### Transform and Optimize Assets
- [See full documentation](https://cloudinary.com/documentation/java_image_manipulation)
Any image uploaded to Cloudinary can be transformed and embedded using powerful view helper methods:

The following example generates the url for accessing an uploaded `sample` image while transforming it to fill a 100x150 rectangle:

```java
cloudinary.url().transformation(new Transformation().width(100).height(150).crop("fill")).generate("sample.jpg");
```

Another example, emedding a smaller version of an uploaded image while generating a 90x90 face detection based thumbnail:

```java
cloudinary.url().transformation(new Transformation().width(90).height(90).crop("thumb").gravity("face")).generate("woman.jpg");
```

You can provide either a Facebook name or a numeric ID of a Facebook profile or a fan page.

Embedding a Facebook profile to match your graphic design is very simple:

```java
cloudinary.url().type("facebook").transformation(new Transformation().width(130).height(130).crop("fill").gravity("north_west")).generate("billclinton.jpg");
```

### File upload
Assuming you have your Cloudinary configuration parameters defined (`cloud_name`, `api_key`, `api_secret`), uploading to Cloudinary is very simple.

The following example uploads a local JPG to the cloud:

```java
cloudinary.uploader().upload("my_picture.jpg", ObjectUtils.emptyMap());
```

The uploaded image is assigned a randomly generated public ID. The image is immediately available for download through a CDN:

```java
cloudinary.url().generate("abcfrmo8zul1mafopawefg.jpg");

# http://res.cloudinary.com/demo/image/upload/abcfrmo8zul1mafopawefg.jpg
```

You can also specify your own public ID:

```java
cloudinary.uploader().upload("http://www.example.com/image.jpg", ObjectUtils.asMap("public_id", "sample_remote"));

cloudinary.url().generate("sample_remote.jpg");

# http://res.cloudinary.com/demo/image/upload/sample_remote.jpg
```

## Contributions
See [contributing guidelines](/CONTRIBUTING.md).

## Get Help
- [Open a Github issue](https://github.com/CloudinaryLtd/cloudinary_java/issues) (for issues related to the SDK)
- [Open a support ticket](https://cloudinary.com/contact) (for issues related to your account)

## About Cloudinary
Cloudinary is a powerful media API for websites and mobile apps alike, Cloudinary enables developers to efficiently manage, transform, optimize, and deliver images and videos through multiple CDNs. Ultimately, viewers enjoy responsive and personalized visual-media experiences—irrespective of the viewing device.

## Additional Resources
- [Cloudinary Transformation and REST API References](https://cloudinary.com/documentation/cloudinary_references): Comprehensive references, including syntax and examples for all SDKs.
- [MediaJams.dev](https://mediajams.dev/): Bite-size use-case tutorials written by and for Cloudinary Developers
- [DevJams](https://www.youtube.com/playlist?list=PL8dVGjLA2oMr09amgERARsZyrOz_sPvqw): Cloudinary developer podcasts on YouTube.
- [Cloudinary Academy](https://training.cloudinary.com/): Free self-paced courses, instructor-led virtual courses, and on-site courses.
- [Code Explorers and Feature Demos](https://cloudinary.com/documentation/code_explorers_demos_index): A one-stop shop for all code explorers, Postman collections, and feature demos found in the docs.
- [Cloudinary Roadmap](https://cloudinary.com/roadmap): Your chance to follow, vote, or suggest what Cloudinary should develop next.
- [Cloudinary Facebook Community](https://www.facebook.com/groups/CloudinaryCommunity): Learn from and offer help to other Cloudinary developers.
- [Cloudinary Account Registration](https://cloudinary.com/users/register/free): Free Cloudinary account registration.
- [Cloudinary Website](https://cloudinary.com)

## Licence
Released under the MIT license.
