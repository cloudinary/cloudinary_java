package com.cloudinary;

public class SingletonManager {

    private Cloudinary cloudinary;
    
    public void setCloudinary(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    
    public void init() {
        Singleton.registerCloudinary(cloudinary);
    }
    
    public void destroy() {
        Singleton.deregisterCloudinary();
    }
}
