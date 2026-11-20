package com.santander.base.service;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.santander.base.config.SeguridadProperties;
import com.santander.base.dto.TokenResponse;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final SeguridadProperties propiedades;

    public JwtService(JwtEncoder jwtEncoder, SeguridadProperties propiedades) {
        this.jwtEncoder = jwtEncoder;
        this.propiedades = propiedades;
    }

    public TokenResponse generarToken(Authentication autenticacion) {
        Instant ahora = Instant.now();
        long expiraEn = propiedades.jwtExpiracion().toSeconds();
        String permisos = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("base-api")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(expiraEn))
                .subject(autenticacion.getName())
                // "scope" lo lee el resource server y lo convierte en autoridades SCOPE_*
                .claim("scope", permisos)
                .build();

        // Sin cabecera explícita Nimbus intenta firmar con RS256
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiraEn);
    }
}
