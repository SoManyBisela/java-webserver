package com.simonebasile.http;

import com.simonebasile.http.unpub.ChunkedWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ChunkedWrapperTest {

    @Test
    void testChunkWrite() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChunkedWrapper chunkedWrapper = new ChunkedWrapper(outputStream);
        chunkedWrapper.write('p');
        chunkedWrapper.write(new byte[0]);
        chunkedWrapper.write("la mela azzurra".getBytes(StandardCharsets.UTF_8));
        chunkedWrapper.close();
        String result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals("1\r\np\r\nf\r\nla mela azzurra\r\n0\r\n\r\n", result);

    }
}
