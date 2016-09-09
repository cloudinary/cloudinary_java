package com.cloudinary.test;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by amir on 01/08/2016.
 */
public class MockableTest {

    protected Object getParam(String name){
        throw new NotImplementedException();
    };
    protected String getURL(){
        throw new NotImplementedException();
    };
    protected String getHttpMethod(){
        throw new NotImplementedException();
    };
}
