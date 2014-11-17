package com.cloudinary.android;

import java.net.URISyntaxException;

import android.net.Uri;

import com.cloudinary.strategies.AbstractUrlBuilderStrategy;

public class UrlBuilderStrategy extends AbstractUrlBuilderStrategy {

	private Uri.Builder  builder;

	public UrlBuilderStrategy() throws URISyntaxException {
		super();
	}
	
	@Override
	public void addParam(String key, Object value) {
		builder.appendQueryParameter(key, (String) value);
	}

	@Override
	public String url() {
		return builder.toString();
	}

	@Override
	public void initialize() throws Exception {
		this.builder = Uri.parse(source).buildUpon();
		
	}

}
