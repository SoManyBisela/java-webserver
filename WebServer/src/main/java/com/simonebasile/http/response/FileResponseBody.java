package com.simonebasile.http.response;

import java.io.*;

/**
 * A response body that writes a file to the output stream.
 */
public class FileResponseBody implements HttpResponseBody {
    private final File file;

    /**
     * Creates a new file response body.
     * @param targetFile the file to write
     */
    public FileResponseBody(File targetFile) {
        this.file = targetFile;
    }

    /**
     * Writes the file to the output stream.
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            in.transferTo(out);
        }
    }

    /**
     * Returns the length of the file as the content-length of the body.
     * @return the length of the file
     */
    @Override
    public Long contentLength() {
        return file.length();
    }

    /**
     * Returns the content type of the file.
     * The content type is determined by the file extension.
     * @return the content type
     */
    @Override
    public String contentType() {
        String name = file.getName();
        int extStart = name.lastIndexOf(".");
        String extension = extStart == -1 ? null : name.substring(extStart + 1);
        return getMime(extension);
    }

    /**
     * Returns the MIME type for the given file extension.
     * @param extension the file extension
     * @return the MIME type
     */
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
