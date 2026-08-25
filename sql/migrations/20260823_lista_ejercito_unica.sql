-- Ejecutar una vez sobre instalaciones existentes antes de desplegar la version
-- que bloquea la lista durante la creacion de una nueva version.
ALTER TABLE listas_ejercito
    ADD CONSTRAINT uk_listas_ejercito_propietario_sistema_nombre
    UNIQUE (propietario_usuario_id, sistema_juego_id, nombre);
