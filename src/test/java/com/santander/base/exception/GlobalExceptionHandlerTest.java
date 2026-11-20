package com.santander.base.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void manejarNoEncontrado_devuelve404ConElMensajeDeLaExcepcion() {
        ProblemDetail problema = handler.manejarNoEncontrado(new RecursoNoEncontradoException("No existe el recurso 7"));

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problema.getDetail()).isEqualTo("No existe el recurso 7");
    }

    @Test
    void manejarAutenticacion_claveIncorrecta_devuelve401ConMensajeGenerico() {
        ProblemDetail problema = handler.manejarAutenticacion(new BadCredentialsException("Bad credentials"));

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problema.getDetail()).isEqualTo("Usuario o clave incorrectos");
    }

    @Test
    void manejarAutenticacion_otraCausa_noRevelaElMotivoReal() {
        ProblemDetail problema = handler.manejarAutenticacion(new DisabledException("Usuario deshabilitado"));

        assertThat(problema.getDetail()).isEqualTo("Usuario o clave incorrectos");
    }

    @Test
    void manejarValidacion_variosCamposInvalidos_losListaTodosEnElDetalle() throws Exception {
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "request");
        resultado.addError(new FieldError("request", "usuario", "no debe estar vacío"));
        resultado.addError(new FieldError("request", "clave", "no debe estar vacío"));
        MethodParameter parametro = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("metodoDePrueba", String.class), 0);

        ProblemDetail problema = handler.manejarValidacion(new MethodArgumentNotValidException(parametro, resultado));

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problema.getDetail()).isEqualTo("usuario: no debe estar vacío, clave: no debe estar vacío");
    }

    @SuppressWarnings("unused")
    private void metodoDePrueba(String argumento) {
    }
}
