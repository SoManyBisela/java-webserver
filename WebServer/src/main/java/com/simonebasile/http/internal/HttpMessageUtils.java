package com.simonebasile.http.internal;

import com.simonebasile.http.message.*;
import com.simonebasile.http.response.HttpResponseBody;

import java.io.*;
import java.util.List;

public class HttpMessageUtils {

    /**
     * Parses an HTTP request from an input stream.
     * The response body is limited to the content length specified in the headers.
     *
     * @param his the input stream to read from
     * @return the parsed HTTP request
     * @throws IOException if an I/O error occurs
     */
    public static HttpRequest<InputStream> parseRequest(HttpInputStream his) throws IOException {
        String line;
        try {
            line = his.readLine();
        }catch (EOFException e){
            throw new ConnectionClosedBeforeRequestStartException();
        }
        String[] statusLine = line.split(" ");
        if (statusLine.length != 3) {
            throw new CustomException("Invalid status line: " + line);
        }

        String method = statusLine[0];
        String resource = statusLine[1];
        HttpVersion version = HttpVersion.parse(statusLine[2]);

        HttpHeaders headers = new HttpHeaders();
        while (!(line = his.readLine()).isEmpty()) {
            parseHeader(headers, line);
        }
        Integer length = headers.contentLength();
        if(length == null) length = 0;
        InputStream body = new FixedLengthInputStream(his, length);
        return new HttpRequest<>(method, resource, version, headers, body);

    }

    /**
     * Writes the response to the given output stream.
     *
     * @param version the HTTP version to use
     * @param response the response to write
     * @param outputStream the output stream to write the response in
     * @throws IOException if an I/O error occurs
     */
    public static void writeResponse(HttpVersion version, HttpResponse<? extends HttpResponseBody> response, HttpOutputStream outputStream) throws IOException {
        var writingHeaders = new HttpHeaders(response.getHeaders());
        HttpResponseBody writingBody = null;
        OutputStream out = outputStream;
        HttpResponseBody body = response.getBody();
        if(body != null) {
            Long cl = body.contentLength();
            if(body.contentType() != null) {
                writingHeaders.add("Content-Type", body.contentType());
            }
            if(cl == null) {
                writingBody = body;
                //Length is unknown, use chunked encoding
                List<String> currentTransferEncoding = writingHeaders.transferEncoding();
                currentTransferEncoding.add("chunked");
                writingHeaders.add("Transfer-Encoding", String.join(", ", currentTransferEncoding));
                //wrap chunked wrapper in an output stream to avoid writing little chunks of response
                //and decrease overhead
                out = new BufferedOutputStream(new ChunkedWrapper(outputStream));
            } else if(cl > 0){
                writingHeaders.add("Content-Length", Long.toString(cl));
                writingBody = body;
                //avoid flushing or closing the http output stream
                out = new FilterOutputStream(outputStream) {
                    @Override
                    public void flush() { }

                    @Override
                    public void close() { }
                };
            } else {
                writingHeaders.add("Content-Length", "0");
            }
        } else {
            writingHeaders.add("Content-Length", "0");
        }

        outputStream.writeStatus(version, response.getStatusCode(), HttpStatusCode.getStatusString(response.getStatusCode()));
        for(var header : writingHeaders.entries()) {
            for(var value: header.getValue()) {
                outputStream.writeHeader(header.getKey(), value);
            }
        }
        outputStream.endHeaders();
        if(writingBody != null) {
            writingBody.write(out);
            out.flush();
            out.close();
        }
        outputStream.flush();
    }

    /**
     * Parses a string containing the header of an HTTP message and adds it to this HttpHeaders.
     * @param line the string header to parse
     */
    public static void parseHeader(HttpHeaders headers, String line) {
        int length = line.length();
        int colon = line.indexOf(":");
        String err = null;
        if (colon == -1) {
            err = "Invalid header: Missing colon";
        } else if (length == colon + 1) {
            err = "Invalid header: Ends with colon";
        }
        if (err != null) throw new CustomException(err);
        String key = line.substring(0, colon);
        while (++colon < length &&
                Character.isWhitespace(line.charAt(colon))) {
        }
        if (colon == length) throw new CustomException("Invalid header: Missing value");
        String value = line.substring(colon);
        headers.add(key, value);
    }

}
