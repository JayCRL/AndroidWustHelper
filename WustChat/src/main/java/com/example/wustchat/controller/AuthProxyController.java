package com.example.wustchat.controller;

import com.example.wustchat.service.AuthProxyService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthProxyController {
    private final AuthProxyService authProxyService;

    public AuthProxyController(AuthProxyService authProxyService) {
        this.authProxyService = authProxyService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody JsonNode requestBody) {
        return authProxyService.login(requestBody);
    }
}
