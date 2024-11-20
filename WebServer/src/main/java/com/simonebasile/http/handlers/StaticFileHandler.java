package com.simonebasile.http.handlers;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.FileResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class StaticFileHandler<T> implements HttpRequestHandler<T, RequestContext> {
    private static final Logger log = LoggerFactory.getLogger(StaticFileHandler.class);
    private final String rootDirectory;


    public StaticFileHandler(String rootDirectory) {
        if(!rootDirectory.endsWith("/")) rootDirectory += "/";
        this.rootDirectory = rootDirectory;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends T> r, RequestContext ctx) {
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

    private HttpResponse<? extends HttpResponse.ResponseBody> handleGet(RequestContext ctx) {
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

    private String getFilePath(String resource) {
        if(resource.contains("..")) {
            throw new StatusErr(404);
        }
        while(resource.startsWith("/")) {
            resource = resource.substring(1);
        }
        return resource;
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> handleHead(HttpRequest<? extends T> r, RequestContext ctx) {
        return new HttpResponse<>(handleGet(ctx), null);
    }

    private static class StatusErr extends RuntimeException {
        private final int statusCode;

        public StatusErr(int statusCode) {
            this.statusCode = statusCode;
        }
    }


}
