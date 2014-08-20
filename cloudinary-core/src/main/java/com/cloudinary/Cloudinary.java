package com.cloudinary;

import java.net.URISyntaxException;
import java.util.Map;

import com.cloudinary.api.ApiBase;
import com.cloudinary.utils.AbstractURLBuilderWrapper;

public class Cloudinary extends CloudinaryBase {

	public Cloudinary() {
		super();
	}

	public Cloudinary(String string) {
		super(string);
	}

	@SuppressWarnings("rawtypes")
	public Cloudinary(Map config) {
		super(config);
	}

	@Override
	public AbstractURLBuilderWrapper urlBuilder(String source) throws URISyntaxException {
		return new URLBuilderWrapper(source);
	}

	@Override
	public UploaderBase uploader() {
		return new Uploader(this).withConnectionManager(connectionManager);
	}

	@Override
	public ApiBase api() {
		return new Api(this).withConnectionManager(connectionManager);
	}

}
