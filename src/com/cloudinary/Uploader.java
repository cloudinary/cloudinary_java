package com.cloudinary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Uploader {
	private final Cloudinary cloudinary;

	public Uploader(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}

	public Map<String, String> build_upload_params(Map options) {
		Map<String, String> params = new HashMap<String, String>();
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
		Boolean backup = Cloudinary.as_bool(options.get("backup"), null);
		if (backup != null)
			options.put("backup", backup.toString());
		params.put("eager", build_eager((List<Transformation>) options.get("eager")));
		params.put("headers", build_custom_headers(options.get("headers")));
		params.put("tags", StringUtils.join(Cloudinary.as_array(options.get("tags")), ","));
		return params;
	}

	public Map upload(Object file, Map options) throws IOException {
		Map<String, String> params = build_upload_params(options);
		return call_api("upload", params, options, file);
	}

	public Map destroy(String public_id, Map options) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", (String) options.get("type"));
		params.put("public_id", public_id);
		return call_api("destroy", params, options, null);
	}

	public Map explicit(String public_id, Map options) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("public_id", public_id);
		params.put("callback", (String) options.get("callback"));
		params.put("type", (String) options.get("type"));
		params.put("eager", build_eager((List<Transformation>) options.get("eager")));
		params.put("headers", build_custom_headers(options.get("headers")));
		params.put("tags", StringUtils.join(Cloudinary.as_array(options.get("tags")), ","));
		return call_api("explicit", params, options, null);
	}

	// options may include 'exclusive' (boolean) which causes clearing this tag
	// from all other resources
	public Map add_tag(String tag, String[] public_ids, Map options) throws IOException {
		boolean exclusive = Cloudinary.as_bool(options.get("exclusive"), false);
		String command = exclusive ? "set_exclusive" : "add";
		return call_tags_api(tag, command, public_ids, options);
	}

	public Map remove_tag(String tag, String[] public_ids, Map options) throws IOException {
		return call_tags_api(tag, "remove", public_ids, options);
	}

	public Map replace_tag(String tag, String[] public_ids, Map options) throws IOException {
		return call_tags_api(tag, "replace", public_ids, options);
	}

	public Map call_tags_api(String tag, String command, String[] public_ids, Map options) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("tag", tag);
		params.put("command", command);
		params.put("type", (String) options.get("type"));
		params.put("public_ids", StringUtils.join(public_ids, ","));
		return call_api("tags", params, options, null);
	}

	private final static String[] TEXT_PARAMS = { "public_id", "font_family", "font_size", "font_color", "text_align", "font_weight",
			"font_style", "background", "opacity", "text_decoration" };

	public Map text(String text, Map options) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("text", text);
		for (String param : TEXT_PARAMS) {
			params.put(param, Cloudinary.as_string(options.get(param)));
		}
		return call_api("text", params, options, null);
	}

	public Map call_api(String action, Map<String, String> params, Map options, Object file) throws IOException {
		boolean return_error = Cloudinary.as_bool(options.get("return_error"), false);
		String api_key = Cloudinary.as_string(options.get("api_key"), this.cloudinary.getStringConfig("api_key"));
		if (api_key == null)
			throw new IllegalArgumentException("Must supply api_key");
		String api_secret = Cloudinary.as_string(options.get("api_secret"), this.cloudinary.getStringConfig("api_secret"));
		if (api_secret == null)
			throw new IllegalArgumentException("Must supply api_secret");
		params.put("timestamp", new Long(System.currentTimeMillis() / 1000L).toString());
		params.put("signature", this.cloudinary.api_sign_request(params, api_secret));
		params.put("api_key", api_key);

		String api_url = cloudinary.cloudinary_api_url(action, options);

		HttpClient client = new DefaultHttpClient();

		HttpPost postMethod = new HttpPost(api_url);
		MultipartEntity multipart = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		// Remove blank parameters
		for (Map.Entry<String, String> param : params.entrySet()) {
			if (StringUtils.isNotBlank(param.getValue())) {
				multipart.addPart(param.getKey(), new StringBody(param.getValue()));
			}
		}

		if (file instanceof String && !((String) file).matches("^https?:")) {
			file = new File((String) file);
		}
		if (file instanceof File) {
			multipart.addPart("file", new FileBody((File) file));
		} else if (file instanceof String) {
			multipart.addPart("file", new StringBody((String) file));
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
			if (return_error) {
				error.put("http_code", code);
			} else {
				throw new RuntimeException((String) error.get("message"));
			}
		}
		return result;
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

	protected String build_eager(List<? extends Transformation> transformations) {
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

	protected String build_custom_headers(Object headers) {
		if (headers == null) {
			return null;
		} else if (headers instanceof String) {
			return (String) headers;
		} else if (headers instanceof Object[]) {
			return StringUtils.join((Object[]) headers, "\n") + "\n";
		} else {
			Map<String, String> headers_map = (Map<String, String>) headers;
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<String, String> header : headers_map.entrySet()) {
				builder.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
			}
			return builder.toString();
		}
	}
}
