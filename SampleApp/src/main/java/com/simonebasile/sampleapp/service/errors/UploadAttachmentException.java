package com.simonebasile.sampleapp.service.errors;

public class UploadAttachmentException extends RuntimeException {
    public UploadAttachmentException(String message) {
        super(message);
    }
}
