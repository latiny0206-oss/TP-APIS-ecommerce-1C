-- =====================================================================
-- Trekking E-commerce — Script OPCIONAL de esquema + datos semilla
-- Base: MySQL 8.x
-- Grupo 13 — APIs 2026
--
-- USO:
--   mysql -u root -p < trekking_ecommerce.sql
--   (o ejecutar en MySQL Workbench)
--
-- NOTA: Spring Boot con `spring.jpa.hibernate.ddl-auto=update` crea las
-- tablas automáticamente. Este script se provee para:
--   (a) Preparar la BD antes de levantar la app.
--   (b) Cargar datos de prueba para validación en Postman.
--   (c) Regenerar el entorno ante corrupción de datos.
--
-- Contraseñas semilla (hash BCrypt, strength=10):
--   admin      / admin123
--   juanperez  / user123
--   mariagomez / cliente123
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. Base de datos
-- ---------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS trekking_ecommerce
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE trekking_ecommerce;

-- ---------------------------------------------------------------------
-- 1. Limpieza previa (idempotencia)
-- ---------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS item_orden;
DROP TABLE IF EXISTS orden;
DROP TABLE IF EXISTS item_carrito;
DROP TABLE IF EXISTS carrito;
DROP TABLE IF EXISTS foto;
DROP TABLE IF EXISTS variante_producto;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS descuento;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS marca;
DROP TABLE IF EXISTS categoria;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 2. Tablas maestras (sin dependencias)
-- ---------------------------------------------------------------------

CREATE TABLE categoria (
    id_categoria BIGINT       NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  TEXT,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB;

CREATE TABLE marca (
    id_marca    BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    PRIMARY KEY (id_marca)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    apellido   VARCHAR(100) NOT NULL,
    rol        VARCHAR(20)  NOT NULL,  -- ADMIN | CLIENTE
    estado     VARCHAR(20)  NOT NULL,  -- ACTIVO | INACTIVO
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uk_usuario_username (username),
    UNIQUE KEY uk_usuario_email    (email)
) ENGINE=InnoDB;

CREATE TABLE descuento (
    id_descuento BIGINT         NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100)   NOT NULL,
    codigo       VARCHAR(50)    NULL,
    tipo         VARCHAR(20)    NOT NULL,   -- PORCENTAJE | FIJO
    valor        DECIMAL(10, 2) NOT NULL,
    fecha_ini    DATE           NOT NULL,
    fecha_fin    DATE           NOT NULL,
    estado       VARCHAR(20)    NOT NULL,   -- ACTIVO | EXPIRADO
    PRIMARY KEY (id_descuento),
    UNIQUE KEY uk_descuento_codigo (codigo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. Catálogo (dependencias: marca, categoria)
-- ---------------------------------------------------------------------

CREATE TABLE producto (
    id_producto  BIGINT         NOT NULL AUTO_INCREMENT,
    id_marca     BIGINT         NOT NULL,
    id_categoria BIGINT         NOT NULL,
    nombre       VARCHAR(150)   NOT NULL,
    descripcion  TEXT,
    estado       VARCHAR(20)    NOT NULL,   -- ACTIVO | PAUSADO | ELIMINADO
    precio_base  DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id_producto),
    KEY idx_producto_marca     (id_marca),
    KEY idx_producto_categoria (id_categoria),
    CONSTRAINT fk_producto_marca
        FOREIGN KEY (id_marca)     REFERENCES marca     (id_marca),
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
) ENGINE=InnoDB;

CREATE TABLE variante_producto (
    id_variante BIGINT         NOT NULL AUTO_INCREMENT,
    id_producto BIGINT         NOT NULL,
    color       VARCHAR(50)    NOT NULL,
    talla       VARCHAR(20)    NOT NULL,
    material    VARCHAR(100)   NOT NULL,
    peso        DECIMAL(8, 2)  NOT NULL,
    stock       INT            NOT NULL,
    precio      DECIMAL(10, 2) NOT NULL,
    estacion    VARCHAR(20)    NOT NULL,   -- PRIMAVERA | VERANO | OTONO | INVIERNO
    version     BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id_variante),
    UNIQUE KEY uk_variante_prod_color_talla (id_producto, color, talla),
    CONSTRAINT fk_variante_producto
        FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
) ENGINE=InnoDB;

