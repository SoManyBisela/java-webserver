package com.simonebasile.http;

import com.simonebasile.http.unpub.ChunkedWrapper;
import com.simonebasile.http.unpub.HttpOutputStream;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents an HTTP response.
 *
 * @param <T> the type of the body of the HTTP response
 */
public class HttpResponse<T extends HttpResponse.ResponseBody> extends HttpMessage<T>{
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

    /**
     * Creates a new HTTP response with status code 200.
     *
     * @param body the body of the response
     */
    public HttpResponse(T body) {
        this(200, new HttpHeaders(), body);
    }

    /**
     * Creates a new HTTP response with the given status code.
     *
     * @param statusCode the status code of the response
     * @param body the body of the response
     */
    public HttpResponse(int statusCode, T body) {
        this(statusCode, new HttpHeaders(), body);
    }

    /**
     * Creates a new HTTP response with the given status code and headers.
     *
     * @param statusCode the status code of the response
     * @param headers the headers of the response
     * @param body the body of the response
     */
    public HttpResponse(int statusCode, HttpHeaders headers, T body) {
        super(headers, body);
        this.statusCode = statusCode;
    }

    /**
     * Creates copy of the given response with the given body.
     *
     * @param source the response to copy
     * @param body the body of the response
     */
    public HttpResponse(HttpResponse<?> source, T body) {
        this(source, source.statusCode, body);
    }

    /**
     * Creates a copy of the given response with the given status code and body.
     *
     * @param source the response to copy
     * @param statusCode the status code of the response
     * @param body the body of the response
     */
    public HttpResponse(HttpMessage<?> source, int statusCode, T body) {
        super(source, body);
        this.statusCode = statusCode;
    }


    /**
     * Writes the response to the given output stream.
     *
     * @param version the HTTP version to use
     * @param outputStream the output stream to write the response in
     * @throws IOException if an I/O error occurs
     */
    public void write(HttpVersion version, HttpOutputStream outputStream) throws IOException {
        var writingHeaders = new HttpHeaders(headers);
        ResponseBody writingBody = null;
        OutputStream out = outputStream;
        if(body != null) {
            //TODO Check if content type is already present
            Long cl = body.contentLength();
            if(body.contentType() != null) {
                writingHeaders.add("Content-Type", body.contentType());
            }
            if(cl == null) {
                writingBody = body;
                //Streaming body
                //TODO parse current transfer-encoding, update and send downstream
                //TODO check if content length is present and remove it?
                writingHeaders.add("Transfer-Encoding", "chunked");
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

        outputStream.writeStatus(version, statusCode, HttpStatusCode.getStatusString(statusCode));
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

    public int getStatusCode() {
        return statusCode;
    }
}
