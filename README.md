# Desarrollo prueba tecnica Novatec

Proyecto base de Spring Boot para empezar a desarrollar una API REST.

Stack: Java 17, Spring Boot 3.5, Spring Security con JWT, Maven, JUnit 5.

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

| Método | Ruta | Descripción | Token | Respuestas |
|--------|------|-------------|-------|------------|
| POST | `/api/auth/login` | Devuelve un token JWT | No | 200, 400, 401 |
| GET | `/api/auth/me` | Usuario y permisos del token | Sí | 200, 401 |
| GET | `/api/ping` | Comprueba que la API responde | No | 200 |
| GET | `/actuator/health` | Estado de la aplicación | No | 200 |
| GET | `/actuator/info` | Información de la aplicación | Sí | 200, 401 |

Cualquier endpoint nuevo exige token, salvo que se agregue a la lista pública en `SeguridadConfig`.

## Autenticación JWT

La API no guarda sesión: el cliente pide un token en `/api/auth/login` y lo envía en cada petición en la cabecera `Authorization: Bearer <token>`.

- El token se firma con HS256 usando el secreto `app.seguridad.jwt-secreto` (mínimo 32 caracteres).
- Dura 1 hora por defecto (`app.seguridad.jwt-expiracion`).
- Trae el usuario en `sub` y sus permisos en `scope`. Spring los convierte en autoridades `SCOPE_USER`, que se pueden exigir con `hasAuthority("SCOPE_USER")`.
- Por ahora hay un único usuario en memoria (`admin` / `admin123`). Cuando exista base de datos se reemplaza el `UserDetailsService` de `SeguridadConfig` por uno que la consulte.

```bash
# 1. Obtener el token
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"admin","clave":"admin123"}'
# {"token":"eyJhbGciOiJIUzI1NiJ9...","tipo":"Bearer","expiraEn":3600}

# 2. Usarlo
curl http://localhost:8080/api/auth/me -H "Authorization: Bearer <token>"
# {"usuario":"admin","permisos":"USER"}
```

Variables de entorno para no usar los valores de desarrollo:

| Variable | Valor por defecto |
|----------|-------------------|
| `JWT_SECRET` | clave de desarrollo incluida en `application.yml` |
| `JWT_EXPIRACION` | `1h` |
| `APP_USUARIO` | `admin` |
| `APP_CLAVE` | `admin123` |

## Estructura

```
src/main/java/com/santander/base/
├── BaseApiApplication.java   Clase principal
├── config/                   Seguridad y JWT (SeguridadConfig, SeguridadProperties)
├── controller/               Endpoints REST
├── service/                  Lógica de negocio
├── repository/               Acceso a datos
├── model/                    Entidades y enums
├── dto/                      Objetos de entrada y salida de la API
└── exception/                Excepciones y GlobalExceptionHandler
src/main/resources/
└── application.yml           Configuración
src/test/java/com/santander/base/
├── BaseApiApplicationTests.java     Arranque del contexto y /api/ping
└── AuthControllerTest.java          Flujo completo: login real y uso del token
```

## Manejo de errores

`GlobalExceptionHandler` devuelve los errores en formato `ProblemDetail` (RFC 9457):

| Excepción | Código |
|-----------|--------|
| `RecursoNoEncontradoException` | 404 |
| `AuthenticationException` (usuario o clave incorrectos en el login) | 401 |
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
| `spring-boot-starter-security` | Autenticación y autorización |
| `spring-boot-starter-oauth2-resource-server` | Firma y validación de tokens JWT (Nimbus) |
| `spring-boot-devtools` | Reinicio automático en desarrollo |
| `spring-boot-starter-test` | JUnit 5, Mockito, MockMvc |
| `spring-security-test` | Utilidades de prueba para Spring Security |
