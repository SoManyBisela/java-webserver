package com.simonebasile.sampleapp.mapping;

import com.simonebasile.sampleapp.json.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FormHttpMapper {
    private static final Logger log = LoggerFactory.getLogger(FormHttpMapper.class);

    public static <T> T map(InputStream requestBody, Class<T> type) {
        //https://url.spec.whatwg.org/#urlencoded-parsing
        //https://developer.mozilla.org/en-US/docs/Glossary/Percent-encoding
        HashMap<String, String> formInput = new HashMap<>();
        try {
            //TODO Extract to class for unit testing
            if(log.isDebugEnabled()) {
                byte[] bytes = requestBody.readAllBytes();
                log.debug("Body: {}", new String(bytes, StandardCharsets.UTF_8));
                requestBody = new ByteArrayInputStream(bytes);
            }
            StringBuilder curr = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody, StandardCharsets.UTF_8), 3);
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
                    formInput.put(name, value);
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
            formInput.put(name, value);
        } catch (IOException e) {
            throw new RuntimeException("IOException reading request body", e);
        }
        return ObjectMapperConfig.jsonMapper.convertValue(formInput, type);
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
