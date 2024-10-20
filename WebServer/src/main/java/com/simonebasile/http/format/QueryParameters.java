package com.simonebasile.http.format;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//https://url.spec.whatwg.org/#urlencoded-parsing
//https://developer.mozilla.org/en-US/docs/Glossary/Percent-encoding
public class QueryParameters {
    public static Map<String, String> decode(String params) throws IOException {
        return decode(new StringReader(params));
    }

    public static Map<String, String> decode(InputStream params) throws IOException {
        return decode(new InputStreamReader(params, StandardCharsets.UTF_8));
    }

    public static Map<String, String> decode(Reader params) throws IOException {
        HashMap<String, String> paramsOut = new HashMap<>();
        StringBuilder curr = new StringBuilder();
        BufferedReader reader = new BufferedReader(params, 3);
        String name = null, value;
        int c;
        while((c = reader.read()) != -1) {
            if(c == '%') {
                reader.mark(2);
                int c0 = hexRead(reader.read());
                int c1 = hexRead(reader.read());
                if(c0 == -1 || c1 == -1) {
                    curr.append("%");
                    reader.reset();
                    continue;
                }
                curr.append((char)(c0 * 16 + c1));
                continue;
            }
            if(name == null && c == '=') {
                name = curr.toString();
                curr.setLength(0);
                continue;
            } else if(c == '&') {
                if(name == null) {
                    name = curr.toString();
                    value = "";
                } else {
                    value = curr.toString();
                }
                curr.setLength(0);
                paramsOut.put(name, value);
                name = value = null;
                continue;
            }
            curr.append((char)c);
        }
        if(name == null) {
            name = curr.toString();
            value = "";
        } else {
            value = curr.toString();
        }
        curr.setLength(0);
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