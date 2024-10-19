package com.simonebasile.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class HttpResponse<T extends HttpResponse.ResponseBody> extends HttpMessage<T>{
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    protected final int statusCode;
    public interface ResponseBody{
        /**
         * Implementation must write the response in the output stream.
         * Number of bytes must match the return value of contentLength().
         *
         * @param out the output stream to write the response in
         * @throws IOException in case an IOException occurred while writing the response
         */

        void write(OutputStream out) throws IOException;

        /**
         * implementations must return the number of bytes that will be written to the outputStream when calling write
         * or null if the number is not know in advance.
         * in the first case the response will be sent with Content-Length header equal to the return of this function,
         * in the latter the response will be sent with Transfer-encoding: chunked.
         *
         * @return the length of the body or null
         */
        Long contentLength();

        /**
         * implementations must return the content type of the body, or null if not applicable.
         * This value will be sent in the Content-Type header.
         *
         * @return content type of the body or null
         */
        String contentType();
    }

    public HttpResponse(HttpVersion version, int statusCode, HttpHeaders headers, T body) {
        super(version, headers, body);
        this.statusCode = statusCode;
    }

    public HttpResponse(HttpResponse<?> source, T body) {
        this(source, source.statusCode, body);
    }

    public HttpResponse(HttpMessage<?> source, int statusCode, T body) {
        super(source, body);
        this.statusCode = statusCode;
    }


    public void write(HttpOutputStream outputStream) throws IOException {
        var writingHeaders = new HttpHeaders(headers);
        ResponseBody writingBody = null;
        OutputStream out = outputStream;
        boolean chunked = false;
        if(body != null) {
            //TOD Check if content type is already present
            Long cl = body.contentLength();
            if(body.contentType() != null) {
                writingHeaders.add("Content-Type", body.contentType());
            }
            if(cl == null) {
                writingBody = body;
                //Streaming body
                //TODO parse current transfer-encoding, update and send downstream
                writingHeaders.add("Transfer-Encoding", "chunked");
                out = new ChunkedWrapper(outputStream);
                chunked = true;
            } else if(cl > 0){
                //TODO check if content-length is already present
                writingHeaders.add("Content-Length", Long.toString(cl));
                writingBody = body;
            }
        }

        outputStream.writeStatus(version, statusCode, getStatusString(statusCode));
        for(var header : writingHeaders.entries()) {
            for(var value: header.getValue()) {
                outputStream.writeHeader(header.getKey(), value);
            }
        }

        if(writingBody != null) {
            outputStream.endHeaders();
            writingBody.write(out);
            outputStream.write("0\r\n");
        }
        outputStream.end();
        outputStream.flush();
    }

    public static String getStatusString(int statusCode) {
        return switch (statusCode) {
            case 101 -> "Switching Protocols";
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 418 -> "I'm a teapot";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> {
                log.warn("Missing status string for code {}", statusCode);
                yield "";
            }
        };
    }

    public int getStatusCode() {
        return statusCode;
    }
}
