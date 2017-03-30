package com.cloudinary.android;

import com.cloudinary.Cloudinary;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 *
 * @author www.codejava.net
 * @author Cloudinary
 */
public class MultipartUtility {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private final MultipartCallback multipartCallback;
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    public final static String USER_AGENT = "CloudinaryAndroid/" + Cloudinary.VERSION;

    /**
     * This constructor initializes a new HTTP POST request with content type is
     * set to multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public MultipartUtility(String requestURL, String charset, String boundary, Map<String, String> headers) throws IOException {
        this(requestURL, charset, boundary, headers, null);
    }

    public MultipartUtility(String requestURL, String charset, String boundary, Map<String, String> headers, MultipartCallback multipartCallback) throws IOException {
        this.charset = charset;
        this.boundary = boundary;
        this.multipartCallback = multipartCallback;

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setChunkedStreamingMode(0);
        httpConn.setDoInput(true);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpConn.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", USER_AGENT);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }

    public MultipartUtility(String requestURL, String charset, String boundary) throws IOException {
        this(requestURL, charset, boundary, null, null);
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in {@code <input type="file" name="..." />}
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile, String fileName) throws IOException {
        if (fileName == null) fileName = uploadFile.getName();
        FileInputStream inputStream = new FileInputStream(uploadFile);
        addFilePart(fieldName, inputStream, fileName);
    }

    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        addFilePart(fieldName, uploadFile, "file");
    }

    public void addFilePart(String fieldName, InputStream inputStream, String fileName) throws IOException {
        if (fileName == null) fileName = "file";
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
        writer.append("Content-Type: ").append(APPLICATION_OCTET_STREAM).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            notifyCallback(totalRead += bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    private void notifyCallback(long bytes) {
        if (multipartCallback != null) {
            multipartCallback.totalBytesLoaded(bytes);
        }
    }

    public void addFilePart(String fieldName, InputStream inputStream) throws IOException {
        addFilePart(fieldName, inputStream, "file");
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned status
     * OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public HttpURLConnection execute() throws IOException {
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        return httpConn;
    }

    /**
     * Closes the internal connection's output stream.
     * Closing a previously closed stream has no effect.
     */
    public void close(){
        if (writer != null){
            writer.close();
        }
    }

    /**
     * For internal use only - callback to monitor multipart upload progress
     */
    interface MultipartCallback {
        void totalBytesLoaded(long bytes);
    }

}
