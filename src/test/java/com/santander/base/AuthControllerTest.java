package com.santander.base;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Flujo completo: login real con el usuario de application.yml y uso del token emitido.
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_credencialesValidas_devuelveToken() throws Exception {
        mockMvc.perform(login("admin", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").value(3600));
    }

    @Test
    void login_claveIncorrecta_responde401() throws Exception {
        mockMvc.perform(login("admin", "otra"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Usuario o clave incorrectos"));
    }

    @Test
    void login_camposVacios_responde400() throws Exception {
        mockMvc.perform(login("", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_sinToken_responde401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_tokenMalFormado_responde401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer token.no.valido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_tokenDelLogin_devuelveUsuario() throws Exception {
        String respuesta = mockMvc.perform(login("admin", "admin123"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(respuesta);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + json.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.permisos").value("USER"));
    }

    private RequestBuilder login(String usuario, String clave) throws Exception {
        String cuerpo = objectMapper.writeValueAsString(
                Map.of("usuario", usuario, "clave", clave));
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }
}