CREATE TABLE foto (
    id_foto        BIGINT       NOT NULL AUTO_INCREMENT,
    id_variante    BIGINT       NOT NULL,
    nombre         VARCHAR(255) NOT NULL,
    tipo_contenido VARCHAR(100) NOT NULL,
    orden          INT          NOT NULL,
    datos          LONGBLOB     NOT NULL,
    PRIMARY KEY (id_foto),
    KEY idx_foto_variante (id_variante),
    CONSTRAINT fk_foto_variante
        FOREIGN KEY (id_variante) REFERENCES variante_producto (id_variante)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. Carritos y Órdenes
-- ---------------------------------------------------------------------

CREATE TABLE carrito (
    id_carrito                BIGINT         NOT NULL AUTO_INCREMENT,
    id_usuario                BIGINT         NOT NULL,
    id_descuento              BIGINT         NULL,
    estado                    VARCHAR(20)    NOT NULL,  -- ACTIVO | VACIO | ABANDONADO | CONVERTIDO
    monto_total               DECIMAL(10, 2) NOT NULL,
    fecha_ultima_modificacion DATETIME,
    PRIMARY KEY (id_carrito),
    KEY idx_carrito_usuario   (id_usuario),
    KEY idx_carrito_descuento (id_descuento),
    CONSTRAINT fk_carrito_usuario
        FOREIGN KEY (id_usuario)   REFERENCES usuario   (id_usuario),
    CONSTRAINT fk_carrito_descuento
        FOREIGN KEY (id_descuento) REFERENCES descuento (id_descuento)
) ENGINE=InnoDB;

CREATE TABLE item_carrito (
    id_item_carrito  BIGINT         NOT NULL AUTO_INCREMENT,
    id_carrito       BIGINT         NOT NULL,
    id_variante      BIGINT         NOT NULL,
    cantidad         INT            NOT NULL,
    precio_unitario  DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id_item_carrito),
    KEY idx_item_carrito_carrito  (id_carrito),
    KEY idx_item_carrito_variante (id_variante),
    CONSTRAINT fk_item_carrito_carrito
        FOREIGN KEY (id_carrito)  REFERENCES carrito           (id_carrito),
    CONSTRAINT fk_item_carrito_variante
        FOREIGN KEY (id_variante) REFERENCES variante_producto (id_variante)
) ENGINE=InnoDB;

CREATE TABLE orden (
    id_orden       BIGINT         NOT NULL AUTO_INCREMENT,
    id_usuario     BIGINT         NOT NULL,
    id_carrito     BIGINT         NULL,
    id_descuento   BIGINT         NULL,
    fecha_creacion DATETIME       NOT NULL,
    monto_final    DECIMAL(10, 2) NOT NULL,
    estado         VARCHAR(20)    NOT NULL,   -- PENDIENTE | CONFIRMADA | ENTREGADA | CANCELADA
    PRIMARY KEY (id_orden),
    KEY idx_orden_usuario   (id_usuario),
    KEY idx_orden_carrito   (id_carrito),
    KEY idx_orden_descuento (id_descuento),
    CONSTRAINT fk_orden_usuario
        FOREIGN KEY (id_usuario)   REFERENCES usuario   (id_usuario),
    CONSTRAINT fk_orden_carrito
        FOREIGN KEY (id_carrito)   REFERENCES carrito   (id_carrito),
    CONSTRAINT fk_orden_descuento
        FOREIGN KEY (id_descuento) REFERENCES descuento (id_descuento)
) ENGINE=InnoDB;

CREATE TABLE item_orden (
    id_item_orden     BIGINT         NOT NULL AUTO_INCREMENT,
    id_orden          BIGINT         NOT NULL,
    id_variante       BIGINT         NOT NULL,
    cantidad          INT            NOT NULL,
    precio_al_momento DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id_item_orden),
    KEY idx_item_orden_orden    (id_orden),
    KEY idx_item_orden_variante (id_variante),
    CONSTRAINT fk_item_orden_orden
        FOREIGN KEY (id_orden)    REFERENCES orden             (id_orden),
    CONSTRAINT fk_item_orden_variante
        FOREIGN KEY (id_variante) REFERENCES variante_producto (id_variante)
) ENGINE=InnoDB;

-- =====================================================================
-- 5. DATOS SEMILLA
-- =====================================================================

-- ---------- 5.1 Categorías ----------
INSERT INTO categoria (id_categoria, nombre, descripcion) VALUES
    (1, 'Calzado',       'Botas y zapatillas de trekking.'),
    (2, 'Indumentaria',  'Abrigo, camperas técnicas y capas térmicas.'),
    (3, 'Equipamiento',  'Mochilas, carpas, bastones y accesorios.'),
    (4, 'Accesorios',    'Linternas, termos, medias técnicas, guantes.');

