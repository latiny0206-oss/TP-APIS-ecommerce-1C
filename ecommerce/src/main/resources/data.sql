-- Seed data — INSERT IGNORE es idempotente: no falla si el registro ya existe

INSERT IGNORE INTO categoria (id_categoria, nombre, descripcion) VALUES
    (1, 'Calzado',      'Botas y zapatillas de trekking.'),
    (2, 'Indumentaria', 'Abrigo, camperas técnicas y capas térmicas.'),
    (3, 'Equipamiento', 'Mochilas, carpas, bastones y accesorios.'),
    (4, 'Accesorios',   'Linternas, termos, medias técnicas, guantes.');

INSERT IGNORE INTO marca (id_marca, nombre, descripcion) VALUES
    (1, 'Columbia',       'Indumentaria y calzado outdoor.'),
    (2, 'The North Face', 'Equipo técnico para expediciones.'),
    (3, 'Salomon',        'Calzado y trail running.'),
    (4, 'Montagne',       'Marca argentina de trekking y montaña.'),
    (5, 'Patagonia',      'Ropa outdoor sustentable.');

-- Passwords BCrypt strength=10: admin123 | user123 | cliente123
INSERT IGNORE INTO usuario (id_usuario, username, email, password, nombre, apellido, rol, estado) VALUES
    (1, 'admin',      'admin@trekking.com',    '$2a$10$rYBTK8hGdafLCBzAoYN0X.ohFBPu5XzcQDL.C1Ib3ZwDk0b6wdE3i', 'Admin',   'Sistema',  'ADMIN',   'ACTIVO'),
    (2, 'juanperez',  'juan.perez@mail.com',   '$2a$10$fa0WiM9OfvwpXFhTZyhxkeHcFWC/1l2SRikXLlke.J82nDykPS7AW', 'Juan',    'Perez',    'CLIENTE', 'ACTIVO'),
    (3, 'mariagomez', 'maria.gomez@mail.com',  '$2a$10$PBo6KmC7d.YhT25MuPM64uRr9z1NMde53sqQ5rl0f7GW3J5kXXcq2', 'Maria',   'Gomez',    'CLIENTE', 'ACTIVO'),
    (4, 'inactivo',   'inactivo@trekking.com', '$2a$10$fa0WiM9OfvwpXFhTZyhxkeHcFWC/1l2SRikXLlke.J82nDykPS7AW', 'Usuario', 'Inactivo', 'CLIENTE', 'INACTIVO');

INSERT IGNORE INTO descuento (id_descuento, nombre, tipo, valor, fecha_ini, fecha_fin, estado) VALUES
    (1, 'Promo Otono 2026',    'PORCENTAJE', 15.00,   '2026-04-01', '2026-06-30', 'ACTIVO'),
    (2, 'Descuento Fijo 5000', 'FIJO',       5000.00, '2026-04-01', '2026-12-31', 'ACTIVO'),
    (3, 'Black Friday 2025',   'PORCENTAJE', 30.00,   '2025-11-20', '2025-11-30', 'EXPIRADO');

INSERT IGNORE INTO producto (id_producto, id_marca, id_categoria, nombre, descripcion, estado, precio_base) VALUES
    (1, 2, 1, 'Bota Trekking Vectiv',  'Bota de caña media, impermeable, suela Vibram.',        'ACTIVO',  85000.00),
    (2, 3, 1, 'Zapatilla X-Ultra 4',   'Zapatilla de trail running con Gore-Tex.',              'ACTIVO',  72000.00),
    (3, 1, 2, 'Campera Bugaboo II',    'Campera 3 en 1 con interior polar desmontable.',        'ACTIVO', 120000.00),
    (4, 5, 2, 'Primera Piel Capilene', 'Remera técnica de manga larga, secado rápido.',         'ACTIVO',  28000.00),
    (5, 4, 3, 'Mochila Ascent 45L',    'Mochila de ataque con sistema de ventilación dorsal.',  'ACTIVO',  65000.00),
    (6, 4, 3, 'Carpa Aconcagua 2P',    'Carpa iglú 2 personas, doble techo, 3 estaciones.',     'ACTIVO', 145000.00),
    (7, 2, 4, 'Linterna Frontal TNF',  'Linterna frontal 400 lumens, recargable USB.',          'PAUSADO', 18500.00);

INSERT IGNORE INTO variante_producto (id_variante, id_producto, color, talla, material, peso, stock, precio, estacion) VALUES
    (1,  1, 'Negro',    '42', 'Cuero / Gore-Tex',    650.00, 12,  85000.00, 'INVIERNO'),
    (2,  1, 'Marron',   '43', 'Cuero / Gore-Tex',    680.00,  8,  85000.00, 'INVIERNO'),
    (3,  2, 'Gris',     '41', 'Mesh / Gore-Tex',     380.00, 15,  72000.00, 'OTONO'),
    (4,  2, 'Azul',     '42', 'Mesh / Gore-Tex',     390.00, 10,  72000.00, 'OTONO'),
    (5,  3, 'Negro',    'M',  'Nylon / Polar',        780.00,  7, 120000.00, 'INVIERNO'),
    (6,  3, 'Rojo',     'L',  'Nylon / Polar',        820.00,  5, 120000.00, 'INVIERNO'),
    (7,  4, 'Gris',     'M',  'Poliester Capilene',   180.00, 20,  28000.00, 'PRIMAVERA'),
    (8,  4, 'Verde',    'L',  'Poliester Capilene',   190.00, 18,  28000.00, 'PRIMAVERA'),
    (9,  5, 'Azul',     'U',  'Nylon ripstop 420D', 1250.00,  9,  65000.00, 'VERANO'),
    (10, 6, 'Amarillo', 'U',  'Poliester 75D',      2800.00,  4, 145000.00, 'OTONO'),
    (11, 7, 'Negro',    'U',  'Aluminio / ABS',      120.00,  0,  18500.00, 'VERANO');

INSERT IGNORE INTO carrito (id_carrito, id_usuario, id_descuento, estado, monto_total, fecha_ultima_modificacion) VALUES
    (1, 2, 1,    'ACTIVO', 157000.00, NOW()),
    (2, 3, NULL, 'VACIO',       0.00, NOW());

INSERT IGNORE INTO item_carrito (id_item_carrito, id_carrito, id_variante, cantidad, precio_unitario) VALUES
    (1, 1, 1, 1, 85000.00),
    (2, 1, 7, 2, 28000.00),
    (3, 1, 9, 1, 65000.00);

INSERT IGNORE INTO orden (id_orden, id_usuario, id_carrito, id_descuento, fecha_creacion, monto_final, estado) VALUES
    (1, 3, NULL, NULL, '2026-03-15 14:22:10', 150000.00, 'ENTREGADA');

INSERT IGNORE INTO item_orden (id_item_orden, id_orden, id_variante, cantidad, precio_al_momento) VALUES
    (1, 1, 5, 1, 120000.00),
    (2, 1, 8, 1,  28000.00);
