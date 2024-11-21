package com.simonebasile.http;

import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.unpub.CustomException;
import com.simonebasile.http.unpub.InterceptorChainImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is the implementation of the HttpRoutingContext interface.
 */
public class HttpRoutingContextImpl<Body, Context extends RequestContext> implements HttpRequestHandler<Body, Context>, HttpRoutingContext<Body, Context> {
    private static final Logger log = LoggerFactory.getLogger(HttpRoutingContextImpl.class);

    private final HandlerRegistry<HttpRequestHandler<Body, ? super Context>> handlers;
    private final List<HttpInterceptor<Body, Context>> interceptors = new ArrayList<>();

    public HttpRoutingContextImpl() {
        this.handlers = new HandlerRegistry<>();
    }

    public void registerHttpContext(String path, HttpRequestHandler<Body, ? super Context> handler){
        log.debug("Registered new http context for path [{}]", path);
        if(!handlers.insertCtx(path, handler)) {
            throw new CustomException("An http handler for path [" + path + "] already exists");
        }
    }

    public void registerHttpHandler(String path, HttpRequestHandler<Body, ? super Context> handler){
        log.debug("Registered new http handler for path [{}]", path);
        if(!handlers.insertExact(path, handler)) {
            throw new CustomException("An http handler for path [" + path + "] already exists");
        }
    }

    @Override
    public void registerInterceptor(HttpInterceptor<Body, Context> interceptor) {
        interceptors.add(interceptor);

    }

    /**
     * This method is called to handle an HTTP request.
     * It will find the correct handler for the request and call it.
     * In case of nested contexts the handler will be matched against the remaining path.
     * And the context will be updated to reflect the matched path.
     * Any registered Interceptor will be called before the handler is called.
     * @param req the request to handle
     * @param requestContext the context of the request
     * @return the response to send to the client
     */
    @Override
    public HttpResponse<?> handle(HttpRequest<? extends Body> req, Context requestContext) {
        String prevMatched = "";
        String resource = req.getResource();
        final ResourceMatch contextMatch = requestContext.getContextMatch();
        if(contextMatch != null) {
            //In case this is a nested context only match on the remaining part
            prevMatched = contextMatch.matchedPath();
            resource = contextMatch.remainingPath();
        }
        var match = handlers.getHandler(resource);
        if(match == null) {
            return new HttpResponse<>(
                    404,
                    new HttpHeaders(),
                    new ByteResponseBody("Resource not found")
            );
        }
        requestContext.setContextMatch(new ResourceMatch(
                prevMatched + match.match().matchedPath(),
                match.match().remainingPath()
        ));
        var httpHandler = match.handler();
        if(!interceptors.isEmpty()){
            httpHandler = new InterceptorChainImpl<>(interceptors, httpHandler);
        }
        return httpHandler.handle(req, requestContext);
    }

}
