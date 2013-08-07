package com.cloudinary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Uploader {
	private final Cloudinary cloudinary;

	public Uploader(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}
	static final String[] BOOLEAN_UPLOAD_OPTIONS = new String[] {"backup", "exif", "faces", "colors", "image_metadata", "use_filename", "eager_async", "invalidate"};

	public Map<String, Object> buildUploadParams(Map options) {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transformation = options.get("transformation");
		if (transformation != null) {
			if (transformation instanceof Transformation) {
				transformation = ((Transformation) transformation).generate();
			}
			params.put("transformation", transformation.toString());
		}
		params.put("public_id", (String) options.get("public_id"));
		params.put("callback", (String) options.get("callback"));
		params.put("format", (String) options.get("format"));
		params.put("type", (String) options.get("type"));
		for (String attr : BOOLEAN_UPLOAD_OPTIONS) {
			Boolean value = Cloudinary.asBoolean(options.get(attr), null);
			if (value != null)
				options.put(attr, value.toString());			
		}
		params.put("eager", buildEager((List<Transformation>) options.get("eager")));
		params.put("headers", buildCustomHeaders(options.get("headers")));
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("eager_notification_url", (String) options.get("eager_notification_url"));
		params.put("proxy", (String) options.get("proxy"));
		params.put("folder", (String) options.get("folder"));
		params.put("tags", StringUtils.join(Cloudinary.asArray(options.get("tags")), ","));
		return params;
	}

	public Map upload(Object file, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = buildUploadParams(options);
		return callApi("upload", params, options, file);
	}

	public Map destroy(String publicId, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", (String) options.get("type"));
		params.put("public_id", publicId);
		params.put("invalidate", Cloudinary.asBoolean(options.get("invalidate"), false).toString());			
		return callApi("destroy", params, options, null);
	}

	public Map rename(String fromPublicId, String toPublicId, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", (String) options.get("type"));
		params.put("overwrite", Cloudinary.asBoolean(options.get("overwrite"), false).toString());			
		params.put("from_public_id", fromPublicId);
		params.put("to_public_id", toPublicId);
		return callApi("rename", params, options, null);
	}

	public Map explicit(String publicId, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("public_id", publicId);
		params.put("callback", (String) options.get("callback"));
		params.put("type", (String) options.get("type"));
		params.put("eager", buildEager((List<Transformation>) options.get("eager")));
		params.put("headers", buildCustomHeaders(options.get("headers")));
		params.put("tags", StringUtils.join(Cloudinary.asArray(options.get("tags")), ","));
		return callApi("explicit", params, options, null);
	}

	public Map generate_sprite(String tag, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transParam = options.get("transformation");
		Transformation transformation = null;
		if (transParam instanceof Transformation) {
			transformation = new Transformation((Transformation) transParam);
		} else if (transParam instanceof String) {
			transformation = new Transformation().rawTransformation((String) transParam);
		} else {
			transformation = new Transformation();
		}
		String format = (String) options.get("format");
		if (format != null) {
			transformation.fetchFormat(format);
		}
		params.put("transformation", transformation.generate());
		params.put("tag", tag);
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("async", Cloudinary.asBoolean(options.get("async"), false).toString());			
		return callApi("sprite", params, options, null);
	}

	public Map multi(String tag, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transformation = options.get("transformation");
		if (transformation != null) {
			if (transformation instanceof Transformation) {
				transformation = ((Transformation) transformation).generate();
			}
			params.put("transformation", transformation.toString());
		}
		params.put("tag", tag);
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("format", (String) options.get("format"));
		params.put("async", Cloudinary.asBoolean(options.get("async"), false).toString());			
		return callApi("multi", params, options, null);
	}

	public Map explode(String public_id, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transformation = options.get("transformation");
		if (transformation != null) {
			if (transformation instanceof Transformation) {
				transformation = ((Transformation) transformation).generate();
			}
			params.put("transformation", transformation.toString());
		}
		params.put("public_id", public_id);
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("format", (String) options.get("format"));
		return callApi("explode", params, options, null);
	}
	
	// options may include 'exclusive' (boolean) which causes clearing this tag
	// from all other resources
	public Map addTag(String tag, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		boolean exclusive = Cloudinary.asBoolean(options.get("exclusive"), false);
		String command = exclusive ? "set_exclusive" : "add";
		return callTagsApi(tag, command, publicIds, options);
	}

	public Map removeTag(String tag, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		return callTagsApi(tag, "remove", publicIds, options);
	}

	public Map replaceTag(String tag, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		return callTagsApi(tag, "replace", publicIds, options);
	}

	public Map callTagsApi(String tag, String command, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tag", tag);
		params.put("command", command);
		params.put("type", (String) options.get("type"));
		params.put("public_ids", Arrays.asList(publicIds));
		return callApi("tags", params, options, null);
	}

	private final static String[] TEXT_PARAMS = { "public_id", "font_family", "font_size", "font_color", "text_align", "font_weight",
			"font_style", "background", "opacity", "text_decoration" };

	public Map text(String text, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("text", text);
		for (String param : TEXT_PARAMS) {
			params.put(param, Cloudinary.asString(options.get(param)));
		}
		return callApi("text", params, options, null);
	}
	
	public void signRequestParams(Map<String, Object> params, Map options) {
        params.put("timestamp", new Long(System.currentTimeMillis() / 1000L).toString());
        cloudinary.signRequest(params, options);
    }


	public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		boolean returnError = Cloudinary.asBoolean(options.get("return_error"), false);
		signRequestParams(params, options);

		String apiUrl = cloudinary.cloudinaryApiUrl(action, options);

		HttpClient client = new DefaultHttpClient();

		HttpPost postMethod = new HttpPost(apiUrl);
		MultipartEntity multipart = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		// Remove blank parameters
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (param.getValue() instanceof String) { 
				String value = (String) param.getValue();
				if (StringUtils.isNotBlank(value)) {
					multipart.addPart(param.getKey(), new StringBody(value));
				}
			} else if (param.getValue() instanceof Collection) {
				for (Object value : (Collection) param.getValue()) {
					multipart.addPart(param.getKey()+"[]", new StringBody(Cloudinary.asString(value)));					
				}
			}
		}

		if (file instanceof String && !((String) file).matches("https?:.*|s3:.*|data:[^;]*;base64,([a-zA-Z0-9/+\n=]+)")) {
			file = new File((String) file);
		}
		if (file instanceof File) {
			multipart.addPart("file", new FileBody((File) file));
		} else if (file instanceof String) {
			multipart.addPart("file", new StringBody((String) file));
        } else if (file instanceof byte[]) {
            multipart.addPart("file", new ByteArrayBody((byte[]) file, "file"));
		} else if (file == null) {
		    // no-problem
		} else {
		    throw new IOException("Uprecognized file parameter " + file);
		}
		postMethod.setEntity(multipart);

		HttpResponse response = client.execute(postMethod);
		int code = response.getStatusLine().getStatusCode();
		InputStream responseStream = response.getEntity().getContent();
		String responseData = readFully(responseStream);

		if (code != 200 && code != 400 && code != 500) {
			throw new RuntimeException("Server returned unexpected status code - " + code + " - " + responseData);
		}

		Map result;
		try {
			result = (Map) JSONValue.parseWithException(responseData);
		} catch (ParseException e) {
			throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
		}
		if (result.containsKey("error")) {
			Map error = (Map) result.get("error");
			if (returnError) {
				error.put("http_code", code);
			} else {
				throw new RuntimeException((String) error.get("message"));
			}
		}
		return result;
	}

	public String uploadTagParams(Map options) {
	    if (options == null) options = new HashMap();
	    if (options.get("resource_type") == null) { 
	        options = new HashMap(options);
	        options.put("resource_type", "auto");
	    }
	    
	    String callback = Cloudinary.asString(options.get("callback"), this.cloudinary.getStringConfig("callback"));
	    if (callback == null) {
	        throw new IllegalArgumentException("Must supply callback");
	    }
	    options.put("callback", callback);
	    
	    Map<String, Object> params = this.buildUploadParams(options);
	    signRequestParams(params, options);
	    
	    // Remove blank parameters
	    for (Iterator<Object> iterator = params.values().iterator(); iterator.hasNext(); ) {
	        String value = (String) iterator.next();
	        if (StringUtils.isBlank(value)) {
	            iterator.remove();
	        }
	    }
	    
	    return JSONObject.toJSONString(params);
	}
	
	public String getUploadUrl(Map options) {
	    if (options == null) options = new HashMap();
	    return this.cloudinary.cloudinaryApiUrl("upload", options);
	}
	
	public String imageUploadTag(String field, Map options, Map<String, Object> htmlOptions) {
        if (htmlOptions == null) htmlOptions = Cloudinary.emptyMap();
		
        String tagParams = StringEscapeUtils.escapeHtml(uploadTagParams(options));
        
		String cloudinaryUploadUrl = getUploadUrl(options);
        
		StringBuilder builder = new StringBuilder();
		builder.append("<input type='file' name='file' data-url='").append(cloudinaryUploadUrl).
				append("' data-form-data='").append(tagParams).
				append("' data-cloudinary-field='").append(field).
				append("' class='cloudinary-fileupload");
		if (htmlOptions.containsKey("class")) {
			builder.append(" ").append(htmlOptions.get("class"));
		}
		for (Map.Entry<String, Object> htmlOption : htmlOptions.entrySet()) {
			if (htmlOption.getKey().equals("class")) continue;
			builder.append("' ").
			        append(htmlOption.getKey()).
			        append("='").
			        append(StringEscapeUtils.escapeHtml(Cloudinary.asString(htmlOption.getValue())));
		}
		builder.append("'/>");
		return builder.toString();
    }
	
	protected static String readFully(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = in.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return new String(baos.toByteArray());
	}

	protected String buildEager(List<? extends Transformation> transformations) {
		if (transformations == null) {
			return null;
		}
		List<String> eager = new ArrayList<String>();
		for (Transformation transformation : transformations) {
			List<String> single_eager = new ArrayList<String>();
			String transformationString = transformation.generate();
			if (StringUtils.isNotBlank(transformationString)) {
				single_eager.add(transformationString);
			}
			if (transformation instanceof EagerTransformation) {
				EagerTransformation eagerTransformation = (EagerTransformation) transformation;
				if (StringUtils.isNotBlank(eagerTransformation.getFormat())) {
					single_eager.add(eagerTransformation.getFormat());
				}
			}
			eager.add(StringUtils.join(single_eager, "/"));
		}
		return StringUtils.join(eager, "|");
	}

	protected String buildCustomHeaders(Object headers) {
		if (headers == null) {
			return null;
		} else if (headers instanceof String) {
			return (String) headers;
		} else if (headers instanceof Object[]) {
			return StringUtils.join((Object[]) headers, "\n") + "\n";
		} else {
			Map<String, String> headersMap = (Map<String, String>) headers;
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<String, String> header : headersMap.entrySet()) {
				builder.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
			}
			return builder.toString();
		}
	}
}
