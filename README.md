# BackGestion

Backend Spring Boot para la gestion academica de un colegio. Expone una API REST para usuarios, docentes, estudiantes, notas, asistencias, evaluaciones, anotaciones y entidades academicas relacionadas.

## Stack actual

- Java 17
- Spring Boot 3.2.5
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Spring Web
- Spring Data JPA
- PostgreSQL (H2 declarada como dependencia runtime, sin uso activo)
- Springdoc OpenAPI / Swagger UI
- Autenticacion por JWT (validacion propia, sin Spring Security completo) con `spring-security-crypto` para BCrypt
- Mensajeria dual: RabbitMQ (`spring-boot-starter-amqp`) en `dev` y AWS SQS (`software.amazon.awssdk:sqs`) en `prod`
- JaCoCo para cobertura de pruebas
- Despliegue como contenedor Docker en AWS ECS Fargate (imagen generada por el `Dockerfile` del repo)
- Soporte adicional (no es el flujo principal de despliegue) para AWS Lambda mediante `StreamLambdaHandler` / `aws-serverless-java-container-springboot3`

## Arquitectura y patrones usados

El proyecto sigue una organizacion por capas:

- `Controller`: expone endpoints REST.
- `Services`: concentra logica de negocio y coordinacion.
- `Repository`: acceso a datos con Spring Data JPA y consultas nativas.
- `Model`: entidades del dominio.
- `dto`: projections y contratos usados para respuestas o autenticacion.
- `Config`: configuraciones transversales, como CORS, RabbitMQ y SQS.
- `Security`: filtro y utilidades de validacion de JWT.
- `Util`: utilidades de dominio (por ejemplo, validacion/formateo de RUT).

Patrones visibles en el codigo:

- Arquitectura por capas `Controller -> Service -> Repository -> Model`
- Inyeccion de dependencias con Spring
- Repositories basados en `JpaRepository`
- DTO/Projection pattern para consultas optimizadas
- Configuracion externalizada mediante variables de entorno y perfiles
- Filtro de autenticacion (`OncePerRequestFilter`) para validar JWT en cada request
- Mensajeria dual por perfil de entorno (RabbitMQ en `dev`, SQS en `prod`)

## Estructura del proyecto

```text
src/
  main/
    java/com/example/BackGestion/
      Config/
        CorsConfig.java
        RabbitMQConfig.java
        SqsConfig.java
      Controller/
      dto/
      Model/
      Repository/
      Security/
        JwtAuthFilter.java
        JwtUtil.java
        JwtValidationException.java
      Services/
      Util/
        RutValidator.java
      BackGestionApplication.java
      StreamLambdaHandler.java
      SwaggerConfig.java
    resources/
      application.properties
      application-local.properties
  test/
    java/com/example/BackGestion/
      BackGestionApplicationTests.java
      Services/
        NotaServiceTest.java
Dockerfile
pom.xml
.env.example
```

## Modulos principales de la API

- `/api/usuarios`
  - incluye autenticacion en `/api/usuarios/login`
- `/api/docentes`
- `/api/estudiantes`
- `/api/notas`
- `/api/asistencias`
- `/api/evaluaciones`
- `/api/anotaciones`
- `/api/academico`
  - cursos
  - asignaturas
  - relacion curso-asignatura-docente (CAD)

La documentacion interactiva queda disponible en:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Autenticacion (JWT)

Todas las rutas requieren un JWT valido en el header `Authorization: Bearer <token>`, excepto:

- `/api/usuarios/login`
- `/swagger-ui/**`, `/swagger-ui.html`
- `/v3/api-docs/**`, `/api-docs/**`
- Peticiones `OPTIONS` (preflight CORS)

El token se valida con `JwtAuthFilter` + `JwtUtil` usando el secreto `JWT_SECRET`; este servicio **valida** el JWT pero no lo emite (se asume que lo emite otro servicio de autenticacion). Si el token falta o es invalido, la API responde `401` con un JSON `{"error": "..."}`.

## Requisitos previos

Para ejecutar el proyecto localmente necesitas:

- JDK 17
- Docker Desktop si quieres correrlo en contenedores
- Una base de datos PostgreSQL accesible

No es obligatorio instalar Maven globalmente, porque el repositorio ya incluye Maven Wrapper.

## Configuracion

La aplicacion se configura enteramente por variables de entorno (ver `src/main/resources/application.properties`):

