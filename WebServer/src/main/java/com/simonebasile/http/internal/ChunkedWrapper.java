package com.simonebasile.http.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * An output stream that wraps another output stream and sends the data in chunks
 * according to the HTTP 1.1 chunked transfer encoding specification.
 */
public class ChunkedWrapper extends OutputStream {
    private final OutputStream target;
    public ChunkedWrapper(OutputStream outputStream) {
        target = outputStream;
    }

    /**
     * Writes a byte of data to the output stream wrapping with the additional information.
     * Use ot this method is discouraged as it incurs in significant overhead.
     * @param b the {@code byte} to write.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(int b) throws IOException {
        target.write("1\r\n".getBytes(StandardCharsets.UTF_8));
        target.write(b);
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to the output stream.
     * prepend the data with the length of the chunk in hexadecimal.
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        //A zero length chunk means the body is finished
        //So if the length is zero we return to avoid sending the chunk unintentionally
        if(len == 0) return;

        target.write(Integer.toString(len, 16).getBytes(StandardCharsets.UTF_8));
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
        target.write(b, off, len);
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Closes the output stream by sending a zero length chunk.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        target.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
