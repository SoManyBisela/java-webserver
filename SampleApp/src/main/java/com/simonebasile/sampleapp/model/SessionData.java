package com.simonebasile.sampleapp.model;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SessionData {
    private String id;
    private String username;

    public SessionData(String sessionId) {
        this.id = sessionId;
    }
}
