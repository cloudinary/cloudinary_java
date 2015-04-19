package com.cloudinary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Transformation {
	protected Map transformation;
	protected List<Map> transformations;
	protected String htmlWidth;
	protected String htmlHeight;
	protected boolean hiDPI = false;
	protected boolean isResponsive = false;
	protected static boolean defaultIsResponsive = false;
	protected static Object defaultDPR = null;

	private static final Map DEFAULT_RESPONSIVE_WIDTH_TRANSFORMATION = ObjectUtils.asMap("width", "auto", "crop", "limit");
	protected static Map responsiveWidthTransformation = null;
	private static final Pattern RANGE_VALUE_RE = Pattern.compile("^((?:\\d+\\.)?\\d+)([%pP])?$");
	private static final Pattern RANGE_RE =  Pattern.compile("^(\\d+\\.)?\\d+[%pP]?\\.\\.(\\d+\\.)?\\d+[%pP]?$");

	public Transformation(Transformation transformation) {
		this(dup(transformation.transformations));
		this.hiDPI = transformation.isHiDPI();
		this.isResponsive = transformation.isResponsive();
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

	public Transformation width(Object value) {
		return param("width", value);
	}

	public Transformation height(Object value) {
		return param("height", value);
	}

	public Transformation named(String... value) {
		return param("transformation", value);
	}

	public Transformation crop(String value) {
		return param("crop", value);
	}

	public Transformation background(String value) {
		return param("background", value);
	}

	public Transformation color(String value) {
		return param("color", value);
	}

	public Transformation effect(String value) {
		return param("effect", value);
	}

	public Transformation effect(String effect, Object param) {
		return param("effect", effect + ":" + param);
	}

	public Transformation angle(int value) {
		return param("angle", value);
	}

	public Transformation angle(String... value) {
		return param("angle", value);
	}

	public Transformation border(String value) {
		return param("border", value);
	}

	public Transformation border(int width, String color) {
		return param("border", "" + width + "px_solid_" + color.replaceFirst("^#", "rgb:"));
	}

	public Transformation x(Object value) {
		return param("x", value);
	}

	public Transformation y(Object value) {
		return param("y", value);
	}

	public Transformation radius(Object value) {
		return param("radius", value);
	}

	public Transformation quality(Object value) {
		return param("quality", value);
	}

	public Transformation defaultImage(String value) {
		return param("default_image", value);
	}

	public Transformation gravity(String value) {
		return param("gravity", value);
	}

	public Transformation colorSpace(String value) {
		return param("color_space", value);
	}

	public Transformation prefix(String value) {
		return param("prefix", value);
	}

	public Transformation overlay(String value) {
		return param("overlay", value);
	}

	public Transformation underlay(String value) {
		return param("underlay", value);
	}

	public Transformation fetchFormat(String value) {
		return param("fetch_format", value);
	}

	public Transformation density(Object value) {
		return param("density", value);
	}

	public Transformation page(Object value) {
		return param("page", value);
	}

	public Transformation delay(Object value) {
		return param("delay", value);
	}

	public Transformation opacity(int value) {
		return param("opacity", value);
	}

	public Transformation rawTransformation(String value) {
		return param("raw_transformation", value);
	}

	public Transformation flags(String... value) {
		return param("flags", value);
	}

	public Transformation dpr(float value) {
		return param("dpr", value);
	}

	public Transformation dpr(int value) {
		return param("dpr", value);
	}

	public Transformation dpr(String value) {
		return param("dpr", value);
	}
	
	public Transformation duration(String value) {
		return param("duration", value);
	}
	
	public Transformation duration(float value) {
		return param("duration", new Float(value));
	}
	
	public Transformation duration(double value) {
		return param("duration", new Double(value));
	}

	public Transformation durationPercent(float value) {
		return param("duration", new Float(value).toString() + "p");
	}
	
	public Transformation durationPercent(double value) {
		return param("duration", new Double(value).toString() + "p");
	}
	
	public Transformation startOffset(String value) {
		return param("start_offset", value);
	}
	
	public Transformation startOffset(float value) {
		return param("start_offset", new Float(value));
	}
	
	public Transformation startOffset(double value) {
		return param("start_offset", new Double(value));
	}
	
	public Transformation startOffsetPercent(float value) {
		return param("start_offset", new Float(value).toString() + "p");
	}
	
	public Transformation startOffsetPercent(double value) {
		return param("start_offset", new Double(value).toString() + "p");
	}

	public Transformation endOffset(String value) {
		return param("end_offset", value);
	}
	
	public Transformation endOffset(float value) {
		return param("end_offset", new Float(value));
	}
	
	public Transformation endOffset(double value) {
		return param("end_offset", new Double(value));
	}

	public Transformation endOffsetPercent(float value) {
		return param("end_offset", new Float(value).toString() + "p");
	}
	
	public Transformation endOffsetPercent(double value) {
		return param("end_offset", new Double(value).toString() + "p");
	}
	
	public Transformation offset(String value) {
		return param("offset", value);
	}
	
	public Transformation offset(String[] value) {
		if (value.length < 2) throw new IllegalArgumentException("Offset range must include at least 2 items");
		return param("offset", value);
	}
	
	public Transformation offset(float[] value) {
		if (value.length < 2) throw new IllegalArgumentException("Offset range must include at least 2 items");
		Number[] numberArray = new Number[]{value[0], value[1]};
		return offset(numberArray);
	}
	
	public Transformation offset(double[] value) {
		if (value.length < 2) throw new IllegalArgumentException("Offset range must include at least 2 items");
		Number[] numberArray = new Number[]{value[0], value[1]};
		return offset(numberArray);
	}
	
	public Transformation offset(Number[] value) {
		if (value.length < 2) throw new IllegalArgumentException("Offset range must include at least 2 items");
		return param("offset", value);
	}

	public Transformation videoCodec(String value) {
		return param("video_codec", value);
	}

	public Transformation videoCodec(Map<String, String> value) {
		return param("video_codec", value);
	}

	public Transformation audioCodec(String value) {
		return param("audio_codec", value);
	}

	public Transformation audioFrequency(String value) {
		return param("audio_frequency", value);
	}

	public Transformation audioFrequency(int value) {
		return param("audio_frequency", value);
	}

	public Transformation bitRate(String value) {
		return param("bit_rate", value);
	}

	public Transformation bitRate(int value) {
		return param("bit_rate", new Integer(value));
	}
	
	public Transformation videoSampling(String value) {
		return param("video_sampling", value);
	}
	
	public Transformation videoSamplingFrames(int value) {
		return param("video_sampling", value);
	}
	
	public Transformation videoSamplingSeconds(Number value) {
		return param("video_sampling", value.toString() + "s");
	}
	
	public Transformation videoSamplingSeconds(int value) {
		return videoSamplingSeconds(new Integer(value));
	}
	
	public Transformation videoSamplingSeconds(float value) {
		return videoSamplingSeconds(new Float(value));
	}
	
	public Transformation videoSamplingSeconds(double value) {
		return videoSamplingSeconds(new Double(value));
	}
	
	public Transformation zoom(String value) {
		return param("zoom", value);
	}
	
	public Transformation zoom(float value) {
		return param("zoom", new Float(value));
	}
	
	public Transformation zoom(double value) {
		return param("zoom", new Double(value));
	}

	public Transformation responsiveWidth(boolean value) {
		return param("responsive_width", value);
	}

	public boolean isResponsive() {
		return this.isResponsive;
	}

	public boolean isHiDPI() {
		return this.hiDPI;
	}

	// Warning: options will destructively updated!
	public Transformation params(Map transformation) {
		this.transformation = transformation;
		transformations.add(transformation);
		return this;
	}

	public Transformation chain() {
		return params(new HashMap());
	}
	
	public Transformation chainWith(Transformation transformation) {
		List<Map> transformations = dup(this.transformations);
		transformations.addAll(dup(transformation.transformations));
		return new Transformation(transformations);
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
		return StringUtils.join(components, "/");
	}

	public String generate(Map options) {
		boolean isResponsive = ObjectUtils.asBoolean(options.get("responsive_width"), defaultIsResponsive);

		String size = (String) options.get("size");
		if (size != null) {
			String[] size_components = size.split("x");
			options.put("width", size_components[0]);
			options.put("height", size_components[1]);
		}
		String width = this.htmlWidth = ObjectUtils.asString(options.get("width"));
		String height = this.htmlHeight = ObjectUtils.asString(options.get("height"));
		boolean hasLayer = StringUtils.isNotBlank((String) options.get("overlay"))
				|| StringUtils.isNotBlank((String) options.get("underlay"));

		String crop = (String) options.get("crop");
		String angle = StringUtils.join(ObjectUtils.asArray(options.get("angle")), ".");

		boolean noHtmlSizes = hasLayer || StringUtils.isNotBlank(angle) || "fit".equals(crop) || "limit".equals(crop);
		if (width != null && (width.equals("auto") || Float.parseFloat(width) < 1 || noHtmlSizes || isResponsive)) {
			this.htmlWidth = null;
		}
		if (height != null && (Float.parseFloat(height) < 1 || noHtmlSizes || isResponsive)) {
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

		List transformations = ObjectUtils.asArray(options.get("transformation"));
		boolean allNamed = true;		
		for (Object baseTransformation : transformations) {			
			if (baseTransformation instanceof Map) {
				allNamed = false;
				break;
			}
		}
		String namedTransformation = null; 
		if (allNamed) {
			namedTransformation = StringUtils.join(transformations,".");
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


		String flags = StringUtils.join(ObjectUtils.asArray(options.get("flags")), ".");

		String duration = normRangeValue(options.get("duration"));
		String startOffset = normRangeValue(options.get("start_offset"));
		String endOffset = normRangeValue(options.get("end_offset"));
		String[] offset = splitRange(options.get("offset"));
		if (offset != null) {
			startOffset = normRangeValue(offset[0]);
			endOffset = normRangeValue(offset[1]);  
		}

		String videoCodec = processVideoCodecParam(options.get("video_codec"));
		String dpr = ObjectUtils.asString(options.get("dpr"), null == defaultDPR ? null : defaultDPR.toString());

		SortedMap<String, String> params = new TreeMap<String, String>();
		params.put("a", angle);
		params.put("b", background);
		params.put("c", crop);
		params.put("co", color);
		params.put("dpr", dpr);
		params.put("du", duration);
		params.put("eo", endOffset);
		params.put("fl", flags);
		params.put("h", height);
		params.put("so", startOffset);
		params.put("t", namedTransformation);
		params.put("vc", videoCodec);
		params.put("w", width);
		
		String[] simple_params = new String[] {
				"ac", "audio_codec",
				"af", "audio_frequency",
				"bo", "border",
				"br", "bit_rate",
				"cs", "color_space", 
				"d",  "default_image", 
				"dl", "delay", 
				"dn", "density", 
				"e",  "effect", 
				"f",  "fetch_format",
				"g",  "gravity", 
				"l",  "overlay", 
				"o",  "opacity",
				"p",  "prefix", 
				"pg", "page", 
				"q",  "quality", 
				"r",  "radius", 
				"u",  "underlay",
				"vs", "video_sampling",
				"x",  "x", 
				"y",  "y",
				"z",  "zoom" };
				 
		for (int i = 0; i < simple_params.length; i += 2) {
			params.put(simple_params[i], ObjectUtils.asString(options.get(simple_params[i + 1])));
		}
		List<String> components = new ArrayList<String>();
		for (Map.Entry<String, String> param : params.entrySet()) {
			if (StringUtils.isNotBlank(param.getValue())) {
				components.add(param.getKey() + "_" + param.getValue());
			}
		}
		String raw_transformation = (String) options.get("raw_transformation");
		if (raw_transformation != null) {
			components.add(raw_transformation);
		}
		if (!components.isEmpty()) {
			transformations.add(StringUtils.join(components, ","));
		}

		if (isResponsive) {
			transformations.add(generate(getResponsiveWidthTransformation()));
		}

		if ("auto".equals(width) || isResponsive) {
			this.isResponsive = true;
		}

		if ("auto".equals(dpr)) {
			this.hiDPI = true;
		}

		return StringUtils.join(transformations, "/");
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

	public static void setResponsiveWidthTransformation(Map transformation) {
		responsiveWidthTransformation = transformation;
	}

	private static Map getResponsiveWidthTransformation() {
		Map result = new HashMap();
		if (null == responsiveWidthTransformation) {
			result.putAll(DEFAULT_RESPONSIVE_WIDTH_TRANSFORMATION);
		} else {
			result.putAll(responsiveWidthTransformation);
		}
		return result;
	}

	public static void setDefaultIsResponsive(boolean isResponsive) {
		defaultIsResponsive = isResponsive;
	}

	public static void setDefaultDPR(Object dpr) {
		defaultDPR = dpr;
	}
	
	private static String[] splitRange(Object range) {
		if (range instanceof String[] && ((String[]) range).length >= 2) {
			String[] stringArrayRange = ((String[]) range);
			return new String[]{stringArrayRange[0], stringArrayRange[1]};
		} else if (range instanceof Number[] && ((Number[]) range).length >= 2) {
			Number[] numberArrayRange = ((Number[]) range);
			return new String[]{numberArrayRange[0].toString(), numberArrayRange[1].toString()};
		} else if (range instanceof String && RANGE_RE.matcher((String) range).matches()) {
			return ((String) range).split("\\.\\.", 2);
		} else {
			return null;
		}
	}

	private static String normRangeValue(Object objectValue) {
		if (objectValue == null) return null;
		String value = objectValue.toString();
		if (StringUtils.isEmpty(value)) return null;

		Matcher matcher = RANGE_VALUE_RE.matcher(value);

		if (!matcher.matches()) {
			return null;
		}

		String modifier = "";
		if (matcher.groupCount() == 2 && !StringUtils.isEmpty(matcher.group(2))) {
			modifier = "p";
		}
		return matcher.group(1) + modifier;
	}

	private static String processVideoCodecParam(Object param) {
		StringBuilder outParam = new StringBuilder();
		if (param instanceof String) {
			outParam.append(param);
		} if (param instanceof Map<?, ?>) {
			Map<String, String> paramMap = ( Map<String, String> ) param;
			outParam.append(paramMap.get("codec"));
			if (paramMap.containsKey("profile")) {
				outParam.append(":").append(paramMap.get("profile"));
				if (paramMap.containsKey("level")) {
					outParam.append(":").append(paramMap.get("level"));
				}
			}
		}
		return outParam.toString();
	}

}
