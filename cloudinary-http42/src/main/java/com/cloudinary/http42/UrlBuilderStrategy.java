package com.cloudinary.http42;

import com.cloudinary.strategies.AbstractUrlBuilderStrategy;
import org.apache.http.client.utils.URIBuilder;

public class UrlBuilderStrategy extends AbstractUrlBuilderStrategy {
	
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
