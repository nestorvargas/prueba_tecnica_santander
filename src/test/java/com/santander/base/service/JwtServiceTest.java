package com.santander.base.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.santander.base.config.SeguridadProperties;
import com.santander.base.dto.TokenResponse;

class JwtServiceTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres";

    private JwtService jwtService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void configurar() {
        SecretKey clave = clave(SECRETO);
        SeguridadProperties propiedades = new SeguridadProperties(SECRETO, Duration.ofMinutes(30), "admin", "admin123");
        jwtService = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<>(clave)), propiedades);
        jwtDecoder = decoder(clave);
    }

    @Test
    void generarToken_usuarioAutenticado_devuelveTokenBearerConExpiracionConfigurada() {
        TokenResponse respuesta = jwtService.generarToken(autenticacion("ana", "USER"));

        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.expiraEn()).isEqualTo(1800);
        assertThat(respuesta.token()).isNotBlank();
    }

    @Test
    void generarToken_tokenGenerado_traeUsuarioEmisorYVigencia() {
        Jwt jwt = jwtDecoder.decode(jwtService.generarToken(autenticacion("ana", "USER")).token());

        assertThat(jwt.getSubject()).isEqualTo("ana");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("base-api");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
    }

    @Test
    void generarToken_variosPermisos_losUneEnScopeSeparadosPorEspacio() {
        Jwt jwt = jwtDecoder.decode(jwtService.generarToken(autenticacion("ana", "USER", "ADMIN")).token());

        assertThat(jwt.getClaimAsString("scope")).isEqualTo("USER ADMIN");
    }

    @Test
    void generarToken_decodificadoConOtroSecreto_esRechazado() {
        String token = jwtService.generarToken(autenticacion("ana", "USER")).token();
        JwtDecoder otroDecoder = decoder(clave("otro-secreto-distinto-con-mas-de-32-caracteres"));

        assertThatThrownBy(() -> otroDecoder.decode(token)).isInstanceOf(JwtException.class);
    }

    private static Authentication autenticacion(String usuario, String... permisos) {
        List<SimpleGrantedAuthority> autoridades = Arrays.stream(permisos)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return UsernamePasswordAuthenticationToken.authenticated(usuario, null, autoridades);
    }

    private static SecretKey clave(String secreto) {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static JwtDecoder decoder(SecretKey clave) {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
