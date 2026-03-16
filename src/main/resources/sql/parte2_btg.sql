-- ============================================================
-- PARTE 2: SQL (20%) — Base de datos BTG
-- Prueba Técnica · BTG Pactual · Back End
-- ============================================================
--
-- ESQUEMA RELACIONAL
-- ----------------------------------------------------------
--   Cliente       (id PK, nombre, apellidos, ciudad)
--   Sucursal      (id PK, nombre, ciudad)
--   Producto      (id PK, nombre, tipoProducto)
--   Inscripcion   (idProducto PK/FK, idCliente PK/FK)
--   Disponibilidad(idSucursal PK/FK, idProducto PK/FK)
--   Visitan       (idSucursal PK/FK, idCliente PK/FK, fechaVisita)
-- ============================================================


-- ============================================================
-- 1. DDL — Creación de tablas
-- ============================================================

CREATE TABLE Cliente (
    id        NUMBER       PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    ciudad    VARCHAR(100) NOT NULL
);

CREATE TABLE Sucursal (
    id     NUMBER       PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL
);

CREATE TABLE Producto (
    id           NUMBER       PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    tipoProducto VARCHAR(50)  NOT NULL
);

-- Tabla de relación: qué productos tiene inscrito cada cliente
CREATE TABLE Inscripcion (
    idProducto NUMBER NOT NULL REFERENCES Producto(id),
    idCliente  NUMBER NOT NULL REFERENCES Cliente(id),
    PRIMARY KEY (idProducto, idCliente)
);

-- Tabla de relación: qué productos están disponibles en cada sucursal
-- CLAVE: no todas las sucursales ofrecen los mismos productos
CREATE TABLE Disponibilidad (
    idSucursal NUMBER NOT NULL REFERENCES Sucursal(id),
    idProducto NUMBER NOT NULL REFERENCES Producto(id),
    PRIMARY KEY (idSucursal, idProducto)
);

-- Tabla de relación: qué sucursales visita cada cliente
CREATE TABLE Visitan (
    idSucursal  NUMBER NOT NULL REFERENCES Sucursal(id),
    idCliente   NUMBER NOT NULL REFERENCES Cliente(id),
    fechaVisita DATE   NOT NULL,
    PRIMARY KEY (idSucursal, idCliente)
);


-- ============================================================
-- 2. Datos de prueba (DML)
-- Escenario diseñado para validar todos los casos de la query
-- ============================================================

-- Clientes
INSERT INTO Cliente VALUES (1, 'Ana',    'Gómez',   'Bogotá');
INSERT INTO Cliente VALUES (2, 'Luis',   'Pérez',   'Medellín');
INSERT INTO Cliente VALUES (3, 'María',  'Torres',  'Cali');
INSERT INTO Cliente VALUES (4, 'Carlos', 'Ramírez', 'Bogotá');

-- Sucursales
INSERT INTO Sucursal VALUES (1, 'Sucursal Norte',  'Bogotá');
INSERT INTO Sucursal VALUES (2, 'Sucursal Sur',    'Bogotá');
INSERT INTO Sucursal VALUES (3, 'Sucursal Centro', 'Medellín');

-- Productos
--   Producto A: disponible solo en sucursales 1 y 2 (Bogotá)
--   Producto B: disponible en las 3 sucursales (amplia disponibilidad)
--   Producto C: disponible solo en sucursal 3 (Medellín)
INSERT INTO Producto VALUES (1, 'Producto A', 'FPV');
INSERT INTO Producto VALUES (2, 'Producto B', 'FIC');
INSERT INTO Producto VALUES (3, 'Producto C', 'FPV');

-- Disponibilidad
INSERT INTO Disponibilidad VALUES (1, 1); -- Sucursal Norte  → Producto A
INSERT INTO Disponibilidad VALUES (2, 1); -- Sucursal Sur    → Producto A
INSERT INTO Disponibilidad VALUES (1, 2); -- Sucursal Norte  → Producto B
INSERT INTO Disponibilidad VALUES (2, 2); -- Sucursal Sur    → Producto B
INSERT INTO Disponibilidad VALUES (3, 2); -- Sucursal Centro → Producto B
INSERT INTO Disponibilidad VALUES (3, 3); -- Sucursal Centro → Producto C

-- Visitas de clientes
--   Ana    visita sucursales 1 y 2       (solo Bogotá)
--   Luis   visita sucursal 3             (solo Medellín)
--   María  visita sucursales 1 y 3       (Bogotá + Medellín)
--   Carlos visita solo sucursal 1        (solo Sucursal Norte)
INSERT INTO Visitan VALUES (1, 1, DATE '2024-01-10'); -- Ana    → Sucursal Norte
INSERT INTO Visitan VALUES (2, 1, DATE '2024-01-11'); -- Ana    → Sucursal Sur
INSERT INTO Visitan VALUES (3, 2, DATE '2024-01-12'); -- Luis   → Sucursal Centro
INSERT INTO Visitan VALUES (1, 3, DATE '2024-01-13'); -- María  → Sucursal Norte
INSERT INTO Visitan VALUES (3, 3, DATE '2024-01-14'); -- María  → Sucursal Centro
INSERT INTO Visitan VALUES (1, 4, DATE '2024-01-15'); -- Carlos → Sucursal Norte

