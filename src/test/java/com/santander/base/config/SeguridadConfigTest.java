package com.santander.base.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Prueba la cadena de seguridad completa con tokens reales: qué rutas son públicas y qué tokens se rechazan.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void health_sinToken_esPublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void info_sinToken_responde401() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void info_conTokenValido_responde200() throws Exception {
        String token = firmar(jwtEncoder, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/actuator/info").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void rutaProtegida_tokenExpirado_responde401() throws Exception {
        String token = firmar(jwtEncoder, Instant.now().minusSeconds(120));

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaProtegida_tokenFirmadoConOtroSecreto_responde401() throws Exception {
        JwtEncoder otroEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(
                "un-secreto-distinto-con-mas-de-32-caracteres".getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        String token = firmar(otroEncoder, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaProtegida_esquemaDistintoDeBearer_responde401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4xMjM="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaInexistente_sinToken_responde401SinRevelarQueNoExiste() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
                .andExpect(status().isUnauthorized());
    }

    private static String firmar(JwtEncoder encoder, Instant expiracion) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("admin")
                .issuedAt(expiracion.minusSeconds(3600))
                .expiresAt(expiracion)
                .claim("scope", "USER")
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
