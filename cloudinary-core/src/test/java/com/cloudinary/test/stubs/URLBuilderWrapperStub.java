package com.cloudinary.test.stubs;

import java.net.URISyntaxException;

import com.cloudinary.utils.AbstractURLBuilderWrapper;

public class URLBuilderWrapperStub extends AbstractURLBuilderWrapper {


	public URLBuilderWrapperStub(String source) throws URISyntaxException {
		super(source);
	}

	@Override
	public void addParam(String key, Object value) {
	}

	@Override
	public String url() {
		return "";
	}

}
