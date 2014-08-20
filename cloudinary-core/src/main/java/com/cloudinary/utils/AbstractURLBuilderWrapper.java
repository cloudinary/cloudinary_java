package com.cloudinary.utils;

public abstract class AbstractURLBuilderWrapper {

	protected String source;
	public AbstractURLBuilderWrapper(String source){
		this.source = source;
	}
	
	public abstract void addParam(String key, Object value);
	public abstract String url();
}
