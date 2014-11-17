Cloudinary
==========

Cloudinary is a cloud service that offers a solution to a web application's entire image management pipeline.

Easily upload images to the cloud. Automatically perform smart image resizing, cropping and conversion without installing any complex software.
Integrate Facebook or Twitter profile image extraction in a snap, in any dimension and style to match your website’s graphics requirements.
Images are seamlessly delivered through a fast CDN, and much much more.

Cloudinary offers comprehensive APIs and administration capabilities and is easy to integrate with any web application, existing or new.

Cloudinary provides URL and HTTP based APIs that can be easily integrated with any Web development framework.

For Android, Cloudinary provides a library for simplifying the integration even further. The library requires Android 2.3 or higher.

## Manual Setup ######################################################################
Download cloudinary-core-1.1.0.jar from [here](http://search.maven.org/remotecontent?filepath=com/cloudinary/cloudinary/1.1.0/cloudinary-core-1.1.0.jar) and cloudinary-android-1.1.0.jar from [here](http://search.maven.org/remotecontent?filepath=com/cloudinary/cloudinary/1.1.0/cloudinary-android-1.1.0.jar)
and put them in your libs folder.

## Maven Integration ######################################################################
The cloudinary_java library is available in [Maven Central](http://repo1.maven.org/maven/). To use it, add the following dependency to your pom.xml:

    <dependency>
        <groupId>com.cloudinary</groupId>
        <artifactId>cloudinary-android</artifactId>
        <version>1.1.0</version>
    </dependency>


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

For more details, see our documentation for embedding [Facebook](http://cloudinary.com/documentation/facebook_profile_pictures) and [Twitter](http://cloudinary.com/documentation/twitter_profile_pictures) profile pictures.


## Usage

### Configuration

Each request for building a URL of a remote cloud resource must have the `cloud_name` parameter set.
Each request to our secure APIs (e.g., image uploads, eager sprite generation) must have the `api_key` and `api_secret` parameters set.
See [API, URLs and access identifiers](http://cloudinary.com/documentation/api_and_access_identifiers) for more details.

Setting the `cloud_name`, `api_key` and `api_secret` parameters can be done either directly in each call to a Cloudinary method,
by when initializing the Cloudinary object, or by using the CLOUDINARY_URL meta-data property.

The entry point of the library is the Cloudinary object.

Here's an example of setting the configuration parameters programatically:

    Map config = new HashMap();
    config.put("cloud_name", "n07t21i7");
    config.put("api_key", "123456789012345");
    config.put("api_secret", "abcdeghijklmnopqrstuvwxyz12");
    Cloudinary cloudinary = new Cloudinary(config);

Another example of setting the configuration parameters by providing the CLOUDINARY_URL value to the constructor:

    Cloudinary cloudinary = new Cloudinary("cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@n07t21i7");

Giving the context will allow Cloudinary to configure from the application's meta-data.

    Cloudinary cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(getContext()));

Then add a meta-data property to your application section in the AndroidManifest.xml

    <manifest>
        ...
        <application>
            ...
            <meta-data android:name="CLOUDINARY_URL" android:value="cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@n07t21i7"/>
        </application>
    <manifest>

### Embedding and transforming images

Any image uploaded to Cloudinary can be transformed and embedded using powerful view helper methods:

The following example generates the url for accessing an uploaded `sample` image while transforming it to fill a 100x150 rectangle:

    cloudinary.url().transformation(new Transformation().width(100).height(150).crop("fill")).generate("sample.jpg")

...