-- ---------- 5.2 Marcas ----------
INSERT INTO marca (id_marca, nombre, descripcion) VALUES
    (1, 'Columbia',      'Indumentaria y calzado outdoor.'),
    (2, 'The North Face','Equipo técnico para expediciones.'),
    (3, 'Salomon',       'Calzado y trail running.'),
    (4, 'Montagne',      'Marca argentina de trekking y montaña.'),
    (5, 'Patagonia',     'Ropa outdoor sustentable.');

-- ---------- 5.3 Usuarios ----------
-- Hashes BCrypt (strength=10) verificados contra Spring Security:
--   admin123    -> $2a$10$rYBTK8hGdafLCBzAoYN0X.ohFBPu5XzcQDL.C1Ib3ZwDk0b6wdE3i
--   user123     -> $2a$10$fa0WiM9OfvwpXFhTZyhxkeHcFWC/1l2SRikXLlke.J82nDykPS7AW
--   cliente123  -> $2a$10$PBo6KmC7d.YhT25MuPM64uRr9z1NMde53sqQ5rl0f7GW3J5kXXcq2
INSERT INTO usuario (id_usuario, username, email, password, nombre, apellido, rol, estado) VALUES
    (1, 'admin',      'admin@trekking.com',      '$2a$10$rYBTK8hGdafLCBzAoYN0X.ohFBPu5XzcQDL.C1Ib3ZwDk0b6wdE3i', 'Admin',  'Sistema',  'ADMIN',   'ACTIVO'),
    (2, 'juanperez',  'juan.perez@mail.com',     '$2a$10$fa0WiM9OfvwpXFhTZyhxkeHcFWC/1l2SRikXLlke.J82nDykPS7AW', 'Juan',   'Perez',    'CLIENTE', 'ACTIVO'),
    (3, 'mariagomez', 'maria.gomez@mail.com',    '$2a$10$PBo6KmC7d.YhT25MuPM64uRr9z1NMde53sqQ5rl0f7GW3J5kXXcq2', 'Maria',  'Gomez',    'CLIENTE', 'ACTIVO'),
    (4, 'inactivo',   'inactivo@trekking.com',   '$2a$10$fa0WiM9OfvwpXFhTZyhxkeHcFWC/1l2SRikXLlke.J82nDykPS7AW', 'Usuario','Inactivo', 'CLIENTE', 'INACTIVO');

-- ---------- 5.4 Descuentos ----------
INSERT INTO descuento (id_descuento, nombre, codigo, tipo, valor, fecha_ini, fecha_fin, estado) VALUES
    (1, 'Promo Otono 2026',    'OTONO2026',       'PORCENTAJE', 15.00,   '2026-04-01', '2027-12-30', 'ACTIVO'),
    (2, 'Descuento Fijo 5000', 'FIJO5000',        'FIJO',       5000.00, '2026-04-01', '2026-12-31', 'ACTIVO'),
    (3, 'Black Friday 2025',   'BLACKFRIDAY2025', 'PORCENTAJE', 30.00,   '2025-11-20', '2027-12-31', 'ACTIVO');

-- ---------- 5.5 Productos ----------
INSERT INTO producto (id_producto, id_marca, id_categoria, nombre, descripcion, estado, precio_base) VALUES
    (1, 2, 1, 'Bota Trekking Vectiv',   'Bota de caña media, impermeable, suela Vibram.',        'ACTIVO',  85000.00),
    (2, 3, 1, 'Zapatilla X-Ultra 4',    'Zapatilla de trail running con Gore-Tex.',              'ACTIVO',  72000.00),
    (3, 1, 2, 'Campera Bugaboo II',     'Campera 3 en 1 con interior polar desmontable.',        'ACTIVO', 120000.00),
    (4, 5, 2, 'Primera Piel Capilene',  'Remera técnica de manga larga, secado rápido.',         'ACTIVO',  28000.00),
    (5, 4, 3, 'Mochila Ascent 45L',     'Mochila de ataque con sistema de ventilación dorsal.',  'ACTIVO',  65000.00),
    (6, 4, 3, 'Carpa Aconcagua 2P',     'Carpa iglú 2 personas, doble techo, 3 estaciones.',     'ACTIVO', 145000.00),
    (7, 2, 4, 'Linterna Frontal TNF',   'Linterna frontal 400 lumens, recargable USB.',          'PAUSADO', 18500.00);

