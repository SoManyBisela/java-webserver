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
    private boolean assign;
    private boolean close;

    public void setAssign(String assign) {
        this.assign = assign != null && assign.isEmpty();
    }

    public void setClose(String close) {
        this.close = close != null && close.isEmpty();
    }
}
