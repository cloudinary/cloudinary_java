package com.cloudinary;

/** This class contains a singleton in a generic way. This class is used by the tags to
 * retrieve the Cloudinary configuration.
 * 
 * the containing framework is responsible for registering the cloudinary configuration with the
 * Singleton, and then removing it on shutdown. This allows the user to use Spring or any other
 * framework without imposing additional dependencies on the cloudinary project.
 *  
 * @author jpollak
 *
 */
public class Singleton {

    private static Cloudinary cloudinary;
    
    public static void registerCloudinary(Cloudinary cloudinary) {
        Singleton.cloudinary = cloudinary;
    }
    
    public static void deregisterCloudinary() {
        cloudinary = null;
    }

    private static class DefaultCloudinaryHolder {
        public static final Cloudinary INSTANCE = new Cloudinary();
    }
    
    public static Cloudinary getCloudinary() {
        if (cloudinary == null) {
            return DefaultCloudinaryHolder.INSTANCE;
        }
        return cloudinary;
    }
}
