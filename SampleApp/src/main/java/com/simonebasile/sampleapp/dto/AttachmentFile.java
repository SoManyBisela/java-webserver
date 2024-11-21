package com.simonebasile.sampleapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class AttachmentFile {
    private String name;
    private File content;
}
