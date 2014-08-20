package com.cloudinary.test.stubs;

import java.net.URISyntaxException;
import java.util.Map;

import com.cloudinary.CloudinaryBase;
import com.cloudinary.UploaderBase;
import com.cloudinary.api.ApiBase;
import com.cloudinary.utils.AbstractURLBuilderWrapper;

public class CloudinaryStub extends CloudinaryBase {
	public CloudinaryStub() {
		super();
	}

	public CloudinaryStub(String string) {
		super(string);
	}

	@SuppressWarnings("rawtypes")
	public CloudinaryStub(Map config) {
		super(config);
	}
	
	@Override
	public AbstractURLBuilderWrapper urlBuilder(String source) throws URISyntaxException {
		return new URLBuilderWrapperStub(source);
	}

	@Override
	public UploaderBase uploader() {
		return new UploaderStub(this).withConnectionManager(connectionManager);
	}

	@Override
	public ApiBase api() {
		return new ApiStub(this).withConnectionManager(connectionManager);
	}


}
