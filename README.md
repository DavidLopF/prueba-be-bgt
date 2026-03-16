# BTG Pactual — API de Gestión de Fondos de Inversión

API REST para la gestión de fondos de inversión voluntaria (FPV) y fondos de inversión colectiva (FIC). Los clientes pueden registrarse, suscribirse a fondos, cancelar suscripciones y consultar su historial de transacciones.

---

## Tabla de contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Variables de entorno](#variables-de-entorno)
- [Autenticación](#autenticación)
- [Fondos disponibles](#fondos-disponibles)
- [Pruebas](#pruebas)
- [Reiniciar seeds](#reiniciar-seeds)

---

## Características

- Registro y autenticación de clientes con JWT
- Listado de fondos de inversión disponibles
- Suscripción a fondos con validación de saldo mínimo
- Cancelación de suscripciones con reembolso automático al saldo
- Historial de transacciones por cliente
- Notificaciones por email o SMS según preferencia del cliente
- Documentación interactiva con Swagger/OpenAPI
- Seeds automáticos al iniciar la aplicación

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Base de datos | MongoDB |
| Seguridad | Spring Security + JWT (HS256) |
| Documentación | SpringDoc OpenAPI 3 (Swagger) |
| Build | Maven |
| Testing | JUnit 5 + Mockito |
| Utilidades | Lombok |

---

## Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Ports & Adapters)**, manteniendo el dominio de negocio completamente desacoplado de la infraestructura.

```
src/main/java/com/enterprise/btgpactual/
│
├── domain/                    # Núcleo del negocio (sin dependencias externas)
│   ├── model/                 # Entidades: Cliente, Fondo, Transaccion
│   ├── exception/             # Excepciones de dominio
│   └── port/
│       ├── in/                # Casos de uso (interfaces de entrada)
│       └── out/               # Repositorios y servicios externos (interfaces de salida)
│
├── application/               # Implementación de casos de uso
│   └── usecase/
│
├── infrastructure/            # Adaptadores técnicos
│   ├── adapter/
│   │   ├── in/rest/           # Controladores REST
│   │   ├── persistence/       # Repositorios MongoDB
│   │   └── notification/      # Adaptadores Email / SMS
│   └── security/              # JWT, filtros y configuración de seguridad
│
└── shared/                    # DTOs, respuestas globales y configuración
```

---

## Requisitos previos

- Java 17+
- MongoDB corriendo en `localhost:27017` (o configurado via variable de entorno)
- Maven 3.x

---

## Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd BTG-Pactual

# 2. Compilar el proyecto
./mvnw clean package -DskipTests

# 3. Ejecutar
./mvnw spring-boot:run
```

La aplicación arranca en `http://localhost:8080`.

Al iniciar, los 5 fondos se insertan automáticamente en MongoDB si la colección está vacía.

---

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `MONGODB_URI` | URI de conexión a MongoDB | `mongodb://localhost:27017/btgpactual` |
| `MONGODB_DB` | Nombre de la base de datos | `btgpactual` |
| `JWT_SECRET` | Clave secreta JWT (mín. 32 caracteres) | valor de desarrollo (no usar en producción) |
| `JWT_EXPIRATION` | Tiempo de expiración del token en ms | `86400000` (24 horas) |

---


---

### Fondos (requiere JWT)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/fondos` | Listar todos los fondos disponibles |
| `POST` | `/fondos/{fondoId}/suscribir` | Suscribirse a un fondo |
| `DELETE` | `/fondos/{fondoId}/cancelar` | Cancelar suscripción a un fondo |
| `GET` | `/fondos/historial` | Ver historial de transacciones |

---

## Autenticación

Todos los endpoints de `/fondos` requieren un token JWT en el header:

```
Authorization: Bearer <token>
```

El token se obtiene al registrarse o iniciar sesión y tiene una vigencia de **24 horas**.

---

## Fondos disponibles

Los siguientes fondos se cargan automáticamente al iniciar la aplicación:

| ID | Nombre | Categoría | Monto mínimo |
|---|---|---|---|
| 1 | FPV_BTG_PACTUAL_RECAUDADORA | FPV | $75.000 |
| 2 | FPV_BTG_PACTUAL_ECOPETROL | FPV | $125.000 |
| 3 | DEUDAPRIVADA | FIC | $50.000 |
| 4 | FDO-ACCIONES | FIC | $250.000 |
| 5 | FPV_BTG_PACTUAL_DINAMICA | FPV | $100.000 |

> Cada cliente nuevo inicia con un saldo de **$500.000**.

---

## Documentación interactiva

Con la aplicación corriendo, accede a:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`

---

## Pruebas

```bash
./mvnw test
```

Los tests cubren los tres casos de uso principales con JUnit 5 y Mockito:

- `SuscribirFondoUseCaseImplTest` — suscripción exitosa, saldo insuficiente, suscripción duplicada, cliente/fondo no encontrado
- `CancelarSuscripcionUseCaseImplTest` — cancelación exitosa, suscripción no encontrada
- `ObtenerHistorialUseCaseImplTest` — historial de transacciones por cliente

---

## Reiniciar seeds

Para volver a cargar los datos iniciales, borra la colección de fondos en MongoDB y reinicia la aplicación:

```bash
mongosh mongodb://localhost:27017/btgpactual --eval "db.fondos.deleteMany({})"
./mvnw spring-boot:run
```

Para limpiar todos los datos:

```bash
mongosh mongodb://localhost:27017/btgpactual --eval "
  db.fondos.deleteMany({});
  db.clientes.deleteMany({});
  db.transacciones.deleteMany({});
"
```
