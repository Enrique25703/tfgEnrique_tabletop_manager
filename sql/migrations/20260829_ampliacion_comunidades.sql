USE `tfgenrique`;

ALTER TABLE `comunidades`
  ADD COLUMN `descripcion` TEXT NULL AFTER `logo_url`,
  ADD COLUMN `privacidad` VARCHAR(20) NOT NULL DEFAULT 'PUBLICA' AFTER `descripcion`;

UPDATE `afiliaciones_comunidad`
SET `rol_comunidad` = 'ADMINISTRADOR'
WHERE `rol_comunidad` = 'PROPIETARIO';

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
