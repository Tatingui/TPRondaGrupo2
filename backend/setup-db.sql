-- ---------------------------------------------------------------
-- Crea la base y el usuario que necesita el backend de Ronda.
-- Se ejecuta UNA sola vez por maquina, conectandose como root.
--
-- No crea tablas: de eso se encarga Hibernate al arrancar la app,
-- a partir de las clases @Entity (spring.jpa.hibernate.ddl-auto=update).
--
-- Es idempotente: se puede correr varias veces sin romper nada.
-- ---------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS ronda
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ronda'@'localhost' IDENTIFIED BY 'ronda';

GRANT ALL PRIVILEGES ON ronda.* TO 'ronda'@'localhost';

FLUSH PRIVILEGES;

SELECT 'Base y usuario creados correctamente.' AS resultado;
