package com.simonebasile.http.response;

import com.simonebasile.http.HttpResponse;

import java.io.*;

public class FileResponseBody implements HttpResponse.ResponseBody {
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

    public static String getMime(String extension) {
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
