package com.cloudinary.test.stubs;

import java.io.IOException;
import java.util.Map;

import com.cloudinary.CloudinaryBase;
import com.cloudinary.UploaderBase;

public class UploaderStub extends UploaderBase {

	public UploaderStub(CloudinaryBase cloudinary) {
		super(cloudinary);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
