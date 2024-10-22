package com.simonebasile.sampleapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateTicket {
    private String id;
    private String comment;
    private String assignee;
    private boolean close;
}