| Variable | Default si no se define | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/colegio` | conexion a PostgreSQL |
| `DB_USERNAME` | `postgres` | usuario de PostgreSQL |
| `DB_PASSWORD` | `secure-key` | password de PostgreSQL |
| `JWT_SECRET` | `default-secret` | secreto para validar los JWT recibidos |
| `RABBITMQ_HOST` | `localhost` | host de RabbitMQ (usado cuando `ENVIRONMENT=dev`) |
| `RABBITMQ_PORT` | `5672` | puerto de RabbitMQ |
| `RABBITMQ_USER` | `admin` | usuario de RabbitMQ |
| `RABBITMQ_PASS` | `admin123` | password de RabbitMQ |
| `ENVIRONMENT` | `dev` | selecciona el canal de mensajeria: `dev` → RabbitMQ, `prod` → SQS |
| `SQS_QUEUE_URL` | *(vacio)* | URL de la cola SQS, requerida cuando `ENVIRONMENT=prod` |

En `dev`, los eventos de notas se publican en RabbitMQ (exchange `gestion.exchange`, queue `gestion.queue`, routing key `gestion.eventos`). En `prod`, se publican en la cola SQS indicada por `SQS_QUEUE_URL`; el bean `SqsClient` (`Config/SqsConfig.java`) solo se crea cuando `ENVIRONMENT=prod`, y usa las credenciales/rol de AWS del entorno de ejecucion (no hay `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY` explicitas en el proyecto: se espera un rol IAM, por ejemplo el task role de ECS).

Puedes tomar como base el archivo [.env.example](.env.example) (actualmente solo cubre `DB_*` y `JWT_SECRET`; complementa con las variables de RabbitMQ/SQS segun el entorno).

### CORS

`Config/CorsConfig.java` restringe los origenes permitidos (no esta abierto a `*`):

- `http://localhost:4200`
- `https://martin-romero.cl`
- `https://*.martin-romero.cl`

Metodos permitidos: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`. Si se agrega un nuevo frontend/dominio, hay que sumarlo ahi.

### Importante sobre la base de datos

La configuracion actual tiene:

- `spring.jpa.hibernate.ddl-auto=none`

Eso significa que la aplicacion no crea ni actualiza el esquema automaticamente. Antes de levantar el backend, la base de datos debe existir y tener el esquema/tablas esperadas por la aplicacion.

### Perfil local

Existe un perfil local en `src/main/resources/application-local.properties` con una conexion orientada a:

```properties
jdbc:postgresql://localhost:5432/colegio
```

Si usas este perfil, ajusta usuario, password y base segun tu entorno.

## Instalacion y ejecucion local

### 1. Clonar y entrar al proyecto

```powershell
git clone <url-del-repo>
cd ms_gestion
```

### 2. Configurar variables de entorno

En PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/colegio"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_password"
```

Opcionalmente puedes apoyarte en `.env.example` como referencia.

### 3. Ejecutar la aplicacion

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

### 4. Ejecutar con perfil local

En Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

En Linux/macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Compilacion y pruebas

Construir el artefacto:

```powershell
.\mvnw.cmd clean package
```

Ejecutar pruebas:

```powershell
.\mvnw.cmd test
```

Actualmente el proyecto incluye pruebas unitarias de lógica de negocio (JUnit 5 + Mockito) para `NotaService`, además de la prueba base de contexto de Spring. Se desarrollaron las siguientes pruebas específicas para validar el flujo crítico de gestión de notas:

1. **`guardarBulkYPublicar_EmptyList_ReturnsEmpty`**: Valida que al intentar guardar una lista vacía de notas, el sistema responda eficientemente sin interactuar con la base de datos ni los servicios de mensajería.
2. **`guardarBulkYPublicar_DevEnvironment_SavesAndPublishesToRabbitMQ`**: Prueba el escenario de éxito en el entorno de desarrollo (`dev`), verificando que las notas se guarden correctamente, se recuperen los datos del estudiante y se publique el evento de notificación en **RabbitMQ** (asegurando que SQS no sea invocado).
3. **`guardarOActualizarNota_ExistingNote_UpdatesValue`**: Valida que si se intenta registrar una nota para un estudiante y evaluación que ya existen, el sistema actualice el registro existente en lugar de duplicarlo.

Para correr específicamente estas pruebas unitarias de negocio, puedes usar:

```powershell
.\mvnw.cmd test -Dtest=NotaServiceTest
```

## Ejecucion con Docker

El repositorio ya incluye un `Dockerfile` multi-stage:

- etapa 1: compila el proyecto con Maven y Java 17
- etapa 2: genera una imagen liviana con JRE 17 y ejecuta el `.jar`

### Opcion 1: usar una base PostgreSQL externa

Construir imagen:

```powershell
docker build -t backgestion:latest .
```

Ejecutar contenedor:

```powershell
docker run --rm -p 8080:8080 `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/colegio" `
  -e DB_USERNAME="postgres" `
  -e DB_PASSWORD="tu_password" `
  backgestion:latest
```

Notas:

- En Docker Desktop para Windows/Mac, `host.docker.internal` permite llegar a una base que corre en tu maquina anfitriona.
- Si la base corre en otro servidor, reemplaza el host en `DB_URL`.

### Opcion 2: app y PostgreSQL en la misma red Docker

Crear red:

```powershell
docker network create backgestion-net
```

Levantar PostgreSQL:

```powershell
docker run -d --name backgestion-db `
  --network backgestion-net `
  -e POSTGRES_DB=colegio `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=tu_password `
  -p 5432:5432 `
  postgres:16
```

