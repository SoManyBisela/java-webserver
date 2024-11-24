package com.simonebasile.http.handlers;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.FileResponseBody;
import com.simonebasile.http.response.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * A handler that serves static files from a directory.
 * The root directory is specified in the constructor.
 * The handler will serve files from the root directory and its subdirectories.
 * The handler will return a 404 Not Found response if the file does not exist.
 * The handler will return a 405 Method Not Allowed response if the method is not GET or HEAD.
 * @param <T> the type of the request body
 */
public class StaticFileHandler<T> implements HttpRequestHandler<T, RequestContext> {
    private static final Logger log = LoggerFactory.getLogger(StaticFileHandler.class);
    private final String rootDirectory;


    /**
     * Creates a new static file handler.
     * @param rootDirectory the root directory
     */
    public StaticFileHandler(String rootDirectory) {
        if(!rootDirectory.endsWith("/")) rootDirectory += "/";
        this.rootDirectory = rootDirectory;
    }

    /**
     * Handles the request.
     * The handler will return a 405 Method Not Allowed response if the method is not GET or HEAD.
     *
     * @param r the request
     * @param ctx the context
     * @return the response
     */
    @Override
    public HttpResponse<? extends ResponseBody> handle(HttpRequest<? extends T> r, RequestContext ctx) {
        log.debug("Into static file handler");
        String method = r.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            return handleGet(ctx);
        } else if ("HEAD".equalsIgnoreCase(method)) {
            return handleHead(r, ctx);
        } else {
            throw new StatusErr(405);
        }
    }

    /**
     * Handles a GET request.
     * The request path is used to find the file to serve.
     * If the file is a directory, the handler will look for an index.html file in the directory.
     * If the file does not exist, a 404 Not Found response is returned.
     *
     * @param ctx the context
     * @return the response
     */
    private HttpResponse<? extends ResponseBody> handleGet(RequestContext ctx) {
        try {
            String path = getFilePath(ctx.getContextMatch().remainingPath());
            File targetFile = new File(rootDirectory + path);
            if(!targetFile.exists()) {
                throw new StatusErr(404);
            }
            if(targetFile.isDirectory()) {
                targetFile = new File(targetFile.getPath() + "/index.html");
                if(!targetFile.exists()) {
                    throw new StatusErr(404);
                }
            }
            return new HttpResponse<>(200, new HttpHeaders(), new FileResponseBody(targetFile));
        } catch (StatusErr e) {
            return new HttpResponse<>(e.statusCode, new HttpHeaders(), new ByteResponseBody(HttpStatusCode.getStatusString(e.statusCode)));
        }
    }

    /**
     * Gets the file path from the resource.
     * removes leading slashes and checks for invalid paths.
     *
     * @param resource the resource path
     * @return the file path
     * @throws StatusErr if the resource contains invalid path
     */
    private String getFilePath(String resource) {
        if(resource.contains("..")) {
            throw new StatusErr(404);
        }
        while(resource.startsWith("/")) {
            resource = resource.substring(1);
        }
        return resource;
    }

    /**
     * Handles a HEAD request. TODO details
     * @param r the request
     * @param ctx the context
     * @return the response
     */
    private HttpResponse<? extends ResponseBody> handleHead(HttpRequest<? extends T> r, RequestContext ctx) {
        return new HttpResponse<>(handleGet(ctx), null);
    }

    /**
     * A runtime exception that represents an HTTP status code.
     */
    private static class StatusErr extends RuntimeException {
        private final int statusCode;

        public StatusErr(int statusCode) {
            this.statusCode = statusCode;
        }
    }


}
