package com.cloudinary;

import java.io.Serializable;


public class Radius implements Serializable {

private Object radius;
    String concat = "";

    public Radius() {
    }

    public Radius(Object value) {
        if(value==null || value=="")
            throw new IllegalArgumentException("Must supply value for the radius");
        else {
            this.radius = value;
            Transformation transformation = new Transformation();
            transformation.radius(value);
        }
    }

    public Radius(Object topLeftAndBottomRight, Object topRightAndBottomLeft) {
        if(topLeftAndBottomRight==null || topRightAndBottomLeft==null)
            throw new IllegalArgumentException("Must supply two values for the radius ");
        else{
            radius = topLeftAndBottomRight + ":" + topRightAndBottomLeft;
            Transformation transformation = new Transformation();
            transformation.radius(radius);
        }
    }

    public Radius(Object topLeft, Object topRightAndBottomLeft, Object bottomRight) {
        if(topLeft==null || topRightAndBottomLeft==null || bottomRight==null)
            throw new IllegalArgumentException("Must supply three values for the radius ");
        else {
            radius = topLeft + ":" + topRightAndBottomLeft + ":" + bottomRight;
            Transformation transformation = new Transformation();
            transformation.radius(radius);
        }
    }

    public Radius(Object topLeft, Object topRight, Object bottomRight, Object bottomLeft) {
        if(topLeft==null || topRight==null || bottomRight==null || bottomLeft==null )
            throw new IllegalArgumentException("Must supply four values for the radius ");
        else{
            radius = topLeft + ":" + topRight + ":" + bottomRight + ":" + bottomLeft;
            Transformation transformation = new Transformation();
            transformation.radius(radius);
        }
    }

    public Radius(Object[] array){
        if(array==null || array.length==0 || array.length>4)
            throw new IllegalArgumentException("Array length cannot be 0 or more than 4");
        else {
            for (Object value : array) {
                if(value==null)
                    throw new IllegalArgumentException("array value cannot be null");
                    concat = concat + ":" + value;
            }
            concat = concat.substring(1, concat.length());
            Transformation transformation = new Transformation();
            transformation.radius(concat);
        }
    }
    @Override
    public String toString() {
        if (radius != null) {
            return String.format("r_" + radius.toString());
        }
        else{
            return String.format("r_" + concat);
        }
    }
}