-- Inscripciones
--   Ana    inscribe Producto A → disponible en {1,2},  Ana visita {1,2}     ✅ CUMPLE
--   Luis   inscribe Producto B → disponible en {1,2,3},Luis visita {3}      ❌ NO CUMPLE (1 y 2 no son visitadas)
--   María  inscribe Producto C → disponible en {3},    María visita {1,3}   ✅ CUMPLE ({3} ⊆ {1,3})
--   Carlos inscribe Producto B → disponible en {1,2,3},Carlos visita {1}    ❌ NO CUMPLE (2 y 3 no son visitadas)
INSERT INTO Inscripcion VALUES (1, 1); -- Ana    inscribe Producto A
INSERT INTO Inscripcion VALUES (2, 2); -- Luis   inscribe Producto B
INSERT INTO Inscripcion VALUES (3, 3); -- María  inscribe Producto C
INSERT INTO Inscripcion VALUES (2, 4); -- Carlos inscribe Producto B

-- RESULTADO ESPERADO: Ana, María


-- ============================================================
-- 3. CONSULTA PRINCIPAL
-- ============================================================
--
-- Requerimiento:
--   "Obtener los nombres de los clientes que tienen inscrito
--    algún producto disponible SOLO en las sucursales que visitan."
--
-- INTERPRETACIÓN:
--   Un cliente C cumple la condición si tiene al menos un
--   producto P inscrito tal que:
--     TODAS las sucursales donde P está disponible
--     son sucursales que C visita.
--   Es decir: no existe ninguna sucursal donde P esté
--   disponible y C NO la visite.
--
-- TÉCNICA: doble NOT EXISTS (cuantificador universal)
--   "Para todo x: f(x)" ≡ "No existe x tal que: ¬f(x)"
-- ============================================================

SELECT DISTINCT
    c.nombre,
    c.apellidos
FROM  Cliente c
JOIN  Inscripcion i ON c.id = i.idCliente
WHERE NOT EXISTS (
    /*
     * No debe existir ninguna sucursal donde el producto inscrito
     * esté disponible Y el cliente NO la visite.
     * Si no existe tal sucursal → el producto está disponible
     * ÚNICAMENTE en sucursales que el cliente sí visita.
     */
    SELECT 1
    FROM   Disponibilidad d
    WHERE  d.idProducto = i.idProducto
      AND  d.idSucursal NOT IN (
               SELECT v.idSucursal
               FROM   Visitan v
               WHERE  v.idCliente = c.id
           )
)
ORDER BY c.nombre;

-- RESULTADO: Ana Gómez, María Torres


-- ============================================================
-- 4. VERIFICACIÓN PASO A PASO
-- ============================================================

-- Ver sucursales que visita cada cliente
SELECT c.nombre, s.nombre AS sucursal
FROM   Cliente c
JOIN   Visitan v    ON c.id = v.idCliente
JOIN   Sucursal s   ON s.id = v.idSucursal
ORDER BY c.nombre;

-- Ver productos inscritos por cliente y en qué sucursales están disponibles
SELECT
    c.nombre                             AS cliente,
    p.nombre                             AS producto,
    s.nombre                             AS sucursal_disponible,
    CASE
        WHEN vis.idCliente IS NOT NULL THEN 'SÍ visita'
        ELSE '❌ NO visita'
    END                                  AS cliente_visita_sucursal
FROM   Cliente c
JOIN   Inscripcion i     ON c.id = i.idCliente
JOIN   Producto p        ON p.id = i.idProducto
JOIN   Disponibilidad d  ON d.idProducto = p.id
JOIN   Sucursal s        ON s.id = d.idSucursal
LEFT JOIN Visitan vis    ON vis.idSucursal = d.idSucursal
                        AND vis.idCliente  = c.id
ORDER BY c.nombre, p.nombre, s.nombre;

/*
 RESULTADO DE LA VERIFICACIÓN:
 ┌─────────┬───────────┬──────────────────┬─────────────────────┐
 │ cliente │ producto  │ sucursal_disp.   │ cliente_visita_suc  │
 ├─────────┼───────────┼──────────────────┼─────────────────────┤
 │ Ana     │ Producto A│ Sucursal Norte   │ SÍ visita           │← todas ✅ → ANA SALE
 │ Ana     │ Producto A│ Sucursal Sur     │ SÍ visita           │
 ├─────────┼───────────┼──────────────────┼─────────────────────┤
 │ Carlos  │ Producto B│ Sucursal Norte   │ SÍ visita           │
 │ Carlos  │ Producto B│ Sucursal Sur     │ ❌ NO visita        │← NO cumple → CARLOS NO SALE
 │ Carlos  │ Producto B│ Sucursal Centro  │ ❌ NO visita        │
 ├─────────┼───────────┼──────────────────┼─────────────────────┤
 │ Luis    │ Producto B│ Sucursal Norte   │ ❌ NO visita        │← NO cumple → LUIS NO SALE
 │ Luis    │ Producto B│ Sucursal Sur     │ ❌ NO visita        │
 │ Luis    │ Producto B│ Sucursal Centro  │ SÍ visita           │
 ├─────────┼───────────┼──────────────────┼─────────────────────┤
 │ María   │ Producto C│ Sucursal Centro  │ SÍ visita           │← todas ✅ → MARÍA SALE
 └─────────┴───────────┴──────────────────┴─────────────────────┘
*/
