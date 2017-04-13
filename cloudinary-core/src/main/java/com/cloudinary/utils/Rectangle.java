package com.cloudinary.utils;

import java.io.Serializable;

public class Rectangle implements Serializable{

    public int height;
    public int width;
    public int y;
    public int x;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}
