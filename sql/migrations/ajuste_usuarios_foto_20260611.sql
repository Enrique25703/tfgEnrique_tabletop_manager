USE `tfgenrique`;

ALTER TABLE `usuarios`
  ADD COLUMN `foto_url` VARCHAR(255) NULL AFTER `email`;

SHOW CREATE TABLE `usuarios`;
