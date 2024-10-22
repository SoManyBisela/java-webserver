package com.simonebasile.http.format;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//https://url.spec.whatwg.org/#urlencoded-parsing
//https://developer.mozilla.org/en-US/docs/Glossary/Percent-encoding
public class QueryParameters {
    public static Map<String, String> decode(String params) throws IOException {
        return decode(new ByteArrayInputStream(params.getBytes(StandardCharsets.UTF_8)));
    }

    public static Map<String, String> decode(InputStream reader) throws IOException {
        HashMap<String, String> paramsOut = new HashMap<>();
        ByteArrayOutputStream curr = new ByteArrayOutputStream();
        if(!reader.markSupported()) {
            reader = new BufferedInputStream(reader, 3);
        }
        String name = null, value;
        int c;
        while((c = reader.read()) != -1) {
            if(c == '+') {
                curr.write(' ');
            } else if(c == '%') {
                reader.mark(2);
                int c0 = hexRead(reader.read());
                int c1 = hexRead(reader.read());
                if(c0 == -1 || c1 == -1) {
                    curr.write('%');
                    reader.reset();
                } else {
                    curr.write(c0 * 16 + c1);
                }
            } else if(name == null && c == '=') {
                name = curr.toString(StandardCharsets.UTF_8);
                curr.reset();
            } else if(c == '&') {
                if(name == null) {
                    name = curr.toString(StandardCharsets.UTF_8);
                    value = "";
                } else {
                    value = curr.toString(StandardCharsets.UTF_8);
                }
                curr.reset();
                paramsOut.put(name, value);
                name = value = null;
            } else {
                curr.write(c);
            }
        }
        if(name == null) {
            name = curr.toString(StandardCharsets.UTF_8);
            value = "";
        } else {
            value = curr.toString(StandardCharsets.UTF_8);
        }
        paramsOut.put(name, value);
        return paramsOut;
    }

    private static int hexRead(int read) {
        if(read >= '0' && read <= '9') {
            return read - '0';
        } else if(read >= 'A' && read <= 'F') {
            return read - 'A' + 10;
        } else if(read >= 'a' && read <= 'f') {
            return read - 'a' + 10;
        } else {
            //Not hexadecimal
            return -1;
        }
    }

}