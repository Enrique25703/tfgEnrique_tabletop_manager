# TFG Enrique

Aplicacion Spring Boot para gestionar usuarios, comunidades, partidas y listas de ejercito de Warhammer 40,000 y Age of Sigmar.

## Requisitos

- Java 17
- Maven 3.9 o el wrapper incluido
- MySQL 8

## Configuracion local

La aplicacion no guarda credenciales en el repositorio. Define estas variables en tu entorno o crea un fichero local `.env` a partir de `.env.example`:

```powershell
$env:TFG_DB_URL = "jdbc:mysql://localhost:3306/tfgenrique?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci"
$env:TFG_DB_USERNAME = "tu_usuario"
$env:TFG_DB_PASSWORD = "tu_contrasena"
```

Inicializa una instalacion nueva con `sql/CopiaSeguridadBaseDatos.sql`. Para bases ya existentes, ejecuta los scripts de `sql/migrations` en orden antes de desplegar.

## Ejecutar y validar

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

## Estructura

- `controller`: endpoints HTTP y preparacion de vistas JSP.
- `service`: reglas de negocio y casos de uso.
- `dao`: persistencia de entidades y modelos de lectura.
- `entity`: mapeos JPA del esquema relacional.
- `sql`: esquema inicial y migraciones incrementales.

Las consultas nativas necesarias para extraer listas almacenadas como JSON estan aisladas en `ListaEjercitoConsultaRepository`; los repositorios de entidades no incluyen logica de presentacion.
