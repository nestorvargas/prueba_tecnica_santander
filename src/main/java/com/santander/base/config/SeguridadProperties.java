package com.santander.base.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seguridad")
public record SeguridadProperties(
        String jwtSecreto,
        Duration jwtExpiracion,
        String usuario,
        String clave) {
}
