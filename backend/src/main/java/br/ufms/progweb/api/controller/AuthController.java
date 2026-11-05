package br.ufms.progweb.api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufms.progweb.api.dto.LoginRequest;
import br.ufms.progweb.api.dto.LoginResponse;
import br.ufms.progweb.api.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController{
    private final AuthService service;

    public AuthController(AuthService service){
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return service.login(request);
    }

}