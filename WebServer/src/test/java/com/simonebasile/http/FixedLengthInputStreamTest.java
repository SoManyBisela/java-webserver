package com.simonebasile.http;

import com.simonebasile.http.unpub.FixedLengthInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

public class FixedLengthInputStreamTest {

    @Test
    void testRead() throws IOException {
        byte[] buf = new byte[100];
        for(int i = 0; i < buf.length; i++) {
            buf[i] = (byte)i;
        }
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        int limit = 15;
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(source, limit);
        for(int i = 0; i < buf.length; i++) {
            int read = fixedLengthInputStream.read();
            if(i < limit) {
                Assertions.assertEquals(buf[i], read);
            } else {
                Assertions.assertEquals(-1, read);
            }
        }
    }

    @Test
    void testRead2() throws IOException {
        byte[] buf = new byte[10];
        for(int i = 0; i < buf.length; i++) {
            buf[i] = (byte)i;
        }
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        int limit = 15;
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(source, limit);
        for(int i = 0; i < limit; i++) {
            int read = fixedLengthInputStream.read();
            if(i < buf.length) {
                Assertions.assertEquals(buf[i], read);
            } else {
                Assertions.assertEquals(-1, read);
            }
        }

    }

    @Test
    void testReadBuf() throws IOException {
        byte[] buf = new byte[100];
        for(int i = 0; i < buf.length; i++) {
            buf[i] = (byte)i;
        }
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(source, 15);
        byte[] resbox = new byte[10];
        int read = fixedLengthInputStream.read(resbox, 0, 10);
        Assertions.assertEquals(read, 10);
        int i = 0;
        for(int start = i; i < start + read; i++) {
            Assertions.assertEquals(buf[i], resbox[i - start]);
        }
        read = fixedLengthInputStream.read(resbox, 0, 5);
        Assertions.assertEquals(read, 5);
        for(int start = i; i < start + read; i++) {
            Assertions.assertEquals(buf[i], resbox[i - start]);
        }
        read = fixedLengthInputStream.read(resbox, 0, 5);
        Assertions.assertEquals(read, -1);
    }

    @Test
    void testReadBuf2() throws IOException {
        byte[] buf = new byte[10];
        for(int i = 0; i < buf.length; i++) {
            buf[i] = (byte)i;
        }
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(source, 15);
        byte[] bytes = fixedLengthInputStream.readNBytes(15);
        Assertions.assertEquals(bytes.length, 10);
    }

    @Test
    void testReadAll() throws IOException {
        byte[] buf = new byte[100];
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        source.mark(buf.length);
        for(int i = 0; i < buf.length; i++) {
            source.reset();
            FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(source, i);
            byte[] bytes = fixedLengthInputStream.readAllBytes();
            Assertions.assertEquals(i, bytes.length);
        }
    }

    @Test
    void testAvailable() throws IOException {
        byte[] buf = new byte[100];
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(source, 15);

        Assertions.assertEquals(fixedLengthInputStream.available(), 15);

        buf = new byte[10];
        source = new ByteArrayInputStream(buf);
        fixedLengthInputStream = new FixedLengthInputStream(source, 15);

        Assertions.assertEquals(fixedLengthInputStream.available(), 10);
    }

    @Test
    void testClose() throws IOException {
        byte[] buf = new byte[100];
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        source.mark(buf.length);
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(source, 15);
        fixedLengthInputStream.read();

        byte[] bytes = source.readAllBytes();
        Assertions.assertEquals(bytes.length, buf.length - 1);

        source.reset();
        fixedLengthInputStream = new FixedLengthInputStream(source, 15);
        fixedLengthInputStream.read();
        fixedLengthInputStream.close();
        bytes = source.readAllBytes();
        Assertions.assertEquals(bytes.length, buf.length - 15);
    }

    @Test
    void testReadAllLong() throws IOException {
        Random random = new Random();
        int l = random.nextInt(18000, 20000);
        byte[] buf = new byte[l];
        ByteArrayInputStream source = new ByteArrayInputStream(buf);
        int length = 16000;
        FixedLengthInputStream flis = new FixedLengthInputStream(source, length);
        byte[] bytes = flis.readAllBytes();
        Assertions.assertEquals(length, bytes.length);
        Assertions.assertEquals(l - length, source.readAllBytes().length);
    }
}
