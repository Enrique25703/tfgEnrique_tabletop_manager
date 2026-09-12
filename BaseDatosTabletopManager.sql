DROP DATABASE IF EXISTS `tfgenrique`;
CREATE DATABASE `tfgenrique`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `tfgenrique`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------------
-- Tablas base
-- ---------------------------------------------------------------------------

CREATE TABLE `usuarios` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `nombre_usuario` VARCHAR(50) NOT NULL,
  `email` VARCHAR(120) NOT NULL,
  `foto_url` VARCHAR(255) DEFAULT NULL,
  `contrasena_hash` VARCHAR(255) NOT NULL,
  `rol` VARCHAR(20) NOT NULL,
  `activo` TINYINT(1) NOT NULL,
  `creado_en` DATETIME NOT NULL,
  `actualizado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuarios_nombre_usuario` (`nombre_usuario`),
  UNIQUE KEY `uk_usuarios_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comunidades` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(120) NOT NULL,
  `logo_url` VARCHAR(255) DEFAULT NULL,
  `descripcion` TEXT DEFAULT NULL,
  `privacidad` VARCHAR(20) NOT NULL DEFAULT 'PUBLICA',
  `activo` TINYINT(1) NOT NULL,
  `creado_en` DATETIME NOT NULL,
  `actualizado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comunidades_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sistemas_juego` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(30) NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `edicion` VARCHAR(50) DEFAULT NULL,
  `activo` TINYINT(1) NOT NULL,
  `creado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sistemas_juego_codigo` (`codigo`),
  UNIQUE KEY `uk_sistemas_juego_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `eventos` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `organizador_usuario_id` BIGINT NOT NULL,
  `comunidad_id` BIGINT DEFAULT NULL,
  `sistema_juego_id` BIGINT NOT NULL,
  `titulo` VARCHAR(150) NOT NULL,
  `descripcion` LONGTEXT DEFAULT NULL,
  `tipo_evento` VARCHAR(20) NOT NULL,
  `sistema_clasificacion` VARCHAR(10) DEFAULT NULL,
  `rondas_planificadas` INT DEFAULT NULL,
  `max_participantes` INT DEFAULT NULL,
  `ubicacion` VARCHAR(150) DEFAULT NULL,
  `latitud` DECIMAL(10,7) DEFAULT NULL,
  `longitud` DECIMAL(10,7) DEFAULT NULL,
  `ciudad` VARCHAR(80) DEFAULT NULL,
  `fecha_limite_inscripcion` DATETIME DEFAULT NULL,
  `inicio_en` DATETIME NOT NULL,
  `fin_en` DATETIME DEFAULT NULL,
  `estado` VARCHAR(20) NOT NULL,
  `creado_en` DATETIME NOT NULL,
  `actualizado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_eventos_organizador_usuario` (`organizador_usuario_id`),
  KEY `idx_eventos_comunidad` (`comunidad_id`),
  KEY `idx_eventos_sistema_juego` (`sistema_juego_id`),
  CONSTRAINT `fk_eventos_organizador_usuario`
    FOREIGN KEY (`organizador_usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_eventos_comunidad`
    FOREIGN KEY (`comunidad_id`) REFERENCES `comunidades` (`id`),
  CONSTRAINT `fk_eventos_sistema_juego`
    FOREIGN KEY (`sistema_juego_id`) REFERENCES `sistemas_juego` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `fuentes_catalogo` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `sistema_juego_id` BIGINT NOT NULL,
  `nombre_fuente` VARCHAR(100) NOT NULL,
  `tipo_fuente` VARCHAR(30) NOT NULL,
  `url_repositorio` VARCHAR(255) NOT NULL,
  `rama_por_defecto` VARCHAR(100) NOT NULL,
  `activo` TINYINT(1) NOT NULL,
  `creado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fuentes_catalogo_sistema_url` (`sistema_juego_id`, `url_repositorio`),
  KEY `idx_fuentes_catalogo_sistema_juego` (`sistema_juego_id`),
  CONSTRAINT `fk_fuentes_catalogo_sistema_juego`
    FOREIGN KEY (`sistema_juego_id`) REFERENCES `sistemas_juego` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `listas_ejercito` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `propietario_usuario_id` BIGINT NOT NULL,
  `sistema_juego_id` BIGINT NOT NULL,
  `nombre` VARCHAR(150) NOT NULL,
  `nombre_faccion_snapshot` VARCHAR(120) DEFAULT NULL,
  `limite_puntos` INT NOT NULL,
  `puntos_actuales` INT NOT NULL,
  `visibilidad` VARCHAR(20) NOT NULL,
  `numero_version_actual` INT NOT NULL,
  `archivada` TINYINT(1) NOT NULL,
  `creado_en` DATETIME NOT NULL,
  `actualizado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_listas_ejercito_propietario_sistema_nombre` (`propietario_usuario_id`, `sistema_juego_id`, `nombre`),
  KEY `idx_listas_ejercito_propietario_usuario` (`propietario_usuario_id`),
  KEY `idx_listas_ejercito_sistema_juego` (`sistema_juego_id`),
  CONSTRAINT `fk_listas_ejercito_propietario_usuario`
    FOREIGN KEY (`propietario_usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_listas_ejercito_sistema_juego`
    FOREIGN KEY (`sistema_juego_id`) REFERENCES `sistemas_juego` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `versiones_lista_ejercito` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `lista_ejercito_id` BIGINT NOT NULL,
  `numero_version` INT NOT NULL,
  `fuente_catalogo_id` BIGINT NOT NULL,
  `commit_catalogo_hash` VARCHAR(100) DEFAULT NULL,
  `revision_catalogo` VARCHAR(50) DEFAULT NULL,
  `version_esquema_json` VARCHAR(30) DEFAULT NULL,
  `checksum_datos` VARCHAR(128) DEFAULT NULL,
  `puntos_totales` INT NOT NULL,
  `datos_lista` JSON NOT NULL,
  `creado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_versiones_lista_ejercito_lista_version` (`lista_ejercito_id`, `numero_version`),
  KEY `idx_versiones_lista_ejercito_lista` (`lista_ejercito_id`),
  KEY `idx_versiones_lista_ejercito_fuente` (`fuente_catalogo_id`),
  CONSTRAINT `fk_versiones_lista_ejercito_lista`
    FOREIGN KEY (`lista_ejercito_id`) REFERENCES `listas_ejercito` (`id`),
  CONSTRAINT `fk_versiones_lista_ejercito_fuente`
    FOREIGN KEY (`fuente_catalogo_id`) REFERENCES `fuentes_catalogo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `estados_catalogo_usuario` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `usuario_id` BIGINT NOT NULL,
  `sistema_juego_id` BIGINT NOT NULL,
  `fuente_catalogo_id` BIGINT NOT NULL,
  `ultima_comprobacion_en` DATETIME DEFAULT NULL,
  `ultima_sincronizacion_en` DATETIME DEFAULT NULL,
  `ultimo_commit_hash` VARCHAR(100) DEFAULT NULL,
  `estado_sincronizacion` VARCHAR(20) NOT NULL,
  `clave_cache_local` VARCHAR(255) DEFAULT NULL,
  `version_catalogo_local` VARCHAR(50) DEFAULT NULL,
  `mensaje_error` LONGTEXT DEFAULT NULL,
  `creado_en` DATETIME NOT NULL,
  `actualizado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_estados_catalogo_usuario_usuario_sistema` (`usuario_id`, `sistema_juego_id`),
  KEY `idx_estados_catalogo_usuario_sistema_juego` (`sistema_juego_id`),
  KEY `idx_estados_catalogo_usuario_fuente_catalogo` (`fuente_catalogo_id`),
  CONSTRAINT `fk_estados_catalogo_usuario_usuario`
    FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_estados_catalogo_usuario_sistema_juego`
    FOREIGN KEY (`sistema_juego_id`) REFERENCES `sistemas_juego` (`id`),
  CONSTRAINT `fk_estados_catalogo_usuario_fuente_catalogo`
    FOREIGN KEY (`fuente_catalogo_id`) REFERENCES `fuentes_catalogo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `afiliaciones_comunidad` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `comunidad_id` BIGINT NOT NULL,
  `usuario_id` BIGINT NOT NULL,
  `rol_comunidad` VARCHAR(20) NOT NULL,
  `estado_afiliacion` VARCHAR(20) NOT NULL,
  `unido_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_afiliacion_comunidad_usuario` (`comunidad_id`, `usuario_id`),
  KEY `idx_afiliaciones_comunidad_usuario` (`usuario_id`),
  CONSTRAINT `fk_afiliaciones_comunidad_comunidad`
    FOREIGN KEY (`comunidad_id`) REFERENCES `comunidades` (`id`),
  CONSTRAINT `fk_afiliaciones_comunidad_usuario`
    FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `solicitudes_comunidad` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `comunidad_id` BIGINT NOT NULL,
  `usuario_id` BIGINT NOT NULL,
  `estado` VARCHAR(20) NOT NULL,
  `fecha_solicitud` DATETIME NOT NULL,
  `resuelta_en` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_solicitudes_comunidad_usuario` (`comunidad_id`, `usuario_id`),
  KEY `idx_solicitudes_comunidad_estado` (`comunidad_id`, `estado`),
  KEY `idx_solicitudes_comunidad_usuario` (`usuario_id`),
  CONSTRAINT `fk_solicitudes_comunidad_comunidad`
    FOREIGN KEY (`comunidad_id`) REFERENCES `comunidades` (`id`),
  CONSTRAINT `fk_solicitudes_comunidad_usuario`
    FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `invitaciones_partida_comunidad` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `comunidad_id` BIGINT NOT NULL,
  `creador_usuario_id` BIGINT NOT NULL,
  `formato_juego` VARCHAR(30) NOT NULL,
  `lugar` VARCHAR(150) NOT NULL,
  `fecha_propuesta` DATETIME NOT NULL,
  `mensaje` LONGTEXT DEFAULT NULL,
  `estado` VARCHAR(20) NOT NULL,
  `creada_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_invitaciones_partida_comunidad_comunidad` (`comunidad_id`),
  KEY `idx_invitaciones_partida_comunidad_creador_usuario` (`creador_usuario_id`),
  CONSTRAINT `fk_invitaciones_partida_comunidad_comunidad`
    FOREIGN KEY (`comunidad_id`) REFERENCES `comunidades` (`id`),
  CONSTRAINT `fk_invitaciones_partida_comunidad_creador_usuario`
    FOREIGN KEY (`creador_usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inscripciones_evento` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `evento_id` BIGINT NOT NULL,
  `usuario_id` BIGINT NOT NULL,
  `version_lista_ejercito_id` BIGINT DEFAULT NULL,
  `nombre_lista_enviada` VARCHAR(150) DEFAULT NULL,
  `puntos_enviados` INT DEFAULT NULL,
  `estado_inscripcion` VARCHAR(20) NOT NULL,
  `inscrito_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inscripciones_evento_evento_usuario` (`evento_id`, `usuario_id`),
  KEY `idx_inscripciones_evento_usuario` (`usuario_id`),
  KEY `idx_inscripciones_evento_version_lista` (`version_lista_ejercito_id`),
  CONSTRAINT `fk_inscripciones_evento_evento`
    FOREIGN KEY (`evento_id`) REFERENCES `eventos` (`id`),
  CONSTRAINT `fk_inscripciones_evento_usuario`
    FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_inscripciones_evento_version_lista_ejercito`
    FOREIGN KEY (`version_lista_ejercito_id`) REFERENCES `versiones_lista_ejercito` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `partidas` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `evento_id` BIGINT DEFAULT NULL,
  `comunidad_id` BIGINT DEFAULT NULL,
  `creado_por_usuario_id` BIGINT NOT NULL,
  `sistema_juego_id` BIGINT NOT NULL,
  `numero_ronda` INT DEFAULT NULL,
  `numero_mesa` INT DEFAULT NULL,
  `jugador1_usuario_id` BIGINT NOT NULL,
  `jugador1_version_lista_id` BIGINT DEFAULT NULL,
  `jugador1_nombre_snapshot` VARCHAR(150) DEFAULT NULL,
  `jugador1_faccion_snapshot` VARCHAR(150) DEFAULT NULL,
  `jugador1_nombre_lista_snapshot` VARCHAR(150) DEFAULT NULL,
  `jugador1_puntos_snapshot` INT DEFAULT NULL,
  `jugador1_puntuacion_total` INT NOT NULL,
  `jugador2_usuario_id` BIGINT DEFAULT NULL,
  `jugador2_version_lista_id` BIGINT DEFAULT NULL,
  `jugador2_nombre_snapshot` VARCHAR(150) DEFAULT NULL,
  `jugador2_faccion_snapshot` VARCHAR(150) DEFAULT NULL,
  `jugador2_nombre_lista_snapshot` VARCHAR(150) DEFAULT NULL,
  `jugador2_puntos_snapshot` INT DEFAULT NULL,
  `jugador2_puntuacion_total` INT NOT NULL,
  `ganador_usuario_id` BIGINT DEFAULT NULL,
  `es_empate` TINYINT(1) NOT NULL,
  `nombre_mision` VARCHAR(150) DEFAULT NULL,
  `layout_mision` VARCHAR(50) DEFAULT NULL,
  `despliegue_mision` VARCHAR(150) DEFAULT NULL,
  `estilo_juego` VARCHAR(30) DEFAULT NULL,
  `jugador_defensor` VARCHAR(20) DEFAULT NULL,
  `jugador_primero` VARCHAR(20) DEFAULT NULL,
  `mostrar_command_points` TINYINT(1) DEFAULT NULL,
  `usar_cartas_giro` TINYINT(1) DEFAULT NULL,
  `notas` LONGTEXT DEFAULT NULL,
  `estado` VARCHAR(20) NOT NULL,
  `programada_en` DATETIME DEFAULT NULL,
  `iniciada_en` DATETIME DEFAULT NULL,
  `finalizada_en` DATETIME DEFAULT NULL,
  `creado_en` DATETIME NOT NULL,
  `actualizado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_partidas_evento` (`evento_id`),
  KEY `idx_partidas_comunidad` (`comunidad_id`),
  KEY `idx_partidas_creado_por_usuario` (`creado_por_usuario_id`),
  KEY `idx_partidas_sistema_juego` (`sistema_juego_id`),
  KEY `idx_partidas_jugador1_usuario` (`jugador1_usuario_id`),
  KEY `idx_partidas_jugador1_version_lista` (`jugador1_version_lista_id`),
  KEY `idx_partidas_jugador2_usuario` (`jugador2_usuario_id`),
  KEY `idx_partidas_jugador2_version_lista` (`jugador2_version_lista_id`),
  KEY `idx_partidas_ganador_usuario` (`ganador_usuario_id`),
  CONSTRAINT `fk_partidas_evento`
    FOREIGN KEY (`evento_id`) REFERENCES `eventos` (`id`),
  CONSTRAINT `fk_partidas_comunidad`
    FOREIGN KEY (`comunidad_id`) REFERENCES `comunidades` (`id`),
  CONSTRAINT `fk_partidas_creado_por_usuario`
    FOREIGN KEY (`creado_por_usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_partidas_sistema_juego`
    FOREIGN KEY (`sistema_juego_id`) REFERENCES `sistemas_juego` (`id`),
  CONSTRAINT `fk_partidas_jugador1_usuario`
    FOREIGN KEY (`jugador1_usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_partidas_jugador1_version_lista`
    FOREIGN KEY (`jugador1_version_lista_id`) REFERENCES `versiones_lista_ejercito` (`id`),
  CONSTRAINT `fk_partidas_jugador2_usuario`
    FOREIGN KEY (`jugador2_usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_partidas_jugador2_version_lista`
    FOREIGN KEY (`jugador2_version_lista_id`) REFERENCES `versiones_lista_ejercito` (`id`),
  CONSTRAINT `fk_partidas_ganador_usuario`
    FOREIGN KEY (`ganador_usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `configuraciones_mision_partida` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `partida_id` BIGINT NOT NULL,
  `pack_mision` VARCHAR(150) DEFAULT NULL,
  `mision` VARCHAR(150) DEFAULT NULL,
  `despliegue` VARCHAR(150) DEFAULT NULL,
  `regla_mision` VARCHAR(150) DEFAULT NULL,
  `seleccion_jugador1` JSON DEFAULT NULL,
  `seleccion_jugador2` JSON DEFAULT NULL,
  `creado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configuraciones_mision_partida_partida` (`partida_id`),
  CONSTRAINT `fk_configuraciones_mision_partida_partida`
    FOREIGN KEY (`partida_id`) REFERENCES `partidas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rondas_partida` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `partida_id` BIGINT NOT NULL,
  `numero_ronda` INT NOT NULL,
  `jugador_con_prioridad_id` BIGINT DEFAULT NULL,
  `cp_jugador1_inicio` INT NOT NULL,
  `cp_jugador1_fin` INT NOT NULL,
  `cp_jugador2_inicio` INT NOT NULL,
  `cp_jugador2_fin` INT NOT NULL,
  `primaria_jugador1` INT NOT NULL,
  `primaria_jugador2` INT NOT NULL,
  `secundaria_jugador1` INT NOT NULL,
  `secundaria_jugador2` INT NOT NULL,
  `bonus_jugador1` INT NOT NULL,
  `bonus_jugador2` INT NOT NULL,
  `total_acumulado_jugador1` INT NOT NULL,
  `total_acumulado_jugador2` INT NOT NULL,
  `detalle_jugador1` JSON DEFAULT NULL,
  `detalle_jugador2` JSON DEFAULT NULL,
  `foto_url` VARCHAR(255) DEFAULT NULL,
  `notas` LONGTEXT DEFAULT NULL,
  `creado_en` DATETIME NOT NULL,
  `actualizado_en` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rondas_partida_partida_numero` (`partida_id`, `numero_ronda`),
  KEY `idx_rondas_partida_prioridad` (`jugador_con_prioridad_id`),
  CONSTRAINT `fk_rondas_partida_partida`
    FOREIGN KEY (`partida_id`) REFERENCES `partidas` (`id`),
  CONSTRAINT `fk_rondas_partida_jugador_con_prioridad`
    FOREIGN KEY (`jugador_con_prioridad_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notificaciones_usuario` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `receptor_usuario_id` BIGINT NOT NULL,
  `emisor_usuario_id` BIGINT DEFAULT NULL,
  `comunidad_id` BIGINT DEFAULT NULL,
  `evento_id` BIGINT DEFAULT NULL,
  `partida_id` BIGINT DEFAULT NULL,
  `tipo` VARCHAR(40) NOT NULL,
  `estado` VARCHAR(20) NOT NULL,
  `mensaje_extra` VARCHAR(255) DEFAULT NULL,
  `creado_en` DATETIME NOT NULL,
  `respondido_en` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notificaciones_receptor` (`receptor_usuario_id`),
  KEY `idx_notificaciones_emisor` (`emisor_usuario_id`),
  KEY `idx_notificaciones_comunidad` (`comunidad_id`),
  KEY `idx_notificaciones_evento` (`evento_id`),
  KEY `idx_notificaciones_partida` (`partida_id`),
  CONSTRAINT `fk_notificaciones_receptor_usuario`
    FOREIGN KEY (`receptor_usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_notificaciones_emisor_usuario`
    FOREIGN KEY (`emisor_usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_notificaciones_comunidad`
    FOREIGN KEY (`comunidad_id`) REFERENCES `comunidades` (`id`),
  CONSTRAINT `fk_notificaciones_evento`
    FOREIGN KEY (`evento_id`) REFERENCES `eventos` (`id`),
  CONSTRAINT `fk_notificaciones_partida`
    FOREIGN KEY (`partida_id`) REFERENCES `partidas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Datos semilla minimos
-- ---------------------------------------------------------------------------

-- Administrador inicial de la aplicación.
-- Credenciales: usuario "admin", contraseña "admin".
INSERT INTO `usuarios` (
  `nombre_usuario`,
  `email`,
  `foto_url`,
  `contrasena_hash`,
  `rol`,
  `activo`,
  `creado_en`,
  `actualizado_en`
) VALUES (
  'admin',
  'admin@tabletopmanager.local',
  NULL,
  '$2a$10$3FMy1JTb5NDiMB7gJ70pEOGKjPiFkFrtuljp.UJ3EIvrlwpigKWMi',
  'ADMIN',
  1,
  NOW(),
  NOW()
);

INSERT INTO `sistemas_juego` (`codigo`, `nombre`, `edicion`, `activo`, `creado_en`) VALUES
  ('WH40K_11', 'Warhammer 40,000', '11a edicion', 1, NOW()),
  ('AOS_4', 'Age of Sigmar', '4a edicion', 1, NOW());

INSERT INTO `fuentes_catalogo` (
  `sistema_juego_id`,
  `nombre_fuente`,
  `tipo_fuente`,
  `url_repositorio`,
  `rama_por_defecto`,
  `activo`,
  `creado_en`
)
SELECT `id`, 'BSData', 'GIT', 'https://github.com/BSData/wh40k-11e', 'main', 1, NOW()
FROM `sistemas_juego`
WHERE `codigo` = 'WH40K_11';

INSERT INTO `fuentes_catalogo` (
  `sistema_juego_id`,
  `nombre_fuente`,
  `tipo_fuente`,
  `url_repositorio`,
  `rama_por_defecto`,
  `activo`,
  `creado_en`
)
SELECT `id`, 'BSData', 'GIT', 'https://github.com/BSData/age-of-sigmar-4th', 'main', 1, NOW()
FROM `sistemas_juego`
WHERE `codigo` = 'AOS_4';

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- Verificacion
-- ---------------------------------------------------------------------------

SHOW TABLES;
SHOW CREATE TABLE `partidas`;
SHOW CREATE TABLE `afiliaciones_comunidad`;
