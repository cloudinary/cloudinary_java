Cloudinary Java/Spring MVC Sample Project on Google AppEngine
=============================================================

A simple web application that allows you to uploads photos, maintain a database with references to them, list them with their metadata, and display them using various cloud-based transformations.

## Installation

Run the following commands from your shell.

Clone the Cloudinary Java project: 

    git clone git://github.com/cloudinary/cloudinary_java.git      

This sample relies on a version of the Cloudinary client not yet published so we need to install it locally:

    cd cloudinary_java
    mvn install

Go to the sample project folder:

    cd samples/photo_album_gae

## Configuration

Next you need to pass your Cloudinary account's Cloud Name, API Key, and API Secret. This sample application assumes these values exists in the form
of a `CLOUDINARY_URL`. The `CLOUDINARY_URL` value is available in the [dashboard of your Cloudinary account](https://cloudinary.com/console). 
If you don't have a Cloudinary account yet, [click here](https://cloudinary.com/users/register/free) to create one for free.

    cp src/main/webapp/WEB-INF/appengine-web.xml.sample src/main/webapp/WEB-INF/appengine-web.xml

Edit the new `appengine-web.xml` file and set `CLOUDINARY_URL` to the value matching your account.
*Note*: you can also change the application ID to match a remote project ID you might have started.

## Running

Running the application locally first clean and compile to make sure everything is working as it should:

    mvn clean compile

Then start the application:  

    mvn appengine:devserver_start

and point your browser to [http://localhost:8080/](http://localhost:8080/).

This sample configures the Cloudinary client with `GAEConnectionManager` from [here](http://esxx.blogspot.co.il/2009/06/using-apaches-httpclient-on-google-app.html).
You can use any other connection manager or leave the default if you have the sockets enabled for your application. In the development environment it should work with either.
Also, the way the `GAEConnectionManager` is instantiated and managed in this sample is not pretty. You should change it when building a real application.