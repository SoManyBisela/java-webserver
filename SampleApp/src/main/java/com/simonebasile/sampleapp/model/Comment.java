package com.simonebasile.sampleapp.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private String author;
    private String content;
    private LocalDateTime creationDate;
}
