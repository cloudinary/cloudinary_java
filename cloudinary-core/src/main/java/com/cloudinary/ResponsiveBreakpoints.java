package com.cloudinary;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

public class ResponsiveBreakpoints {
	private boolean createDerived = true;
	private Transformation transformation = null;
	private Integer maxWidth = null;  //default 1000
	private Integer minWidth = null;  //default 50
	private Integer bytesStep = null; //default 20kb
	private Integer maxImages = null; //default 20
	
	public boolean isCreateDerived() {
		return createDerived;
	}
	public ResponsiveBreakpoints createDerived(boolean createDerived) {
		this.createDerived = createDerived;
		return this;
	}
	
	public Transformation transformation() {
		return transformation;
	}
	public ResponsiveBreakpoints transformation(Transformation transformation) {
		this.transformation = transformation;
		return this;
	}
	
	public Integer maxWidth() {
		return maxWidth;
	}
	public ResponsiveBreakpoints maxWidth(Integer maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}
	
	public Integer minWidth() {
		return minWidth;
	}
	public ResponsiveBreakpoints minWidth(Integer minWidth) {
		this.minWidth = minWidth;
		return this;
	}
	
	public Integer bytesStep() {
		return bytesStep;
	}
	public ResponsiveBreakpoints bytesStep(Integer bytesStep) {
		this.bytesStep = bytesStep;
		return this;
	}
	
	public Integer maxImages() {
		return maxImages;
	}
	public ResponsiveBreakpoints maxImages(Integer maxImages) {
		this.maxImages = maxImages;
		return this;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("create_derived", createDerived);
		if (transformation != null)
			json.put("transformation", transformation.generate());
		if (maxWidth != null)
			json.put("max_width", maxWidth);
		if (minWidth != null)
			json.put("min_width", minWidth);
		if (bytesStep != null)
			json.put("bytes_step", bytesStep);
		if (maxImages != null)
			json.put("max_images", maxImages);
		return json;
	}
	
	public static String toJsonString(Object breakpoints) {
		if (breakpoints == null)
			return null;
		
		JSONArray arr = new JSONArray();
		if (breakpoints instanceof ResponsiveBreakpoints) {
			arr.put(0, ((ResponsiveBreakpoints) breakpoints).toJson());
		} else if (breakpoints instanceof ResponsiveBreakpoints[]) {
			for (ResponsiveBreakpoints i : (ResponsiveBreakpoints[]) breakpoints) {
				arr.put(i.toJson());
			}
		} else {
			throw new IllegalArgumentException("breakpoints must be either of type ResponsiveBreakpoints or ResponsiveBreakpoints[]");
		}
		return arr.toString();
	}
	
}
