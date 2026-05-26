# BackGestion

Backend Spring Boot para la gestion academica de un colegio. Expone una API REST para usuarios, docentes, estudiantes, notas, asistencias, evaluaciones, anotaciones y entidades academicas relacionadas.

## Stack actual

- Java 17
- Spring Boot 3.2.5
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Spring Web
- Spring Data JPA
- PostgreSQL
- Springdoc OpenAPI / Swagger UI
- Soporte adicional para AWS Lambda mediante `aws-serverless-java-container-springboot3`

## Arquitectura y patrones usados

El proyecto sigue una organizacion por capas:

- `Controller`: expone endpoints REST.
- `Services`: concentra logica de negocio y coordinacion.
- `Repository`: acceso a datos con Spring Data JPA y consultas nativas.
- `Model`: entidades del dominio.
- `dto`: projections y contratos usados para respuestas o autenticacion.
- `Config`: configuraciones transversales, como CORS.

Patrones visibles en el codigo:

- Arquitectura por capas `Controller -> Service -> Repository -> Model`
- Inyeccion de dependencias con Spring
- Repositories basados en `JpaRepository`
- DTO/Projection pattern para consultas optimizadas
- Configuracion externalizada mediante variables de entorno y perfiles

## Estructura del proyecto

```text
src/
  main/
    java/com/example/BackGestion/
      Config/
      Controller/
      dto/
      Model/
      Repository/
      Services/
      BackGestionApplication.java
      StreamLambdaHandler.java
      SwaggerConfig.java
    resources/
      application.properties
      application-local.properties
  test/
    java/com/example/BackGestion/
      BackGestionApplicationTests.java
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

## Requisitos previos

Para ejecutar el proyecto localmente necesitas:

- JDK 17
- Docker Desktop si quieres correrlo en contenedores
- Una base de datos PostgreSQL accesible

No es obligatorio instalar Maven globalmente, porque el repositorio ya incluye Maven Wrapper.

## Configuracion

La aplicacion usa variables de entorno para conectarse a PostgreSQL:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
```

Puedes tomar como base el archivo [.env.example](/C:/Users/marti/Desktop/fullstack3/ms_gestion/.env.example).

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

Actualmente el proyecto incluye una prueba base de contexto Spring (`contextLoads`), por lo que conviene complementar con pruebas funcionales a medida que evolucione la API.

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
- CORS: actualmente abierto a cualquier origen (`*`)
- Base de datos principal: PostgreSQL
- H2 esta declarada como dependencia runtime, pero la configuracion activa del proyecto apunta a PostgreSQL

## Soporte para AWS Lambda

Existe la clase `StreamLambdaHandler`, lo que indica preparacion para ejecutar la app sobre AWS Lambda usando el adaptador serverless de Spring Boot. Aun asi, el flujo principal del repositorio hoy esta orientado a ejecutarse como API Spring Boot tradicional y tambien dentro de Docker.

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

Este README fue alineado al estado actual del repositorio y del `Dockerfile`. Si luego se agrega `docker-compose.yml`, migraciones SQL o nuevos perfiles Spring, conviene actualizar tambien esta guia operativa.
