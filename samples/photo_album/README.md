Cloudinary Java/Spring MVC Sample Project
=========================================

A simple web application that allows you to uploads photos, maintain a database with references to them, list them with their metadata, and display them using various cloud-based transformations.

## Installation

Run the following commands from your shell.

Clone the Cloudinary Java project: 

    git clone git://github.com/cloudinary/cloudinary_java.git    
        
Compile the sample project and create a WAR file (package):

    cd samples/photo_album
    mvn compile && mvn package

A WAR file should have been created in:

    target/photo_album.war

You need to deploy this war file into your J2EE container of choice. The following instructions assume a *nix with [Tomcat](http://tomcat.apache.org/) 7 on `$CATALINA_HOME`.

    mv target/photo_album.war $CATALINA_HOME/webapps/

If you would like to deploy the sample application in the root server path do this instead:

    mv target/photo_album.war $CATALINA_HOME/webapps/ROOT.war

## Configuration

Next you need to pass your Cloudinary account's Cloud Name, API Key, and API Secret. This sample application assumes these values exists in the form
of a `CLOUDINARY_URL` settings either as an environment variable or as system property. The `CLOUDINARY_URL` value is available in the [dashboard of your Cloudinary account](https://cloudinary.com/console). 
If you don't have a Cloudinary account yet, [click here](https://cloudinary.com/users/register/free) to create one for free.

The specific method with which you pass that information to Tomcat (or any other Servlet container) is not important but we present 2 alternatives here.

* Start Tomcat with `CLOUDINARY_URL` as a process bound environment variable
    
        CLOUDINARY_URL=cloudinary://<API-KEY>:<API-SECRET>@<CLOUD-NAME> $CATALINA_HOME/bin/startup.sh
    
* Set `TOMCAT_OPTS` environment variable either globally (`/etc/profile`) or for the user running Tomcat (`~/.profile`)

        TOMCAT_OPTS=-DCLOUDINARY_URL=cloudinary://<API-KEY>:<API-SECRET>@<CLOUD-NAME>

## Running

If you chose the second option you will need to now start Tomcat:

    $CATALINA_HOME/bin/startup.sh
    
and point your browser to [http://localhost:8080/photo_album/](http://localhost:8080/photo_album/) or [http://localhost:8080/](http://localhost:8080/) if you opted to put the WAR file in root. *Note:* you might need to give Tomcat a while to pick the deployed WAR file explode it, and deploy it.

This sample uses an in memory [HSQLDB](http://hsqldb.org/) database so you should expect all data to be lost when the servlet process ends.