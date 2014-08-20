package com.cloudinary;

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import com.cloudinary.utils.AbstractURLBuilderWrapper;

public class URLBuilderWrapper extends AbstractURLBuilderWrapper {

	private URIBuilder builder;

	public URLBuilderWrapper(String source) throws URISyntaxException {
		super(source);
		this.builder = new URIBuilder(source);
	}

	@Override
	public void addParam(String key, Object value) {
		builder.addParameter(key, (String) value);
	}

	@Override
	public String url() {
		return builder.toString();
	}

}
