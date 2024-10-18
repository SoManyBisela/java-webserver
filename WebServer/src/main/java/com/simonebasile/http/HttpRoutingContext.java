package com.simonebasile.http;

import com.simonebasile.CustomException;
import com.simonebasile.http.response.ByteResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class HttpRoutingContext implements HttpRequestHandler<InputStream>, HttpHandlerContext<InputStream> {
    private static final Logger log = LoggerFactory.getLogger(HttpRoutingContext.class);

    private final HandlerRegistry<HttpRequestHandler<InputStream>> handlers;
    private final List<HttpInterceptor<InputStream>> preprocessors = new ArrayList<>();

    public HttpRoutingContext() {
        this.handlers = new HandlerRegistry<>();
    }

    public void registerHttpContext(String path, HttpRequestHandler<InputStream> handler){
        log.debug("Registered new http handler for path [{}]", path);
        if(!handlers.insertCtx(path, handler)) {
            throw new CustomException("An http handler for path [" + path + "] already exists");
        }
    }

    public void registerHttpHandler(String path, HttpRequestHandler<InputStream> handler){
        log.debug("Registered new http handler for path [{}]", path);
        if(!handlers.insertExact(path, handler)) {
            throw new CustomException("An http handler for path [" + path + "] already exists");
        }
    }

    @Override
    public void registerInterceptor(HttpInterceptor<InputStream> preprocessor) {
        preprocessors.add(preprocessor);

    }

    @Override
    public HttpResponse<?> handle(HttpRequest<InputStream> req) {
        log.debug("Incoming http request [{}]", req);
        //TODO preprocess
        var httpHandler = handlers.getHandler(req.getResource());
        if(httpHandler != null) {
            return httpHandler.handle(req);
        } else {
            return new HttpResponse<>(req.version,
                    404,
                    new HttpHeaders(),
                    new ByteResponseBody("Resource not found")
            );
        }
    }

}
