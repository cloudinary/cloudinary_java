package com.cloudinary.http44;

import org.apache.http.client.utils.URIBuilder;

public class UrlBuilderStrategy extends com.cloudinary.strategies.AbstractUrlBuilderStrategy {
	
	private URIBuilder builder;
	@Override
	public void addParam(String key, Object value){
		builder.addParameter(key, (String) value);
	}
	
	@Override
	public String url(){
		return builder.toString();
	}
	
	@Override
	public void initialize() throws Exception {
		this.builder = new URIBuilder(source);
	}

}
