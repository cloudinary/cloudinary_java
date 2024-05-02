package com.cloudinary;

import com.cloudinary.strategies.AbstractUploaderStrategy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import org.cloudinary.json.JSONObject;

import java.io.*;
import java.util.*;

import static com.cloudinary.Util.buildGenerateSpriteParams;
import static com.cloudinary.Util.buildMultiParams;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Uploader {

    public static final int BUFFER_SIZE = 20000000;

    private final class Command {
        final static String add = "add";
        final static String remove = "remove";
        final static String replace = "replace";
        final static String removeAll = "remove_all";

        private Command() {
        }
    }

    public Map callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
        return strategy.callApi(action, params, options, file, null);
    }

    public Map callApi(String action, Map<String, Object> params, Map options, Object file, ProgressCallback progressCallback) throws IOException {
        return strategy.callApi(action, params, options, file, progressCallback);
    }

    private Cloudinary cloudinary;
    private AbstractUploaderStrategy strategy;

    public Uploader(Cloudinary cloudinary, AbstractUploaderStrategy strategy) {
        this.cloudinary = cloudinary;
        this.strategy = strategy;
        strategy.init(this);
    }

    public Cloudinary cloudinary() {
        return this.cloudinary;
    }

    public Map<String, Object> buildUploadParams(Map options) {
        return Util.buildUploadParams(options);
    }

    public Map unsignedUpload(Object file, String uploadPreset, Map options) throws IOException {
        return unsignedUpload(file, uploadPreset, options, null);
    }

    public Map unsignedUpload(Object file, String uploadPreset, Map options, ProgressCallback progressCallback) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        HashMap nextOptions = new HashMap(options);
        nextOptions.put("unsigned", true);
        nextOptions.put("upload_preset", uploadPreset);
        return upload(file, nextOptions, progressCallback);
    }

    public Map upload(Object file, Map options) throws IOException {
        return upload(file, options, null);
    }

    public Map upload(Object file, Map options, final ProgressCallback progressCallback) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        Map<String, Object> params = buildUploadParams(options);

        return callApi("upload", params, options, file, progressCallback);
    }

    public Map uploadLargeRaw(Object file, Map options) throws IOException {
        return uploadLargeRaw(file, options, BUFFER_SIZE, null);
    }

    public Map uploadLargeRaw(Object file, Map options, ProgressCallback progressCallback) throws IOException {
        return uploadLargeRaw(file, options, BUFFER_SIZE, progressCallback);
    }

    public Map uploadLargeRaw(Object file, Map options, int bufferSize) throws IOException {
        return uploadLargeRaw(file, options, bufferSize, null);
    }

    public Map uploadLargeRaw(Object file, Map options, int bufferSize, ProgressCallback callback) throws IOException {
        Map sentOptions = new HashMap();
        sentOptions.putAll(options);
        sentOptions.put("resource_type", "raw");
        return uploadLarge(file, sentOptions, bufferSize, callback);
    }

    public Map uploadLarge(Object file, Map options) throws IOException {
        return uploadLarge(file, options, null);
    }

    public Map uploadLarge(Object file, Map options, ProgressCallback progressCallback) throws IOException {
        int bufferSize = ObjectUtils.asInteger(options.get("chunk_size"), BUFFER_SIZE);
        return uploadLarge(file, options, bufferSize, progressCallback);
    }

    @SuppressWarnings("resource")
    public Map uploadLarge(Object file, Map options, int bufferSize) throws IOException {
        return uploadLarge(file, options, bufferSize, null);
    }

    public Map uploadLarge(Object file, Map options, int bufferSize, ProgressCallback progressCallback) throws IOException {
        return uploadLarge(file, options, bufferSize, 0, null, progressCallback);
    }

    public Map uploadLarge(Object file, Map options, int bufferSize, long offset, String uniqueUploadId, ProgressCallback progressCallback) throws IOException {
        InputStream input;
        long length = -1;
        boolean remote = false;
        String filename = null;
        if (file instanceof InputStream) {
            input = (InputStream) file;
        } else if (file instanceof File) {
            length = ((File) file).length();
            filename = ((File) file).getName();
            input = new FileInputStream((File) file);
        } else if (file instanceof byte[]) {
            length = ((byte[]) file).length;
            input = new ByteArrayInputStream((byte[]) file);
        } else {
            if (StringUtils.isRemoteUrl(file.toString())){
                remote = true;
                input = null;
            } else {
                File f = new File(file.toString());
                length = f.length();
                filename = f.getName();
                input = new FileInputStream(f);
            }
        }
        try {
            final Map result;
            if (remote) {
                result = upload(file, options);
            } else {
                if (!options.containsKey("filename") && StringUtils.isNotBlank(filename)) {
                    options.put("filename", filename);
                }
                result = uploadLargeParts(input, options, bufferSize, length, offset, uniqueUploadId, progressCallback);
            }
            return result;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    private Map uploadLargeParts(InputStream input, Map options, int bufferSize, long length, long offset, String uniqueUploadId, final ProgressCallback progressCallback) throws IOException {
        Map params = buildUploadParams(options);

        Map sentOptions = new HashMap();
        sentOptions.putAll(options);
        Map extraHeaders = new HashMap();
        extraHeaders.put("X-Unique-Upload-Id", StringUtils.isBlank(uniqueUploadId) ? cloudinary().randomPublicId() : uniqueUploadId);
        sentOptions.put("extra_headers", extraHeaders);

        byte[] buffer = new byte[bufferSize];
        byte[] nibbleBuffer = new byte[1];
        int bytesRead = 0;
        int currentBufferSize = 0;
        int partNumber = 0;
        long totalBytes = offset;
        Map response = null;
        final long knownLengthBeforeUpload = length;
        long totalBytesUploaded = offset;
        input.skip(offset);
        while (true) {
            bytesRead = input.read(buffer, currentBufferSize, bufferSize - currentBufferSize);
            boolean atEnd = bytesRead == -1;
            boolean fullBuffer = !atEnd && (bytesRead + currentBufferSize) == bufferSize;
            if (!atEnd) currentBufferSize += bytesRead;

            if (atEnd || fullBuffer) {
                totalBytes += currentBufferSize;
                long currentLoc = offset + bufferSize * partNumber;
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
                String range = String.format(Locale.US, "bytes %d-%d/%d", currentLoc, currentLoc + currentBufferSize - 1, length);
                extraHeaders.put("Content-Range", range);
                Map sentParams = new HashMap();
                sentParams.putAll(params);

                // wrap the callback with another callback to account for multiple parts
                final long bytesUploadedSoFar = totalBytesUploaded;
                final ProgressCallback singlePartProgressCallback;
                if (progressCallback == null) {
                    singlePartProgressCallback = null;
                } else {
                    singlePartProgressCallback = new ProgressCallback() {

                        @Override
                        public void onProgress(long bytesUploaded, long totalBytes) {
                            progressCallback.onProgress(bytesUploadedSoFar + bytesUploaded, knownLengthBeforeUpload);
                        }
                    };
                }

                response = callApi("upload", sentParams, sentOptions, buffer, singlePartProgressCallback);

                if (atEnd) break;
                buffer[0] = nibbleBuffer[0];
                totalBytesUploaded += currentBufferSize;
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
        params.put("notification_url", (String) options.get("notification_url"));
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
        params.put("invalidate", ObjectUtils.asBoolean(options.get("invalidate"), false).toString());
        params.put("to_type", options.get("to_type"));
        params.put("context", ObjectUtils.asBoolean(options.get("context"), false).toString());
        params.put("metadata", ObjectUtils.asBoolean(options.get("metadata"), false).toString());
        params.put("notification_url", (String) options.get("notification_url"));
        return callApi("rename", params, options, null);
    }

    public Map explicit(String publicId, Map options) throws IOException {
        if (options == null) {
            options = ObjectUtils.emptyMap();
        }
        Map<String, Object> params = buildUploadParams(options);
        params.put("public_id", publicId);
        return callApi("explicit", params, options, null);
    }

    @Deprecated
    public Map generate_sprite(String tag, Map options) throws IOException {
        return generateSprite(tag, options);
    }

    public Map generateSprite(String tag, Map options) throws IOException {
        if (options == null)
            options = Collections.singletonMap("tag", tag);
        else
            options.put("tag", tag);

        return callApi("sprite", buildGenerateSpriteParams(options), options, null);
    }

    public Map generateSprite(String[] urls, Map options) throws IOException {
        if (options == null)
            options = Collections.singletonMap("urls", urls);
        else
            options.put("urls", urls);

        return callApi("sprite", buildGenerateSpriteParams(options), options, null);
    }

    public Map multi(String[] urls, Map options) throws IOException {
        if (options == null) {
            options = Collections.singletonMap("urls", urls);
        } else {
            options.put("urls", urls);
        }

        return multi(options);
    }

    public Map multi(String tag, Map options) throws IOException {
        if (options == null) {
            options = Collections.singletonMap("tag", tag);
        } else {
            options.put("tag", tag);
        }

        return multi(options);
    }

    private Map multi(Map options) throws IOException {
        return callApi("multi", buildMultiParams(options), options, null);
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

    /**
     * Add a tag to one or more assets in your cloud.
     * Tags are used to categorize and organize your images, and can also be used to apply group actions to images,
     * for example to delete images, create sprites, ZIP files, JSON lists, or animated GIFs.
     * Each image can be assigned one or more tags, which is a short name that you can dynamically use (no need to predefine tags).
     * @param tag - The tag to assign.
     * @param publicIds - An array of Public IDs of images uploaded to Cloudinary.
     * @param options - An object holding the available parameters for the request.
     *                options may include 'exclusive' (boolean) which causes clearing this tag from all other resources
     * @return A map with the public ids returned from the server
     * @throws IOException
     */
    public Map addTag(String tag, String[] publicIds, Map options) throws IOException {
        return addTag(new String[]{tag}, publicIds, options);
    }

    /**
     * Add a tag to one or more assets in your cloud.
     * Tags are used to categorize and organize your images, and can also be used to apply group actions to images,
     * for example to delete images, create sprites, ZIP files, JSON lists, or animated GIFs.
     * Each image can be assigned one or more tags, which is a short name that you can dynamically use (no need to predefine tags).
     * @param tag - An array of tags to assign.
     * @param publicIds - An array of Public IDs of images uploaded to Cloudinary.
     * @param options - An object holding the available parameters for the request.
     *                options may include 'exclusive' (boolean) which causes clearing this tag from all other resources
     * @return A map with the public ids returned from the server.
     * @throws IOException
     */
    public Map addTag(String[] tag, String[] publicIds, Map options) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        boolean exclusive = ObjectUtils.asBoolean(options.get("exclusive"), false);
        String command = exclusive ? "set_exclusive" : Command.add;
        return callTagsApi(tag, command, publicIds, options);
    }

    /**
     * Remove a tag to one or more assets in your cloud.
     * Tags are used to categorize and organize your images, and can also be used to apply group actions to images,
     * for example to delete images, create sprites, ZIP files, JSON lists, or animated GIFs.
     * Each image can be assigned one or more tags, which is a short name that you can dynamically use (no need to predefine tags).
     * @param tag - The tag to remove.
     * @param publicIds - An array of Public IDs of images uploaded to Cloudinary.
     * @param options - An object holding the available parameters for the request.
     *                options may include 'exclusive' (boolean) which causes clearing this tag from all other resources
     * @return - A map with the public ids returned from the server.
     * @throws IOException
     */
    public Map removeTag(String tag, String[] publicIds, Map options) throws IOException {
        return removeTag(new String[]{tag}, publicIds, options);
    }

    /**
     * Remove tags to one or more assets in your cloud.
     * Tags are used to categorize and organize your images, and can also be used to apply group actions to images,
     * for example to delete images, create sprites, ZIP files, JSON lists, or animated GIFs.
     * Each image can be assigned one or more tags, which is a short name that you can dynamically use (no need to predefine tags).
     * @param tag - The array of tags to remove.
     * @param publicIds - An array of Public IDs of images uploaded to Cloudinary.
     * @param options - An object holding the available parameters for the request.
     *                options may include 'exclusive' (boolean) which causes clearing this tag from all other resources
     * @return -      * @return - A map with the public ids returned from the server.
     * @throws IOException
     */
    public Map removeTag(String[] tag, String[] publicIds, Map options) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callTagsApi(tag, Command.remove, publicIds, options);
    }

    /**
     * Remove an array of tags to one or more assets in your cloud.
     * Tags are used to categorize and organize your images, and can also be used to apply group actions to images,
     * for example to delete images, create sprites, ZIP files, JSON lists, or animated GIFs.
     * Each image can be assigned one or more tags, which is a short name that you can dynamically use (no need to predefine tags).
     * @param publicIds - An array of Public IDs of images uploaded to Cloudinary.
     * @param options - An object holding the available parameters for the request.
     *                options may include 'exclusive' (boolean) which causes clearing this tag from all other resources
     * @return -      * @return - A map with the public ids returned from the server.
     * @throws IOException
     */
    public Map removeAllTags(String[] publicIds, Map options) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callTagsApi(null, Command.removeAll, publicIds, options);
    }

    /**
     * Replaces a tag to one or more assets in your cloud.
     * Tags are used to categorize and organize your images, and can also be used to apply group actions to images,
     * for example to delete images, create sprites, ZIP files, JSON lists, or animated GIFs.
     * Each image can be assigned one or more tags, which is a short name that you can dynamically use (no need to predefine tags).
     * @param tag - The tag to replace.
     * @param publicIds - An array of Public IDs of images uploaded to Cloudinary.
     * @param options - An object holding the available options for the request.
     *                options may include 'exclusive' (boolean) which causes clearing this tag from all other resources
     * @return - A map with the public ids returned from the server.
     * @throws IOException
     */
    public Map replaceTag(String tag, String[] publicIds, Map options) throws IOException {
        return replaceTag(new String[]{tag}, publicIds, options);
    }

    /**
     * Replaces tags to one or more assets in your cloud.
     * Tags are used to categorize and organize your images, and can also be used to apply group actions to images,
     * for example to delete images, create sprites, ZIP files, JSON lists, or animated GIFs.
     * Each image can be assigned one or more tags, which is a short name that you can dynamically use (no need to predefine tags).
     * @param tag - An array of tag to replace.
     * @param publicIds - An array of Public IDs of images uploaded to Cloudinary.
     * @param options - An object holding the available options for the request.
     *                options may include 'exclusive' (boolean) which causes clearing this tag from all other resources
     * @return - A map with the public ids returned from the server.
     * @throws IOException
     */
    public Map replaceTag(String[] tag, String[] publicIds, Map options) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        return callTagsApi(tag, Command.replace, publicIds, options);
    }

    public Map callTagsApi(String[] tag, String command, String[] publicIds, Map options) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        Map<String, Object> params = new HashMap<String, Object>();
        if (tag != null) {
            params.put("tag", StringUtils.join(tag, ","));
        }
        params.put("command", command);
        params.put("type", (String) options.get("type"));
        params.put("public_ids", Arrays.asList(publicIds));
        return callApi("tags", params, options, null);
    }

    /**
     * Add a context keys and values. If a particular key already exists, the value associated with the key is updated.
     * @param context a map of key and value. Serialized to "key1=value1|key2=value2"
     * @param publicIds the public IDs of the resources to update
     * @param options additional options passed to the request
     * @return a list of public IDs that were updated
     * @throws IOException
     */
    public Map addContext(Map context, String[] publicIds, Map options) throws IOException {
        return callContextApi(context, Command.add, publicIds, options);
    }

    /**
     * Add a context keys and values. If a particular key already exists, the value associated with the key is updated.
     * @param context Serialized context in the form of "key1=value1|key2=value2"
     * @param publicIds the public IDs of the resources to update
     * @param options additional options passed to the request
     * @return a list of public IDs that were updated
     * @throws IOException
     */
    public Map addContext(String context, String[] publicIds, Map options) throws IOException {
        return callContextApi(context, Command.add, publicIds, options);
    }

    /**
     * Remove all custom context from the specified public IDs.
     * @param publicIds the public IDs of the resources to update
     * @param options additional options passed to the request
     * @return a list of public IDs that were updated
     * @throws IOException
     */
    public Map removeAllContext(String[] publicIds, Map options) throws IOException {
        return callContextApi((String)null, Command.removeAll, publicIds, options);
    }

    protected Map callContextApi(Map context, String command, String[] publicIds, Map options) throws IOException {
        return callContextApi(Util.encodeContext(context), command, publicIds, options);
    }

    protected Map callContextApi(String context, String command, String[] publicIds, Map options) throws IOException {
        if (options == null)
            options = ObjectUtils.emptyMap();
        Map<String, Object> params = new HashMap<String, Object>();
        if (context != null) {
            params.put("context", context);
        }
        params.put("command", command);
        params.put("type", (String) options.get("type"));
        params.put("public_ids", Arrays.asList(publicIds));
        return callApi("context", params, options, null);
    }

    private final static String[] TEXT_PARAMS = {"public_id", "font_family", "font_size", "font_color", "text_align", "font_weight", "font_style",
            "background", "opacity", "text_decoration"};

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

    public Map createArchive(Map options, String targetFormat) throws IOException {
        Map params = Util.buildArchiveParams(options, targetFormat);
        return callApi("generate_archive", params, options, null);
    }

    public Map createZip(Map options) throws IOException {
        return createArchive(options, "zip");
    }

    public Map createArchive(ArchiveParams params) throws IOException {
        return createArchive(params.toMap(), params.targetFormat());
    }

    public void signRequestParams(Map<String, Object> params, Map options) {
        if (!params.containsKey("timestamp"))
            params.put("timestamp", Util.timestamp());
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

    public Map deleteByToken(String token) throws Exception {
        return callApi("delete_by_token", ObjectUtils.asMap("token", token), ObjectUtils.emptyMap(), null);
    }

    /**
     * Populates metadata fields with the given values. Existing values will be overwritten.
     * @param metadata a map of field name and value.
     * @param publicIds the public IDs of the resources to update
     * @param options additional options passed to the request
     * @return a list of public IDs that were updated
     * @throws IOException
     */
    public Map updateMetadata(Map metadata, String[] publicIds, Map options) throws IOException {
        if (options == null)
            options = new HashMap();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("metadata", Util.encodeContext(metadata));
        params.put("public_ids", Arrays.asList(publicIds));
        params.put("type", (String)options.get("type"));

        return callApi("metadata", params, options, null);
    }
}
