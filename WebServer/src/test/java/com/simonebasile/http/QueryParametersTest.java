package com.simonebasile.http;

import com.simonebasile.http.format.QueryParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class QueryParametersTest {


    @Test
    public void testQueryParameters() throws IOException {
        Map<String, String> decode = QueryParameters.decode("id=lama&gatto=rana&titolo=io+e+un+mio+amico");
        Assertions.assertEquals("lama", decode.get("id"));
        Assertions.assertEquals("rana", decode.get("gatto"));
        Assertions.assertEquals("io e un mio amico", decode.get("titolo"));
    }

    @Test
    public void testQueryParametersUnescaping1() throws IOException {
        Map<String, String> decode = QueryParameters.decode("stato=max%26co%20%c3%A8%20arrivato%20al%2016%25&");
        Assertions.assertEquals("max&co è arrivato al 16%", decode.get("stato"));
    }

    @Test
    public void testQueryParametersUnescaping3() throws IOException {
        FilterInputStream filterInputStream = new FilterInputStream(new ByteArrayInputStream("k=%&bau=miao".getBytes(StandardCharsets.UTF_8))) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        Map<String, String> decode = QueryParameters.decode(filterInputStream);
        Assertions.assertEquals("%", decode.get("k"));
        Assertions.assertEquals("miao", decode.get("bau"));
    }

    @Test
    public void testQueryParametersUnescaping2() throws IOException {
        Map<String, String> decode = QueryParameters.decode("simbolo=%&limone&");
        Assertions.assertEquals("%", decode.get("simbolo"));
        Assertions.assertEquals("", decode.get("limone"));
    }



}
