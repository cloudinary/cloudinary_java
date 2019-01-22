[![Build Status](https://travis-ci.org/cloudinary/cloudinary_java.svg?branch=master)](https://travis-ci.org/cloudinary/cloudinary_java)
[![Maven Central](https://img.shields.io/maven-central/v/com.cloudinary/cloudinary-core.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Acom.cloudinary)
[![license](https://img.shields.io/github/license/cloudinary/cloudinary_js.svg?maxAge=2592000)]()

Cloudinary
==========

Cloudinary is a cloud service that offers a solution to a web application's entire image management pipeline. 

Easily upload images to the cloud. Automatically perform smart image resizing, cropping and conversion without installing any complex software. 
Integrate Facebook or Twitter profile image extraction in a snap, in any dimension and style to match your website’s graphics requirements. 
Images are seamlessly delivered through a fast CDN, and much much more. 

Cloudinary offers comprehensive APIs and administration capabilities and is easy to integrate with any web application, existing or new.


Cloudinary provides URL and HTTP based APIs that can be easily integrated with any Web development framework. 

For Java, Cloudinary provides a library for simplifying the integration even further.

**Notes:** 

* There are three flavors of the library to support different HttpClient versions: cloudinary-http42, cloudinary-http43 and cloudinary-http44. 
* For Android there's a separate library available at https://github.com/cloudinary/cloudinary_android

## Getting started guide
![](https://res.cloudinary.com/cloudinary/image/upload/see_more_bullet.png)  **Take a look at our [Getting started guide for Java](https://cloudinary.com/documentation/java_integration#getting_started_guide)**.

## Setup ######################################################################

The cloudinary_java library is available in [Maven Central](https://repo1.maven.org/maven2/com/cloudinary/). To use it, add the following dependency to your pom.xml :

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.22.01.22.0</version>
</dependency>
```

Alternatively, download cloudinary_java from [here](https://repo1.maven.org/maven2/com/cloudinary/cloudinary-core/1.22.0/cloudinary-core-1.22.0.jar) and [here](https://repo1.maven.org/maven2/com/cloudinary/cloudinary-http44/1.22.0/cloudinary-http44-1.22.0.jar)
and see [build.gradle](https://github.com/cloudinary/cloudinary_java/blob/master/cloudinary-http44/build.gradle) for library dependencies.

## Try it right away

Sign up for a [free account](https://cloudinary.com/users/register/free) so you can try out image transformations and seamless image delivery through CDN.

*Note: Replace `demo` in all the following examples with your Cloudinary's `cloud name`.*  

Accessing an uploaded image with the `sample` public ID through a CDN:

    http://res.cloudinary.com/demo/image/upload/sample.jpg

![Sample](https://res.cloudinary.com/demo/image/upload/w_0.4/sample.jpg "Sample")

Generating a 150x100 version of the `sample` image and downloading it through a CDN:

    http://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill/sample.jpg

![Sample 150x100](https://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill/sample.jpg "Sample 150x100")

Converting to a 150x100 PNG with rounded corners of 20 pixels: 

    http://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill,r_20/sample.png

![Sample 150x150 Rounded PNG](https://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill,r_20/sample.png "Sample 150x150 Rounded PNG")

For plenty more transformation options, see our [image transformations documentation](http://cloudinary.com/documentation/image_transformations).

Generating a 120x90 thumbnail based on automatic face detection of the Facebook profile picture of Bill Clinton:
 
    http://res.cloudinary.com/demo/image/facebook/c_thumb,g_face,h_90,w_120/billclinton.jpg
    
![Facebook 90x120](https://res.cloudinary.com/demo/image/facebook/c_thumb,g_face,h_90,w_120/billclinton.jpg "Facebook 90x200")

For more details, see our documentation for embedding [Facebook](https://cloudinary.com/documentation/facebook_profile_pictures) and [Twitter](https://cloudinary.com/documentation/twitter_profile_pictures) profile pictures.

## Usage

### Configuration

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

### Embedding and transforming images

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

Same goes for Twitter:

```java
cloudinary.url().type("twitter_name").generate("billclinton.jpg");
```

![](https://res.cloudinary.com/cloudinary/image/upload/see_more_bullet.png) **See [our documentation](https://cloudinary.com/documentation/java_image_manipulation) for more information about displaying and transforming images in Java**.

### Upload

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

![](https://res.cloudinary.com/cloudinary/image/upload/see_more_bullet.png) **See [our documentation](https://cloudinary.com/documentation/java_image_upload) for plenty more options of uploading to the cloud from your Java code**.        

### imageTag

Returns an html image tag pointing to Cloudinary.

Usage:

```java
cloudinary.url().format("png").transformation(new Transformation().width(100).height(100).crop("fill")).imageTag("sample");

# <img src='http://res.cloudinary.com/cloud_name/image/upload/c_fill,h_100,w_100/sample.png' height='100' width='100'/>
```

### imageUploadTag

Returns an html input field for direct image upload, to be used in conjunction with [cloudinary\_js package](https://github.com/cloudinary/cloudinary_js/). It integrates [jQuery-File-Upload widget](https://github.com/blueimp/jQuery-File-Upload) and provides all the necessary parameters for a direct upload.

Usage:

```java
Map options = ObjectUtils.asMap("resource_type", "auto");
Map htmlOptions = ObjectUtils.asMap("alt", "sample");
String html = cloudinary.uploader().imageUploadTag("image_id", options, htmlOptions);
```

![](https://res.cloudinary.com/cloudinary/image/upload/see_more_bullet.png) **See [our documentation](https://cloudinary.com/documentation/java_image_upload#direct_uploading_from_the_browser) for plenty more options of uploading directly from the browser**.
  
## Additional resources ##########################################################

Additional resources are available at:

* [Website](https://cloudinary.com)
* [Interactive demo](https://demo.cloudinary.com/default)
* [Knowledge Base](https://support.cloudinary.com/hc/en-us) 
* [Documentation](https://cloudinary.com/documentation)
* [Documentation for Java integration](https://cloudinary.com/documentation/java_integration)
* [Image transformations documentation](https://cloudinary.com/documentation/image_transformations)
* [Upload API documentation](https://cloudinary.com/documentation/upload_images)

## Support

You can [open an issue through GitHub](https://github.com/cloudinary/cloudinary_java/issues).

Contact us [https://cloudinary.com/contact](https://cloudinary.com/contact)

Stay tuned for updates, tips and tutorials: [Blog](https://cloudinary.com/blog), [Twitter](https://twitter.com/cloudinary), [Facebook](https://www.facebook.com/Cloudinary).

## Join the Community ##########################################################

Impact the product, hear updates, test drive new features and more! Join [here](https://www.facebook.com/groups/CloudinaryCommunity).


## License #######################################################################

Released under the MIT license. 

