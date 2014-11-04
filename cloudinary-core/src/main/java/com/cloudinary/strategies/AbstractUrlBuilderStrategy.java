package com.cloudinary.strategies;


public abstract class AbstractUrlBuilderStrategy {
	protected String source;
	public AbstractUrlBuilderStrategy(){
	}
	
	public abstract void addParam(String key, Object value);
	public abstract String url();
	public abstract void initialize() throws Exception;

	public AbstractUrlBuilderStrategy init(String source) throws Exception{
		this.source = source;
		initialize();
		return this;
	}
}
