package com.cloudinary.test.stubs;

import java.util.Map;

import com.cloudinary.CloudinaryBase;
import com.cloudinary.api.ApiBase;
import com.cloudinary.api.ApiResponse;

public class ApiStub extends ApiBase {
	public ApiStub(CloudinaryBase cloudinary) {
		super(cloudinary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ApiResponse callApi(HttpMethod method, Iterable<String> uri, Map<String, ? extends Object> params, Map options) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
