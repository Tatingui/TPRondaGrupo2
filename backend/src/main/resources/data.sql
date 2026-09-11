-- ---------------------------------------------------------------
-- Datos semilla: Categorias, Usuarios de prueba y Publicaciones
-- ---------------------------------------------------------------

-- 1. Categorias
INSERT IGNORE INTO categories (id, name) VALUES (1, 'Deportes');
INSERT IGNORE INTO categories (id, name) VALUES (2, 'Hogar');
INSERT IGNORE INTO categories (id, name) VALUES (3, 'Electrónica');
INSERT IGNORE INTO categories (id, name) VALUES (4, 'Ropa');
INSERT IGNORE INTO categories (id, name) VALUES (5, 'Otros');

-- 2. Usuarios de prueba (Password es 'password' hasheada)
-- $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uSyLnS
INSERT IGNORE INTO users (id, nombre, email, password, email_verified, created_at)
VALUES (1, 'Juan Vendedor', 'juan@ronda.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uSyLnS', 1, NOW());

INSERT IGNORE INTO users (id, nombre, email, password, email_verified, created_at)
VALUES (2, 'Maria Hogar', 'maria@ronda.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uSyLnS', 1, NOW());

INSERT IGNORE INTO users (id, nombre, email, password, email_verified, created_at)
VALUES (3, 'Tech Store', 'tech@ronda.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uSyLnS', 1, NOW());

-- 3. Publicaciones
-- Bicicleta (id=1)
INSERT IGNORE INTO publications (id, title, description, price, status, location, category_id, seller_id, created_at)
VALUES (1, 'Bicicleta de montaña rodado 29', 'Excelente estado, cuadro de aluminio, 21 velocidades Shimano.', 150000.0, 'LIKE_NEW', 'Palermo', 1, 1, NOW());

INSERT INTO publication_images (publication_id, image_url)
SELECT 1, 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&q=80&w=800'
WHERE NOT EXISTS (
    SELECT 1 FROM publication_images WHERE publication_id = 1 AND image_url = 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&q=80&w=800'
);

-- Sillon (id=2)
INSERT IGNORE INTO publications (id, title, description, price, status, location, category_id, seller_id, created_at)
VALUES (2, 'Sillón 3 cuerpos gris', 'Súper cómodo, tapizado impecable. Se retira por Almagro.', 85000.0, 'USED', 'Almagro', 2, 2, NOW());

INSERT INTO publication_images (publication_id, image_url)
SELECT 2, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800'
WHERE NOT EXISTS (
    SELECT 1 FROM publication_images WHERE publication_id = 2 AND image_url = 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800'
);

-- iPhone (id=3)
INSERT IGNORE INTO publications (id, title, description, price, status, location, category_id, seller_id, created_at)
VALUES (3, 'iPhone 13 128GB Azul', 'Nuevo en caja sellada. Garantía oficial.', 950000.0, 'NEW', 'Belgrano', 3, 3, NOW());

INSERT INTO publication_images (publication_id, image_url)
SELECT 3, 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&q=80&w=800'
WHERE NOT EXISTS (
    SELECT 1 FROM publication_images WHERE publication_id = 3 AND image_url = 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&q=80&w=800'
);

-- Mesa de luz (id=4)
INSERT IGNORE INTO publications (id, title, description, price, status, location, category_id, seller_id, created_at)
VALUES (4, 'Mesa de luz de pino', 'Madera maciza, un cajón. Medidas 40x40.', 25000.0, 'USED', 'Caballito', 2, 2, NOW());

INSERT INTO publication_images (publication_id, image_url)
SELECT 4, 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800'
WHERE NOT EXISTS (
    SELECT 1 FROM publication_images WHERE publication_id = 4 AND image_url = 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800'
);

-- -> Zapatillas (id=5)
INSERT IGNORE INTO publications (id, title, description, price, status, location, category_id, seller_id, created_at)
VALUES (5, 'Zapatillas running Adidas', 'Talle 42, color azul. Sin uso, nuevas en caja.', 65000.0, 'NEW', 'Villa Urquiza', 4, 1, NOW());

INSERT INTO publication_images (publication_id, image_url)
SELECT 5, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=800'
WHERE NOT EXISTS (
    SELECT 1 FROM publication_images WHERE publication_id = 5 AND image_url = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=800'
);
