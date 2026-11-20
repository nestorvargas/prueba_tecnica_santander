package com.santander.base.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.santander.base.config.SeguridadConfig;
import com.santander.base.dto.TokenResponse;
import com.santander.base.service.JwtService;

@WebMvcTest(AuthController.class)
@Import(SeguridadConfig.class)
class AuthControllerWebMvcTest {

    private static final String LOGIN_VALIDO = """
            {"usuario":"admin","clave":"admin123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void login_credencialesValidas_devuelveElTokenDelServicio() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated("admin", null, List.of()));
        when(jwtService.generarToken(any())).thenReturn(new TokenResponse("abc.def.ghi", "Bearer", 3600));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc.def.ghi"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").value(3600));
    }

    @Test
    void login_credencialesInvalidas_responde401YNoGeneraToken() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Usuario o clave incorrectos"));

        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void login_sinUsuario_responde400YNoAutentica() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clave\":\"admin123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("usuario")));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_cuerpoQueNoEsJson_responde400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("no-es-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_conJwt_devuelveUsuarioYPermisosDelToken() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(jwt().jwt(token -> token.subject("ana").claim("scope", "USER ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("ana"))
                .andExpect(jsonPath("$.permisos").value("USER ADMIN"));
    }

    @Test
    void me_sinToken_responde401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
