package com.simonebasile.http.response;

import java.io.IOException;
import java.io.OutputStream;

public interface ResponseBody {
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