-- ---------- 5.6 Variantes ----------
INSERT INTO variante_producto (id_variante, id_producto, color, talla, material, peso, stock, precio, estacion) VALUES
    (1, 1, 'Negro',     '42',  'Cuero / Gore-Tex',   650.00, 12,  85000.00, 'INVIERNO'),
    (2, 1, 'Marron',    '43',  'Cuero / Gore-Tex',   680.00,  8,  85000.00, 'INVIERNO'),
    (3, 2, 'Gris',      '41',  'Mesh / Gore-Tex',    380.00, 15,  72000.00, 'OTONO'),
    (4, 2, 'Azul',      '42',  'Mesh / Gore-Tex',    390.00, 10,  72000.00, 'OTONO'),
    (5, 3, 'Negro',     'M',   'Nylon / Polar',      780.00,  7, 120000.00, 'INVIERNO'),
    (6, 3, 'Rojo',      'L',   'Nylon / Polar',      820.00,  5, 120000.00, 'INVIERNO'),
    (7, 4, 'Gris',      'M',   'Poliester Capilene', 180.00, 20,  28000.00, 'PRIMAVERA'),
    (8, 4, 'Verde',     'L',   'Poliester Capilene', 190.00, 18,  28000.00, 'PRIMAVERA'),
    (9, 5, 'Azul',      'U',   'Nylon ripstop 420D',1250.00,  9,  65000.00, 'VERANO'),
    (10,6, 'Amarillo',  'U',   'Poliester 75D',     2800.00,  4, 145000.00, 'OTONO'),
    (11,7, 'Negro',     'U',   'Aluminio / ABS',     120.00,  0,  18500.00, 'VERANO');

-- ---------- 5.7 Carrito activo de ejemplo (usuario 'juanperez') ----------
INSERT INTO carrito (id_carrito, id_usuario, id_descuento, estado, monto_total, fecha_ultima_modificacion) VALUES
    (1, 2, 1,    'ACTIVO', 157000.00, NOW()),
    (2, 3, NULL, 'VACIO',       0.00, NOW());

INSERT INTO item_carrito (id_item_carrito, id_carrito, id_variante, cantidad, precio_unitario) VALUES
    (1, 1, 1, 1,  85000.00),   -- 1 x Bota Vectiv talle 42
    (2, 1, 7, 2,  28000.00),   -- 2 x Primera Piel Capilene M
    (3, 1, 9, 1,  65000.00);   -- (el total se recalcula vía API; valor de semilla ilustrativo)

-- ---------- 5.8 Orden histórica confirmada (usuario 'mariagomez') ----------
INSERT INTO orden (id_orden, id_usuario, id_carrito, id_descuento, fecha_creacion, monto_final, estado) VALUES
    (1, 3, NULL, NULL, '2026-03-15 14:22:10', 150000.00, 'ENTREGADA');

INSERT INTO item_orden (id_item_orden, id_orden, id_variante, cantidad, precio_al_momento) VALUES
    (1, 1, 5,  1, 120000.00),  -- Campera Bugaboo II, talle M
    (2, 1, 8,  1,  28000.00);  -- Primera Piel Capilene, talle L

-- ---------------------------------------------------------------------
-- 6. Verificación rápida
-- ---------------------------------------------------------------------
SELECT 'categoria'          AS tabla, COUNT(*) AS filas FROM categoria
UNION ALL SELECT 'marca',             COUNT(*) FROM marca
UNION ALL SELECT 'usuario',           COUNT(*) FROM usuario
UNION ALL SELECT 'descuento',         COUNT(*) FROM descuento
UNION ALL SELECT 'producto',          COUNT(*) FROM producto
UNION ALL SELECT 'variante_producto', COUNT(*) FROM variante_producto
UNION ALL SELECT 'foto',              COUNT(*) FROM foto
UNION ALL SELECT 'carrito',           COUNT(*) FROM carrito
UNION ALL SELECT 'item_carrito',      COUNT(*) FROM item_carrito
UNION ALL SELECT 'orden',             COUNT(*) FROM orden
UNION ALL SELECT 'item_orden',        COUNT(*) FROM item_orden;

-- =====================================================================
-- FIN — Credenciales para Postman:
--   POST /api/auth/login
--     { "username": "admin",      "password": "admin123" }     -> rol ADMIN
--     { "username": "juanperez",  "password": "user123" }      -> rol CLIENTE
--     { "username": "mariagomez", "password": "cliente123" }   -> rol CLIENTE
-- =====================================================================