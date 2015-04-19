package com.cloudinary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudinary.json.JSONObject;

import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Uploader {
	public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException{
		return strategy.callApi(action,params,options,file);
	}

	private Cloudinary cloudinary;
	private AbstractUploaderStrategy strategy;

	public Uploader(Cloudinary cloudinary,AbstractUploaderStrategy strategy) {
		this.cloudinary = cloudinary;
		this.strategy = strategy;
		strategy.init(this);
	}
	
	public Cloudinary cloudinary(){
		return this.cloudinary;
	}

	public Map<String, Object> buildUploadParams(Map options) {
		return Util.buildUploadParams(options);
	}

	public Map unsignedUpload(Object file, String uploadPreset, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		HashMap nextOptions = new HashMap(options);
		nextOptions.put("unsigned", true);
		nextOptions.put("upload_preset", uploadPreset);
		return upload(file, nextOptions);
	}

	public Map upload(Object file, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		Map<String, Object> params = buildUploadParams(options);
		return callApi("upload", params, options, file);
	}
	
	public Map uploadLargeRaw(Object file, Map options) throws IOException {
		return uploadLargeRaw(file, options, 20000000);
	}

	public Map uploadLargeRaw(Object file, Map options, int bufferSize) throws IOException {
		Map sentOptions = new HashMap();
		sentOptions.putAll(options);
		sentOptions.put("resource_type", "raw");
		return uploadLarge(file, sentOptions, bufferSize);
	}
	
	public Map uploadLarge(Object file, Map options) throws IOException {
		int bufferSize = ObjectUtils.asInteger(options.get("chunk_size"), 20000000);
		return uploadLarge(file, options, bufferSize);
	}
	
	@SuppressWarnings("resource")
	public Map uploadLarge(Object file, Map options, int bufferSize) throws IOException {
		InputStream input;
		long length = -1;
		if (file instanceof InputStream) {
			input = (InputStream) file;
		} else if (file instanceof File) {
			length = ((File) file).length();
			input = new FileInputStream((File) file);
		} else if (file instanceof byte[]) {
			length = ( (byte[]) file ).length;
			input = new ByteArrayInputStream((byte[]) file);
		} else {
			File f = new File(file.toString());
			length = f.length();
			input = new FileInputStream(f);
		}
		try {
			Map result = uploadLargeParts(input, options, bufferSize, length);
			return result;
		} finally {
			input.close();
		}
	}

	private Map uploadLargeParts(InputStream input, Map options, int bufferSize, long length) throws IOException {
		Map params = buildUploadParams(options);
		Map nextParams = new HashMap();
		nextParams.putAll(params);
		Map sentParams = new HashMap();

		Map sentOptions = new HashMap();
		sentOptions.putAll(options);

		byte[] buffer = new byte[bufferSize];
		byte[] nibbleBuffer = new byte[1];
		int bytesRead = 0;
		int currentBufferSize = 0;
		int partNumber = 0;
		long totalBytes = 0;
		Map response = null;
		while (true) {
			bytesRead = input.read(buffer, currentBufferSize, bufferSize - currentBufferSize);
			boolean atEnd = bytesRead == -1;
			boolean fullBuffer = !atEnd && (bytesRead + currentBufferSize) == bufferSize;
			if (!atEnd) currentBufferSize += bytesRead;

			if (atEnd || fullBuffer) {
				totalBytes += currentBufferSize;
				sentParams.clear();
				sentParams.putAll(nextParams);
				int currentLoc = bufferSize * partNumber;
				if (!atEnd) {
					//verify not on end - try read another byte
					bytesRead = input.read(nibbleBuffer, 0, 1);
					atEnd = bytesRead == -1;
				}
				if (atEnd) {
					if (length == -1) length = totalBytes;
					byte[] finalBuffer = new byte[currentBufferSize];
					System.arraycopy(buffer, 0, finalBuffer, 0, currentBufferSize);
					buffer = finalBuffer;
				}
				String range = String.format("bytes %d-%d/%d", currentLoc, currentLoc + currentBufferSize - 1, length);  
				sentOptions.put("content_range", range);
				response = callApi("upload", sentParams, sentOptions, buffer);
				nextParams.put("public_id", response.get("public_id"));
				nextParams.put("upload_id", response.get("upload_id"));
				if (atEnd) break;
				buffer[0] = nibbleBuffer[0];
				currentBufferSize = 1;
				partNumber++;
			}
		}
		return response;
	}

	public Map destroy(String publicId, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", (String) options.get("type"));
		params.put("public_id", publicId);
		params.put("invalidate", ObjectUtils.asBoolean(options.get("invalidate"), false).toString());
		return callApi("destroy", params, options, null);
	}

	public Map rename(String fromPublicId, String toPublicId, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", (String) options.get("type"));
		params.put("overwrite", ObjectUtils.asBoolean(options.get("overwrite"), false).toString());
		params.put("from_public_id", fromPublicId);
		params.put("to_public_id", toPublicId);
		return callApi("rename", params, options, null);
	}

	public Map explicit(String publicId, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("public_id", publicId);
		params.put("callback", (String) options.get("callback"));
		params.put("type", (String) options.get("type"));
		params.put("eager", Util.buildEager((List<Transformation>) options.get("eager")));
		params.put("eager_async", ObjectUtils.asBoolean(options.get("eager_async"), false).toString());
		params.put("eager_notification_url", (String) options.get("eager_notification_url"));
		params.put("headers", Util.buildCustomHeaders(options.get("headers")));
		params.put("tags", StringUtils.join(ObjectUtils.asArray(options.get("tags")), ","));
		if (options.get("face_coordinates") != null) {
			params.put("face_coordinates", Coordinates.parseCoordinates(options.get("face_coordinates")).toString());
		}
		if (options.get("custom_coordinates") != null) {
			params.put("custom_coordinates", Coordinates.parseCoordinates(options.get("custom_coordinates")).toString());
		}
		if (options.get("context") != null) {
			params.put("context", ObjectUtils.encodeMap(options.get("context")));
		}
		return callApi("explicit", params, options, null);
	}

	public Map generate_sprite(String tag, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
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
		params.put("async", ObjectUtils.asBoolean(options.get("async"), false).toString());
		return callApi("sprite", params, options, null);
	}

	public Map multi(String tag, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
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
		params.put("async", ObjectUtils.asBoolean(options.get("async"), false).toString());
		return callApi("multi", params, options, null);
	}

	public Map explode(String public_id, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
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
		if (options == null)
			options = ObjectUtils.emptyMap();
		boolean exclusive = ObjectUtils.asBoolean(options.get("exclusive"), false);
		String command = exclusive ? "set_exclusive" : "add";
		return callTagsApi(tag, command, publicIds, options);
	}

	public Map removeTag(String tag, String[] publicIds, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		return callTagsApi(tag, "remove", publicIds, options);
	}

	public Map replaceTag(String tag, String[] publicIds, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		return callTagsApi(tag, "replace", publicIds, options);
	}

	public Map callTagsApi(String tag, String command, String[] publicIds, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tag", tag);
		params.put("command", command);
		params.put("type", (String) options.get("type"));
		params.put("public_ids", Arrays.asList(publicIds));
		return callApi("tags", params, options, null);
	}

	private final static String[] TEXT_PARAMS = { "public_id", "font_family", "font_size", "font_color", "text_align", "font_weight", "font_style",
			"background", "opacity", "text_decoration" };

	public Map text(String text, Map options) throws IOException {
		if (options == null)
			options = ObjectUtils.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("text", text);
		for (String param : TEXT_PARAMS) {
			params.put(param, ObjectUtils.asString(options.get(param)));
		}
		return callApi("text", params, options, null);
	}

	public void signRequestParams(Map<String, Object> params, Map options) {
		params.put("timestamp", new Long(System.currentTimeMillis() / 1000L).toString());
		cloudinary.signRequest(params, options);
	}

	public String uploadTagParams(Map options) {
		if (options == null)
			options = new HashMap();
		if (options.get("resource_type") == null) {
			options = new HashMap(options);
			options.put("resource_type", "auto");
		}

		String callback = ObjectUtils.asString(options.get("callback"), this.cloudinary.config.callback);
		if (callback == null) {
			throw new IllegalArgumentException("Must supply callback");
		}
		options.put("callback", callback);

		Map<String, Object> params = this.buildUploadParams(options);
		if (options.get("unsigned") == null || Boolean.FALSE.equals(options.get("unsigned"))) {
			signRequestParams(params, options);
		} else {
			Util.clearEmpty(params);
		}

		return JSONObject.valueToString(params);
	}

	public String getUploadUrl(Map options) {
		if (options == null)
			options = new HashMap();
		return this.cloudinary.cloudinaryApiUrl("upload", options);
	}

	public String unsignedImageUploadTag(String field, String uploadPreset, Map options, Map<String, Object> htmlOptions) {
		Map nextOptions = new HashMap(options);
		nextOptions.put("upload_preset", uploadPreset);
		nextOptions.put("unsigned", true);
		return imageUploadTag(field, nextOptions, htmlOptions);
	}

	public String imageUploadTag(String field, Map options, Map<String, Object> htmlOptions) {
		if (htmlOptions == null)
			htmlOptions = ObjectUtils.emptyMap();

		String tagParams = StringUtils.escapeHtml(uploadTagParams(options));

		String cloudinaryUploadUrl = getUploadUrl(options);

		StringBuilder builder = new StringBuilder();
		builder.append("<input type='file' name='file' data-url='").append(cloudinaryUploadUrl).append("' data-form-data='").append(tagParams)
				.append("' data-cloudinary-field='").append(field).append("'");
		if (options.containsKey("chunk_size")) 
			builder.append(" data-max-chunk-size='").append(options.get("chunk_size")).append("'");
		builder.append(" class='cloudinary-fileupload");
		
		if (htmlOptions.containsKey("class")) {
			builder.append(" ").append(htmlOptions.get("class"));
		}
		for (Map.Entry<String, Object> htmlOption : htmlOptions.entrySet()) {
			if (htmlOption.getKey().equals("class"))
				continue;
			builder.append("' ").append(htmlOption.getKey()).append("='").append(StringUtils.escapeHtml(ObjectUtils.asString(htmlOption.getValue())));
		}
		builder.append("'/>");
		return builder.toString();
	}

}
