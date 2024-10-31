package com.simonebasile.http;

import com.simonebasile.http.unpub.ChunkedWrapper;
import com.simonebasile.http.unpub.HttpOutputStream;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
