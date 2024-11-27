package com.simonebasile.http;

import com.simonebasile.http.internal.UnmaskingInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

public class UnmaskingInputStreamTest {

    @Test
    public void testUnmaskingInputStream() throws IOException {
        Random random = new Random();
        byte[] mask = new byte[4];
        random.nextBytes(mask);
        int l = random.nextInt(1 << 13, 1 << 16);
        byte[] content = new byte[l];
        byte[] maskedContent = new byte[l];
        for (int i = 0; i < l; i++) {
            maskedContent[i] = (byte) (content[i] ^ mask[i % mask.length]);
        }
        UnmaskingInputStream unmaskingInputStream = new UnmaskingInputStream(new ByteArrayInputStream(content), mask);
        byte[] read = unmaskingInputStream.readAllBytes();
        Assertions.assertArrayEquals(maskedContent, read);
    }
}
