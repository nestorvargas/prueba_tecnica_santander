package com.santander.base.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.santander.base.config.SeguridadConfig;

@WebMvcTest(PingController.class)
@Import(SeguridadConfig.class)
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ping_sinToken_responde200ConEstadoOk() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"));
    }
}
