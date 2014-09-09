package com.cloudinary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.text.TextUtils;

@SuppressWarnings({"rawtypes","unchecked"})
public class Transformation {
	protected Map transformation;
	protected List<Map> transformations;
	protected String htmlWidth;
	protected String htmlHeight;	

	public Transformation(Transformation transformation) {		
		this(dup(transformation.transformations));
	}
	
	// Warning: options will destructively updated!
	public Transformation(List<Map> transformations) {
		this.transformations = transformations;
		if (transformations.isEmpty()) {
			chain();
		} else {
			this.transformation = transformations.get(transformations.size() - 1);
		}
	}
	
	public Transformation() {
		this.transformations = new ArrayList<Map>();
		chain();
	}

	public Transformation width(Object value) { return param("width", value); }
	public Transformation height(Object value) { return param("height", value); }
	public Transformation named(String...value) { return param("transformation", value); }
	public Transformation crop(String value) { return param("crop", value); }
	public Transformation background(String value) { return param("background", value); }
	public Transformation color(String value) { return param("color", value); }
	public Transformation effect(String value) { return param("effect", value); }
	public Transformation effect(String effect, Object param) { return param("effect", effect + ":" + param); }
	public Transformation angle(int value) { return param("angle", value); }
	public Transformation angle(String...value) { return param("angle", value); }
	public Transformation border(String value) { return param("border", value); }
	public Transformation border(int width, String color) { return param("border", "" + width + "px_solid_" + color.replaceFirst("^#", "rgb:")); }
	public Transformation x(Object value) { return param("x", value); }
	public Transformation y(Object value) { return param("y", value); }
	public Transformation radius(Object value) { return param("radius", value); }
	public Transformation quality(Object value) { return param("quality", value); }
	public Transformation defaultImage(String value) { return param("default_image", value); }
	public Transformation gravity(String value) { return param("gravity", value); }
	public Transformation colorSpace(String value) { return param("color_space", value); }
	public Transformation prefix(String value) { return param("prefix", value); }
	public Transformation overlay(String value) { return param("overlay", value); }
	public Transformation underlay(String value) { return param("underlay", value); }
	public Transformation fetchFormat(String value) { return param("fetch_format", value); }
	public Transformation density(Object value) { return param("density", value); }
	public Transformation page(Object value) { return param("page", value); }
	public Transformation delay(Object value) { return param("delay", value); }
	public Transformation rawTransformation(String value) { return param("raw_transformation", value); }
	public Transformation flags(String...value) { return param("flags", value); }
	
	// Warning: options will destructively updated!
	public Transformation params(Map transformation) {
		this.transformation = transformation;
		transformations.add(transformation);
		return this;
	}
	
	public Transformation chain() {
		return params(new HashMap());
	}
	
	public Transformation param(String key, Object value) {
		transformation.put(key, value);
		return this;
	}

	public String generate() {
		return generate(transformations);
	}

	public String generate(Iterable<Map> optionsList) {
		List<String> components = new ArrayList<String>();
		for (Map options : optionsList) {
			components.add(generate(options));
		}
		return TextUtils.join("/", components);
	}

	public String generate(Map options) {
		String size = (String) options.get("size");
		if (size != null) {
			String[] size_components = size.split("x");
			options.put("width", size_components[0]);
			options.put("height", size_components[1]);
		}
		String width = this.htmlWidth = Cloudinary.asString(options.get("width"));
		String height = this.htmlHeight = Cloudinary.asString(options.get("height"));
		boolean has_layer = !TextUtils.isEmpty((String) options.get("overlay")) || 
				!TextUtils.isEmpty((String) options.get("underlay"));
		     
		String crop = (String) options.get("crop");
		String angle = TextUtils.join(".", Cloudinary.asArray(options.get("angle")));
		
		boolean no_html_sizes = has_layer || !TextUtils.isEmpty(angle) || "fit".equals(crop) || "limit".equals(crop);
		if (width != null && (Float.parseFloat(width) < 1 || no_html_sizes)) {
			this.htmlWidth = null;
		}
		if (height != null && (Float.parseFloat(height) < 1 || no_html_sizes)) {
			this.htmlHeight = null;
		}

		String background = (String) options.get("background");
		if (background != null) {
			background = background.replaceFirst("^#", "rgb:");
		}
		    
		String color = (String) options.get("color");
		if (color != null) {
			color = color.replaceFirst("^#", "rgb:");
		}
		    
		List transformations = Cloudinary.asArray(options.get("transformation"));
		boolean allNamed = true;		
		for (Object baseTransformation : transformations) {			
			if (baseTransformation instanceof Map) {
				allNamed = false;
				break;
			}
		}
		String namedTransformation = null; 
		if (allNamed) {
			namedTransformation = TextUtils.join(".", transformations);
			transformations = new ArrayList();
		} else {
			List ts = transformations;
			transformations = new ArrayList();
			for (Object baseTransformation : ts) {
				String transformationString;
				if (baseTransformation instanceof Map) {
					transformationString = generate((Map) baseTransformation); 
				} else {
					Map map = new HashMap();
					map.put("transformation", baseTransformation);
					transformationString = generate(map);
				}
				transformations.add(transformationString);
			}
		}

		String flags = TextUtils.join(".", Cloudinary.asArray(options.get("flags")));
		
		SortedMap<String, String> params = new TreeMap<String, String>();
		params.put("w", width);
		params.put("h", height);
		params.put("t", namedTransformation);
		params.put("c", crop);
		params.put("b", background);
		params.put("co", color);
		params.put("a", angle);
		params.put("fl", flags);
		String[] simple_params = new String[]{
			"x", "x", "y", "y", "r", "radius", "d", "default_image", "g", "gravity", "cs", "color_space",
			"p", "prefix", "l", "overlay", "u", "underlay", "f", "fetch_format", "dn", "density",
			"pg", "page", "dl", "delay", "e", "effect", "bo", "border", "q", "quality"
		};
		for (int i = 0; i < simple_params.length; i+=2) {
			params.put(simple_params[i], Cloudinary.asString(options.get(simple_params[i+1])));
		}
		List<String> components = new ArrayList<String>();
		for (Map.Entry<String, String> param : params.entrySet()) {
			if (!TextUtils.isEmpty(param.getValue())) {
				components.add(param.getKey() + "_" + param.getValue());
			}
		}
		String raw_transformation = (String) options.get("raw_transformation");
		if (raw_transformation != null) {
			components.add(raw_transformation);
		}
		if (!components.isEmpty()) {
			transformations.add(TextUtils.join(",", components));
		}		
		return TextUtils.join("/", transformations);
	}

	public String getHtmlWidth() {
		return htmlWidth;
	}

	public String getHtmlHeight() {
		return htmlHeight;
	}

	private static List<Map> dup(List<Map> transformations) {
		List<Map> result = new ArrayList<Map>(); 
		for (Map params : transformations) {
			result.add(new HashMap(params));
		}
		return result;
	}
}
