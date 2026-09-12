# Tabletop Manager

Aplicación web desarrollada como Trabajo de Fin de Grado para facilitar la gestión de partidas, listas de ejército y comunidades de juegos de miniaturas.

Actualmente permite trabajar con **Warhammer 40.000** y **Age of Sigmar**.

## Funcionalidades principales

- Registro, inicio de sesión y edición del perfil de usuario.
- Consulta de catálogos de facciones, ejércitos y unidades.
- Creación y almacenamiento de listas de ejército.
- Exportación de listas compatibles a PDF.
- Seguimiento de partidas por rondas, misiones y puntuaciones.
- Historial de partidas con filtros por juego, fecha y resultado.
- Estadísticas personales de victorias, derrotas y facciones utilizadas.
- Creación de comunidades, gestión de miembros y organización de eventos.
- Panel de administración para gestionar usuarios y comunidades.

## Tecnologías utilizadas

- Java 17.
- Spring Boot 4.
- Spring MVC y JSP/JSTL para la interfaz web.
- Spring Data JPA e Hibernate.
- MySQL 8.
- Maven.
- Apache PDFBox para generar documentos PDF.
- BCrypt para almacenar las contraseñas de forma segura.

## Estructura del proyecto

```text
src/main/java        Código Java: controladores, servicios, repositorios y entidades
src/main/resources   Configuración, catálogos, imágenes, CSS y JavaScript
src/main/webapp      Vistas JSP
src/test/java        Pruebas automatizadas
BaseDatosTabletopManager.sql  Esquema completo para crear la base de datos
sql                           Migraciones para instalaciones existentes
```

La aplicación sigue una organización por capas:

- `controller`: recibe las peticiones HTTP y prepara las vistas.
- `service`: contiene las reglas y procesos de negocio.
- `dao`: contiene los repositorios y consultas de acceso a datos.
- `entity`: representa las tablas de la base de datos mediante JPA.

## Requisitos para ejecutar el proyecto

- JDK 17.
- MySQL 8 en ejecución.
- Maven 3.9 o el wrapper incluido en el repositorio.
- Conexión a Internet para descargar inicialmente los catálogos externos de los juegos.

## Preparación de la base de datos

Para crear una instalación nueva, hay que ejecutar el archivo situado en la raíz del proyecto:

```text
BaseDatosTabletopManager.sql
```

El script crea la base de datos `tfgenrique`, sus tablas, los datos básicos de los sistemas de juego y una cuenta de administración inicial:

```text
Usuario: admin
Contraseña: admin
```

Los scripts de `sql/migrations` deben aplicarse en orden cuando se actualiza una base de datos existente.

## Configuración

Las credenciales de MySQL no se guardan en el repositorio. Hay que copiar `.env.example` como `.env` en la raíz del proyecto y completar sus valores:

```properties
TFG_DB_URL=jdbc:mysql://localhost:3306/tfgenrique?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci
TFG_DB_USERNAME=usuario_mysql
TFG_DB_PASSWORD=contrasena_mysql
```

## Ejecución

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

Una vez iniciada, la aplicación está disponible en:

```text
http://localhost:8080
```

## Pruebas

Para ejecutar las pruebas automatizadas:

```powershell
.\mvnw.cmd test
```

Las pruebas utilizan una base de datos H2 aislada cuando corresponde, por lo que no modifican los datos almacenados en MySQL.

## Autor

Enrique Silveira García — Trabajo de Fin de Grado
