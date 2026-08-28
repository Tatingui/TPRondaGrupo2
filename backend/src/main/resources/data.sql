-- ---------------------------------------------------------------
-- Datos semilla.
--
-- Las TABLAS no se crean aca: las genera Hibernate a partir de las
-- clases @Entity (spring.jpa.hibernate.ddl-auto=update). Este archivo
-- es solo para INSERTs de datos iniciales.
--
-- Todavia no hay datos semilla que cargar. La linea de abajo es un
-- no-op: Spring falla al arrancar si el script no tiene al menos UNA
-- sentencia real (un archivo con solo comentarios cuenta como vacio).
--
-- Cuando agregues INSERTs, tienen que ser idempotentes, porque este
-- archivo se ejecuta en CADA arranque:
--
--   INSERT IGNORE INTO tabla (...) VALUES (...);
-- ---------------------------------------------------------------

SELECT 1;
