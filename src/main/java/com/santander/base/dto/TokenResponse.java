package com.santander.base.dto;

public record TokenResponse(
        String token,
        String tipo,
        long expiraEn) {
}
