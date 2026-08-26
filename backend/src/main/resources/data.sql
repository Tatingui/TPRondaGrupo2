-- ---------------------------------------------------------------
-- Datos semilla. Spring lo ejecuta en CADA arranque, despues de que
-- Hibernate crea/actualiza las tablas desde las clases @Entity.
--
-- IMPORTANTE: como corre siempre, los INSERT tienen que ser
-- idempotentes o vas a duplicar filas en cada arranque. Usar:
--
--   INSERT IGNORE INTO tabla (...) VALUES (...);
--
--   -- o bien, para actualizar si ya existe:
--   INSERT INTO tabla (id, campo) VALUES (1, 'x')
--       ON DUPLICATE KEY UPDATE campo = VALUES(campo);
--
-- Todavia no hay entidades JPA, asi que no hay nada que insertar.
-- La linea de abajo es un no-op: Spring falla al arrancar si este
-- archivo no tiene al menos UNA sentencia real (un archivo con solo
-- comentarios cuenta como vacio). Borrala cuando agregues los INSERT.
-- ---------------------------------------------------------------

SELECT 1;

-- Ejemplo de como va a quedar con las entidades del proximo incremento:
--
--   INSERT IGNORE INTO categorias (id, nombre) VALUES
--       (1, 'Deportes'),
--       (2, 'Hogar'),
--       (3, 'Electronica'),
--       (4, 'Ropa'),
--       (5, 'Otros');
