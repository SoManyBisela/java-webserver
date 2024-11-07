package com.simonebasile.sampleapp.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UploadAttachmentRequest {
    String ticketId;
    String filename;

    public boolean valid() {
        return filename != null && !filename.isEmpty();
    }
}
