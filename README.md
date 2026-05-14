# Microservicio de Gestión (BackGestion)

Este proyecto es un microservicio backend desarrollado en **Spring Boot** para el sistema de Libro de Clases (LibroClase_V3). Su propósito principal es gestionar la información académica y administrativa de un establecimiento educacional.

## Características Principales

El microservicio expone una API REST para administrar las siguientes entidades:

- **Usuarios**: Gestión de cuentas y roles de usuario.
- **Docentes**: Información y carga académica de los profesores.
- **Estudiantes**: Datos de los alumnos matriculados.
- **Asistencia**: Registro y control de asistencia de los estudiantes.
- **Evaluaciones y Notas**: Configuración de evaluaciones y registro de calificaciones.
- **Anotaciones**: Registro de observaciones y hoja de vida de los estudiantes.
- **Académico**: Gestión de cursos, asignaturas y períodos académicos.

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.5**
  - Spring Web
  - Spring Data JPA
- **PostgreSQL**: Base de datos principal.
- **H2 Database**: Base de datos en memoria para pruebas y desarrollo local.
- **Lombok**: Para reducir el código repetitivo (getters, setters, constructores).
- **Swagger / OpenAPI 3**: Documentación interactiva de la API.
- **AWS Serverless Java Container**: Soporte para despliegue serverless en AWS Lambda.

## Requisitos Previos

- Java Development Kit (JDK) 17 o superior.
- Maven 3.6+ (o puedes usar el Wrapper `./mvnw` incluido).
- Instancia de PostgreSQL en ejecución.

## Configuración

El proyecto utiliza un archivo `application.properties` para la configuración. Puedes sobrescribir las propiedades mediante variables de entorno.

### Base de Datos

Por defecto, la aplicación intentará conectarse a una base de datos local llamada `colegio`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/colegio}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:PossGAdmin}
```

Puedes cambiar estos valores estableciendo las variables de entorno `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` antes de ejecutar la aplicación.

## Ejecución Local

Para compilar y ejecutar el proyecto localmente, utiliza Maven Wrapper en la raíz del microservicio (`BackGestion/`):

### En Windows:
```cmd
mvnw.cmd spring-boot:run
```

### En Linux/macOS:
```bash
./mvnw spring-boot:run
```

El servidor se iniciará en el puerto **8080** por defecto.

## Documentación de la API (Swagger)

Una vez que la aplicación esté en ejecución, puedes acceder a la documentación interactiva de la API a través de Swagger UI:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Despliegue en AWS Lambda

El proyecto está configurado para ser desplegado como una función AWS Lambda.
1. Ejecuta `mvn clean package` para generar un archivo `jar` sombreado (`shaded`).
2. El archivo generado se ubicará en la carpeta `target/` y excluye el servidor Tomcat integrado, optimizado para el entorno serverless de AWS. El punto de entrada está manejado por la clase `StreamLambdaHandler.java`.

## Pruebas

Para ejecutar las pruebas unitarias y de integración (usando JUnit y Mockito):

```bash
mvnw.cmd test
```
