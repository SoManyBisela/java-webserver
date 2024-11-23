package com.simonebasile.sampleapp.dto;

import com.simonebasile.sampleapp.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String username;
    private String password;
    private String cpassword;
    private Role role;

    public CreateUserRequest(RegisterRequest registerRequest) {
        this.username = registerRequest.getUsername();
        this.password = registerRequest.getPassword();
        this.cpassword = registerRequest.getCpassword();
        this.role = Role.user;
    }
}
