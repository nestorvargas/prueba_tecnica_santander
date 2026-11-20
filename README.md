# Desarrollo prueba tecnica Novatec

Proyecto base de Spring Boot para empezar a desarrollar una API REST.

Stack: Java 17, Spring Boot 3.5, Maven, JUnit 5.

## Cómo ejecutarlo

Requisitos: JDK 17 o superior y Maven.

```bash
# Tests
mvn clean test

# Aplicación en http://localhost:8080
mvn spring-boot:run
```

Con `spring-boot-devtools` la aplicación se reinicia sola al recompilar desde el IDE.

En IntelliJ: clic derecho en `pom.xml` → *Add as Maven Project*, y ejecutar `BaseApiApplication`.

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| GET | `/api/ping` | Comprueba que la API responde | 200 |
| GET | `/actuator/health` | Estado de la aplicación | 200 |
| GET | `/actuator/info` | Información de la aplicación | 200 |

```bash
curl http://localhost:8080/api/ping
# {"estado":"ok"}
```

## Estructura

```
src/main/java/com/santander/base/
├── BaseApiApplication.java   Clase principal
├── controller/               Endpoints REST
├── service/                  Lógica de negocio
├── repository/               Acceso a datos
├── model/                    Entidades y enums
├── dto/                      Objetos de entrada y salida de la API
└── exception/                Excepciones y GlobalExceptionHandler
src/main/resources/
└── application.yml           Configuración
src/test/java/com/santander/base/
└── BaseApiApplicationTests.java
```

## Manejo de errores

`GlobalExceptionHandler` devuelve los errores en formato `ProblemDetail` (RFC 9457):

| Excepción | Código |
|-----------|--------|
| `RecursoNoEncontradoException` | 404 |
| `MethodArgumentNotValidException` (falla `@Valid`) | 400 |

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No existe el recurso 1",
  "instance": "/api/recursos/1"
}
```

## Dependencias

| Dependencia | Para qué |
|-------------|----------|
| `spring-boot-starter-web` | API REST con Tomcat embebido |
| `spring-boot-starter-validation` | Validación con `@Valid`, `@NotBlank`, etc. |
| `spring-boot-starter-actuator` | Endpoints de salud y monitoreo |
| `spring-boot-devtools` | Reinicio automático en desarrollo |
| `spring-boot-starter-test` | JUnit 5, Mockito, MockMvc |