Levantar la API:

```powershell
docker build -t backgestion:latest .

docker run --rm --name backgestion-api `
  --network backgestion-net `
  -p 8080:8080 `
  -e DB_URL="jdbc:postgresql://backgestion-db:5432/colegio" `
  -e DB_USERNAME="postgres" `
  -e DB_PASSWORD="tu_password" `
  backgestion:latest
```

### Verificacion

Una vez arriba, valida con:

```powershell
curl http://localhost:8080/swagger-ui.html
```

o abre directamente:

- `http://localhost:8080/swagger-ui.html`

## Variables y comportamiento relevantes

- Puerto HTTP: `8080`
- Todas las rutas requieren JWT salvo login/Swagger/OpenAPI/OPTIONS (ver [Autenticacion (JWT)](#autenticacion-jwt))
- CORS restringido a origenes especificos, no abierto a `*` (ver seccion [CORS](#cors))
- Base de datos principal: PostgreSQL, con `spring.jpa.hibernate.ddl-auto=none` (no crea/actualiza esquema)
- H2 esta declarada como dependencia runtime, pero la configuracion activa del proyecto apunta a PostgreSQL
- Mensajeria dual segun `ENVIRONMENT` (`dev` → RabbitMQ, `prod` → SQS)

## Despliegue en AWS ECS Fargate

El despliegue objetivo de este servicio es una **task de ECS sobre Fargate**, construida a partir del `Dockerfile` del repo (imagen basada en `eclipse-temurin:17-jre`, expone el puerto `8080`).

Puntos a tener en cuenta al definir la task definition / servicio:

- **Container port**: `8080`, mapeado normalmente detras de un Application Load Balancer.
- **Variables de entorno / secretos**: definir en la task definition (idealmente `DB_PASSWORD` y `JWT_SECRET` como `secrets` desde AWS Secrets Manager o SSM Parameter Store, no como `environment` en texto plano):
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET`
  - `ENVIRONMENT=prod` (para que la app use SQS en vez de RabbitMQ)
  - `SQS_QUEUE_URL`
- **Task role (IAM)**: el bean `SqsClient` usa credenciales por defecto de la cadena de AWS SDK, por lo que el *task role* de ECS necesita permisos de `sqs:SendMessage` (y `sqs:GetQueueAttributes` si aplica) sobre la cola indicada en `SQS_QUEUE_URL`.
- **Networking**: la task necesita alcanzar la instancia de PostgreSQL (RDS u otra) en la VPC/subnets configuradas; si `ENVIRONMENT=dev` tambien necesitaria alcanzar RabbitMQ, pero en `prod` no es necesario.
- **Health check**: el proyecto no incluye Spring Boot Actuator, por lo que como health check (ALB target group o `HEALTHCHECK` del contenedor) se puede usar una ruta publica existente, por ejemplo `GET /api-docs` o `GET /swagger-ui.html`.
- **Build/push de imagen**: `docker build -t backgestion:latest .` y luego `docker push` al repositorio ECR que consuma la task definition (no hay pipeline de CI/CD ni `buildspec.yml` en este repo todavia).

## Soporte para AWS Lambda

Existe la clase `StreamLambdaHandler`, lo que indica preparacion para ejecutar la app sobre AWS Lambda usando el adaptador serverless de Spring Boot. Este camino queda como alternativa; el despliegue principal hoy es como contenedor Docker en ECS Fargate (tambien se puede correr localmente como API Spring Boot tradicional o con Docker Compose manual, ver seccion [Ejecucion con Docker](#ejecucion-con-docker)).

## Recomendaciones para desarrollo

- Mantener la separacion por capas ya establecida
- Agregar nuevas consultas complejas en `Repository` usando projections/DTO cuando sea necesario
- Evitar meter logica de negocio en los controladores
- Documentar endpoints nuevos con OpenAPI/Swagger para mantener la API navegable

## Troubleshooting rapido

### Error de conexion a base de datos

Revisa:

- que `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` esten definidos
- que PostgreSQL este accesible desde donde corre la app
- que la base `colegio` exista
- que el esquema/tablas ya hayan sido creados

### La app corre local pero no en Docker

Revisa especialmente el host usado en `DB_URL`:

- local fuera de Docker: normalmente `localhost`
- app dentro de Docker conectando al host: `host.docker.internal`
- app dentro de Docker conectando a otro contenedor: nombre del contenedor o servicio, por ejemplo `backgestion-db`

### Swagger no abre

Confirma que la app haya iniciado sin errores y luego prueba:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api-docs`

## Estado actual de documentacion

Este README fue alineado al estado actual del repositorio (config de PostgreSQL, JWT, mensajeria RabbitMQ/SQS, CORS) y del `Dockerfile`, con el despliegue objetivo en AWS ECS Fargate. No existe todavia en el repo una task definition, `docker-compose.yml`, migraciones SQL ni pipeline de CI/CD; si se agregan, conviene actualizar tambien esta guia operativa.
