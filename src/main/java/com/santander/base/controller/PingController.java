package com.santander.base.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Salud")
@RestController
@RequestMapping("/api")
public class PingController {

    @Operation(summary = "Comprueba que la API responde")
    @SecurityRequirements
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("estado", "ok");
    }
}
