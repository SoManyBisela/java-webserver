package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class StaticFileHandler implements HttpRequestHandler<InputStream> {
    private static final Logger log = LoggerFactory.getLogger(StaticFileHandler.class);
    private final String registrationPath;
    private final String rootDirectory;


    public StaticFileHandler(String registrationPath, String rootDirectory) {
        //TODO in the future the webserver should tell the handler the registration path that matched
        this.registrationPath = registrationPath;
        if(!rootDirectory.endsWith("/")) rootDirectory += "/";
        this.rootDirectory = rootDirectory;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<InputStream> r) {
        log.debug("Into static file handler");
        String method = r.getMethod();
        try {
            if ("GET".equalsIgnoreCase(method)) {
                return handleGet(r);
            } else if ("HEAD".equalsIgnoreCase(method)) {
                return handleHead(r);
            } else {
                throw new StatusErr(405);
            }
        } catch (StatusErr e) {
            return new HttpResponse<>(r.getVersion(), e.statusCode, new HttpHeaders(), new ByteResponseBody(HttpStatusCode.getStatusString(e.statusCode)));
        }
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        String path = getFilePath(r.getResource());
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
        return new HttpResponse<>(r.getVersion(), 200, new HttpHeaders(), new FileResponseBody(targetFile));
    }

    private String getFilePath(String resource) {
        if(!resource.startsWith(registrationPath) || resource.contains("..")) {
            throw new StatusErr(404);
        }
        String filePath = resource.substring(registrationPath.length());
        while(filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        return filePath;
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> handleHead(HttpRequest<InputStream> r) {
        return new HttpResponse<>(handleGet(r), null);
    }

    private static class StatusErr extends RuntimeException {
        private final int statusCode;

        public StatusErr(int statusCode) {
            this.statusCode = statusCode;
        }
    }

    public static class FileResponseBody implements HttpResponse.ResponseBody {
        private final File file;

        public FileResponseBody(File targetFile) {
            this.file = targetFile;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            try (InputStream in = new FileInputStream(file)) {
                in.transferTo(out);
            }
        }

        @Override
        public Long contentLength() {
            return file.length();
        }

        @Override
        public String contentType() {
            String name = file.getName();
            int extStart = name.lastIndexOf(".");
            String extension = extStart == -1 ? null : name.substring(extStart + 1);
            return getMime(extension);
        }
    }

    private static String getMime(String extension) {
        if(extension == null || extension.isEmpty()) {
            return "application/octet-stream";
        }
        return switch (extension) {
            case "txt" -> "text/plain";
            case "html" -> "text/html";
            case "js" -> "text/javascript";
            case "xml" -> "text/xml";
            case "css" -> "text/css";
            case "json" -> "application/json";
            default -> "application/octet-stream";
        };
    }
}
