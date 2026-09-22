# Ronda — Marketplace de Compraventa

**Ronda** es una aplicación de marketplace para Android que permite a los usuarios publicar, buscar, ofertar y concretar compras y ventas de productos de segunda mano. Desarrollada como Trabajo Práctico Obligatorio para la materia *Desarrollo de Aplicaciones I* — Universidad Argentina de la Empresa (UADE), 2025.

---

## Tabla de contenidos

1. [Descripción general](#descripción-general)
2. [Arquitectura del sistema](#arquitectura-del-sistema)
3. [Stack tecnológico](#stack-tecnológico)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Flujos principales](#flujos-principales)
6. [Comunicación cliente–servidor](#comunicación-clienteservidor)
7. [Persistencia y almacenamiento](#persistencia-y-almacenamiento)
8. [Autenticación y seguridad](#autenticación-y-seguridad)
9. [Módulos y features](#módulos-y-features)
10. [Configuración del entorno](#configuración-del-entorno)
11. [Ejecución](#ejecución)
12. [Testing](#testing)
13. [Git Hooks y flujo de trabajo](#git-hooks-y-flujo-de-trabajo)
14. [Equipo](#equipo)

---

## Descripción general

Ronda conecta compradores y vendedores en un flujo completo de marketplace:

1. Un **vendedor** publica un producto con fotos, descripción, precio y ubicación.
2. Los **compradores** navegan el catálogo, filtran por categoría/estado/precio, y realizan **ofertas**.
3. El vendedor **acepta o rechaza** ofertas; al aceptar, se genera una **transacción**.
4. Ambas partes pueden **calificar** la experiencia dentro de los 7 días posteriores a la entrega.
5. Los usuarios gestionan su **perfil**, foto de perfil, y consultan su **historial** de compras y ventas con filtros por fecha.

Funcionalidades complementarias incluyen autenticación biométrica, verificación por OTP vía email, favoritos, notificaciones y chat entre comprador y vendedor.

---

## Arquitectura del sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                        ANDROID (app/)                           │
│                                                                 │
│   Activity única ──► Fragments (Navigation Component)           │
│        │                                                        │
│        ├── UI Layer      : Fragments + ViewModels (MVVM)        │
│        ├── Domain Layer  : Repositories (interfaz)              │
│        └── Data Layer    : Retrofit (red) + Room (local)        │
│                                                                 │
│   Inyección de dependencias: Hilt                               │
│   Imágenes: Glide  │  Recorte: UCrop  │  Biometría: BiometricX │
└──────────────────────────┬──────────────────────────────────────┘
                           │  HTTP / JSON
                           │  (Retrofit + OkHttp)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     BACKEND (backend/)                           │
│                                                                 │
│   Spring Boot 3.3.5  ──► Java 17                                │
│        │                                                        │
│        ├── Controllers   : REST endpoints (@RestController)     │
│        ├── Services      : Lógica de negocio                    │
│        ├── Repositories  : Spring Data JPA                      │
│        ├── Security      : Spring Security + JWT (jjwt 0.12.6)  │
│        └── DTOs          : Objetos de transferencia              │
│                                                                 │
│   Email: Spring Mail (OTP)  │  Passwords: BCrypt                │
└──────────────────────────┬──────────────────────────────────────┘
                           │  JPA / Hibernate
                           ▼
                    ┌──────────────┐
                    │  MySQL 8.x   │
                    │  Schema:     │
                    │  ronda       │
                    └──────────────┘
```

### Patrón MVVM en Android

```
Fragment (Vista)
    │  observa LiveData
    ▼
ViewModel
    │  invoca métodos
    ▼
Repository (interfaz)
    │
    ├──► Retrofit Service  ──► Backend REST API
    │
    └──► Room DAO           ──► SQLite local
```

Cada feature sigue esta estructura. El `ViewModel` expone `LiveData` que el `Fragment` observa de forma reactiva. El `Repository` abstrae si los datos vienen de la red (Retrofit) o del caché local (Room).

---

## Stack tecnológico

### Android

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 11 (source/target) |
| Min SDK | Android | API 26 (Oreo 8.0) |
| Target/Compile SDK | Android | API 37 |
| Build tool | Android Gradle Plugin | 9.3.1 |
| Inyección de dependencias | Hilt (Dagger) | 2.60.1 |
| Navegación | Navigation Component | 2.8.0 |
| HTTP Client | Retrofit 2 + OkHttp | 2.9.0 / 4.12.0 |
| Imágenes | Glide | 4.16.0 |
| Recorte de fotos | UCrop | 2.2.9 |
| Base de datos local | Room | 2.6.1 |
| Preferencias | DataStore Preferences | 1.1.1 |
| Biometría | BiometricPrompt | 1.2.0-alpha05 |
| Seguridad local | EncryptedSharedPreferences | — |
| Tabs/Paging | ViewPager2 | 1.1.0 |
| UI | Material Design 3 | — |

### Backend

| Componente | Tecnología | Versión |
|---|---|---|
| Framework | Spring Boot | 3.3.5 |
| Lenguaje | Java | 17 |
| Build tool | Maven | — |
| Base de datos | MySQL | 8.x |
| ORM | Hibernate (JPA) | ddl-auto=update |
| Autenticación | JWT (jjwt) | 0.12.6 |
| Seguridad | Spring Security | — |
| Email | Spring Mail | — |
| Hashing | BCrypt | — |
| Testing | H2 (in-memory) | — |

---

## Estructura del proyecto

```
TPRondaGrupo2/
│
├── app/                              # Módulo Android
│   └── src/main/
│       ├── java/com/ronda/app/
│       │   ├── di/                   # Módulos Hilt (NetworkModule, DatabaseModule)
│       │   ├── model/                # Entidades y DTOs del cliente
│       │   ├── network/              # RetrofitClient, ApiService, interceptors
│       │   ├── repository/           # Repositorios (red + local)
│       │   ├── db/                   # Room (DAOs, entidades, AppDatabase)
│       │   └── ui/
│       │       ├── auth/             # Login, registro, OTP, biometría
│       │       ├── home/             # Feed principal, búsqueda, filtros
│       │       ├── publish/          # Wizard de publicación (3 pasos)
│       │       ├── detalle/          # Detalle de publicación, perfil vendedor
│       │       ├── ofertas/          # Crear, listar, aceptar/rechazar ofertas
│       │       ├── historial/        # Historial compras/ventas con filtros
│       │       ├── calificaciones/   # Calificar transacciones
│       │       ├── favoritos/        # Publicaciones guardadas
│       │       ├── perfil/           # Perfil propio, edición, foto
│       │       ├── chat/             # Mensajería comprador-vendedor
│       │       └── notificaciones/   # Centro de notificaciones
│       └── res/
│           ├── layout/               # Layouts XML de fragments y vistas
│           ├── navigation/           # nav_graph.xml (grafo de navegación)
│           ├── values/               # strings.xml, colors.xml, themes.xml
│           └── drawable/             # Iconos, fondos, shapes
│
├── backend/                          # Módulo Spring Boot
│   └── src/
│       ├── main/java/com/ronda/backend/
│       │   ├── controller/           # REST Controllers (6)
│       │   ├── service/              # Servicios de negocio (8+)
│       │   ├── repository/           # Spring Data JPA Repositories
│       │   ├── model/                # Entidades JPA (@Entity)
│       │   ├── dto/                  # Data Transfer Objects
│       │   ├── security/             # JWT filter, SecurityConfig
│       │   └── config/               # Configuración general
│       ├── main/resources/
│       │   └── application.properties
│       └── test/                     # Tests unitarios (13 clases)
│
├── hooks/                            # Git hooks (pre-push)
├── db/                               # Scripts SQL de inicialización
├── gradlew / gradlew.bat             # Gradle wrapper
├── setup-hooks.bat                   # Instalación de hooks en Windows
└── build.gradle / settings.gradle    # Configuración raíz de Gradle
```

---

## Flujos principales

### 1. Registro y autenticación

```
Usuario                  App                          Backend
  │                       │                              │
  ├── Completa form ────► │                              │
  │                       ├── POST /api/auth/register ──►│
  │                       │                              ├── Hashea password (BCrypt)
  │                       │                              ├── Genera OTP (6 dígitos)
  │                       │                              ├── Envía email (Spring Mail)
  │                       │◄── 200 OK ──────────────────┤
  │◄── Pantalla OTP ─────┤                              │
  ├── Ingresa OTP ──────► │                              │
  │                       ├── POST /api/auth/verify ────►│
  │                       │                              ├── Valida OTP
  │                       │                              ├── Genera JWT
  │                       │◄── { token } ───────────────┤
  │                       ├── Guarda token (EncryptedSP) │
  │                       ├── Ofrece activar biometría   │
  │◄── Home ─────────────┤                              │
```

**Login posterior:** el usuario puede autenticarse con email/contraseña o, si lo activó, con huella digital / reconocimiento facial (BiometricPrompt). El token JWT se adjunta automáticamente a cada request mediante un interceptor de OkHttp.

### 2. Publicación de un producto

```
Paso 1: Datos básicos          Paso 2: Fotos              Paso 3: Confirmación
┌─────────────────────┐   ┌──────────────────────┐   ┌──────────────────────┐
│ • Título             │   │ • Galería / Cámara   │   │ • Resumen completo   │
│ • Descripción        │   │ • Recorte (UCrop)    │   │ • Confirmar y        │
│ • Precio             │──►│ • Mín. 1 foto        │──►│   publicar           │
│ • Categoría          │   │ • Preview en grid    │   │                      │
│ • Estado (Nuevo/     │   │                      │   │ POST /api/           │
│   Usado/Como nuevo)  │   │                      │   │   publications       │
│ • Ubicación          │   │                      │   │   (multipart)        │
└─────────────────────┘   └──────────────────────┘   └──────────────────────┘
```

El wizard (`PublishWizardFragment`) valida en cada paso: datos obligatorios en el paso 1, al menos una foto en el paso 2. Las imágenes se envían como `multipart/form-data` al backend, que las almacena y devuelve URLs.

### 3. Flujo de oferta → transacción → calificación

```
Comprador                        Vendedor                      Sistema
    │                                │                             │
    ├── Envía oferta ───────────────►│                             │
    │   (precio, mensaje)            │                             │
    │                                ├── Ve oferta en bandeja      │
    │                                ├── Acepta ──────────────────►│
    │                                │                             ├── Crea Transaction
    │                                │                             ├── Marca pub SOLD
    │◄── Notificación: aceptada ────┤                             │
    │                                │                             │
    │   ◄── 7 días desde entrega ──►│                             │
    │                                │                             │
    ├── Califica (1-5 ★ + texto) ──►│                             │
    │                                ├── Califica ────────────────►│
    │                                │                             ├── Guarda Rating
    │                                │                             ├── Actualiza promedio
```

La ventana de calificación se calcula como: `delivery_date` + 7 días ≥ fecha actual. En el historial, las transacciones calificables muestran un badge **"Calificar"**.

### 4. Historial de compras y ventas

```
HistorialFragment
    │
    ├── Tab VENTAS: transacciones donde user = seller_id
    ├── Tab COMPRAS: transacciones donde user = buyer_id
    │
    └── Filtros por fecha (Desde / Hasta)
        │
        ├── Filtrado server-side: GET /api/transactions/user/{tab}
        │   con parámetros ?from=...&to=...
        │
        └── Cada item muestra:
            ├── Imagen del producto
            ├── Título y monto
            ├── Fecha de entrega
            └── Badge "Calificar" (si delivery_date + 7d ≥ hoy)
```

---

## Comunicación cliente–servidor

### Capa de red (Android)

```
Fragment ──► ViewModel ──► Repository ──► ApiService (interfaz Retrofit)
                                              │
                                              ▼
                                         RetrofitClient
                                              │
                                         OkHttpClient
                                              ├── AuthInterceptor (agrega JWT)
                                              ├── LoggingInterceptor
                                              └── Base URL: http://10.0.2.2:8081/api/
                                                  (emulador → localhost)
```

**Serialización:** Gson convierte automáticamente JSON ↔ objetos Java.

### Endpoints principales del backend

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/auth/register` | Registro con envío de OTP |
| `POST` | `/api/auth/verify` | Verificación OTP y obtención de JWT |
| `POST` | `/api/auth/login` | Login con credenciales |
| `GET` | `/api/publications` | Listado de publicaciones (con filtros) |
| `GET` | `/api/publications/{id}` | Detalle de publicación |
| `POST` | `/api/publications` | Crear publicación (multipart) |
| `PUT` | `/api/publications/{id}` | Editar publicación |
| `DELETE` | `/api/publications/{id}` | Eliminar publicación |
| `GET` | `/api/publications/user/{userId}` | Publicaciones de un usuario |
| `POST` | `/api/offers` | Crear oferta |
| `GET` | `/api/offers/publication/{id}` | Ofertas de una publicación |
| `PUT` | `/api/offers/{id}/accept` | Aceptar oferta |
| `PUT` | `/api/offers/{id}/reject` | Rechazar oferta |
| `GET` | `/api/transactions/user/sales` | Historial de ventas |
| `GET` | `/api/transactions/user/purchases` | Historial de compras |
| `POST` | `/api/ratings` | Calificar transacción |
| `GET` | `/api/users/{id}/profile` | Perfil público |
| `PUT` | `/api/users/profile` | Editar perfil propio |
| `POST` | `/api/users/profile/image` | Subir foto de perfil |
| `GET` | `/api/favorites` | Listar favoritos |
| `POST` | `/api/favorites/{pubId}` | Agregar a favoritos |
| `DELETE` | `/api/favorites/{pubId}` | Quitar de favoritos |

Todos los endpoints (excepto auth) requieren el header `Authorization: Bearer <JWT>`.

---

## Persistencia y almacenamiento

### Base de datos remota — MySQL (Backend)

Esquema principal `ronda`:

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│    users      │     │   publications    │     │    offers     │
├──────────────┤     ├──────────────────┤     ├──────────────┤
│ id (PK)       │◄───┤ seller_id (FK)    │◄───┤ pub_id (FK)   │
│ email         │     │ title             │     │ buyer_id (FK) │
│ password_hash │     │ description       │     │ seller_id(FK) │
│ first_name    │     │ price             │     │ offered_price │
│ last_name     │     │ status (enum)     │     │ message       │
│ profile_img   │     │ state (enum)      │     │ status (enum) │
│ otp_code      │     │ location          │     │ created_at    │
│ verified      │     │ category_id (FK)  │     │ expires_at    │
└──────────────┘     │ created_at        │     └──────────────┘
                      └──────────────────┘            │
                             │                         │
                      ┌──────────────────┐     ┌──────────────┐
                      │ publication_     │     │ transactions  │
                      │ images           │     ├──────────────┤
                      ├──────────────────┤     │ pub_id (FK)   │
                      │ pub_id (FK)      │     │ buyer_id (FK) │
                      │ image_url        │     │ seller_id(FK) │
                      └──────────────────┘     │ offer_id (FK) │
                                                │ final_amount  │
                      ┌──────────────────┐     │ delivery_date │
                      │    ratings       │     └──────────────┘
                      ├──────────────────┤
                      │ transaction_id   │     ┌──────────────┐
                      │ rater_id (FK)    │     │  categories   │
                      │ rated_id (FK)    │     ├──────────────┤
                      │ score (1-5)      │     │ id (PK)       │
                      │ comment          │     │ name          │
                      └──────────────────┘     └──────────────┘

                      ┌──────────────────┐
                      │   favorites      │
                      ├──────────────────┤
                      │ user_id (FK)     │
                      │ publication_id   │
                      └──────────────────┘
```

**Categorías precargadas:** Deportes, Hogar, Electrónica, Ropa, Otros.

Hibernate se encarga del DDL con `ddl-auto=update`, por lo que el esquema se genera y actualiza automáticamente al iniciar el backend.

### Base de datos local — Room (Android)

Room se usa como caché offline para:
- Publicaciones recientes consultadas
- Datos de perfil del usuario logueado
- Favoritos (sincronizados con el backend)

### Preferencias — DataStore + EncryptedSharedPreferences

| Store | Contenido |
|---|---|
| **DataStore Preferences** | Configuración de la app (theme, filtros guardados, flags de onboarding) |
| **EncryptedSharedPreferences** | Token JWT, datos sensibles de sesión |
| **SharedPreferences** | Preferencia de biometría activada/desactivada |

---

## Autenticación y seguridad

```
┌──────────────────────────────────────────────────────────┐
│                    Flujo de seguridad                      │
│                                                           │
│  1. Registro → BCrypt hash de password                    │
│  2. OTP por email (Spring Mail) → verificación            │
│  3. Login → genera JWT (jjwt 0.12.6)                      │
│  4. Cada request → AuthInterceptor agrega Bearer token    │
│  5. Backend → JwtAuthFilter valida token                  │
│  6. Biometría (opcional) → BiometricPrompt desbloquea     │
│     token almacenado en EncryptedSharedPreferences         │
└──────────────────────────────────────────────────────────┘
```

- **JWT** incluye el ID y email del usuario. Expiración configurable en `application.properties`.
- **Spring Security** protege todas las rutas excepto `/api/auth/**`.
- **Biometría** no reemplaza la autenticación; simplemente evita re-ingresar credenciales al abrir la app, desbloqueando el token almacenado localmente.

---

## Módulos y features

| # | Feature | Descripción |
|---|---|---|
| 1 | **Autenticación** | Registro con OTP por email, login, logout |
| 2 | **Biometría** | Login con huella digital / reconocimiento facial |
| 3 | **Feed / Home** | Grid de publicaciones con búsqueda y filtros |
| 4 | **Publicar** | Wizard de 3 pasos con carga de fotos y UCrop |
| 5 | **Detalle** | Vista completa de publicación con fotos en carrusel |
| 6 | **Perfil público** | Ver perfil, publicaciones y calificación de un usuario |
| 7 | **Perfil propio** | Editar datos personales y foto de perfil |
| 8 | **Ofertas** | Enviar, listar, aceptar y rechazar ofertas |
| 9 | **Historial** | Compras y ventas con filtro por fecha |
| 10 | **Calificaciones** | Calificar contraparte dentro de los 7 días post-entrega |
| 11 | **Favoritos** | Guardar y quitar publicaciones de favoritos |
| 12 | **Chat** | Mensajería entre comprador y vendedor |
| 13 | **Notificaciones** | Centro de notificaciones de la app |

---

## Configuración del entorno

### Requisitos previos

- **Android Studio** Ladybug (2024.x) o superior
- **JDK 17** (para el backend)
- **JDK 11** (source/target del módulo Android)
- **MySQL 8.x** corriendo en localhost
- **Git** con soporte para hooks

### Base de datos

```sql
CREATE DATABASE ronda;
CREATE USER 'ronda'@'localhost' IDENTIFIED BY 'ronda';
GRANT ALL PRIVILEGES ON ronda.* TO 'ronda'@'localhost';
FLUSH PRIVILEGES;
```

O ejecutar el script provisto en `db/` que automatiza la creación.

### Variables del backend (`application.properties`)

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/ronda
spring.datasource.username=ronda
spring.datasource.password=ronda
spring.jpa.hibernate.ddl-auto=update
```

---

## Ejecución

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

El servidor inicia en `http://localhost:8081`. Hibernate crea/actualiza las tablas automáticamente.

### Android

1. Abrir el proyecto raíz en Android Studio.
2. Verificar que el backend esté corriendo.
3. Ejecutar en un emulador (la app usa `10.0.2.2:8081` para conectarse al localhost del host).
4. Para dispositivo físico, actualizar `BASE_URL` en `BuildConfig` a la IP de la máquina.

---

## Testing

### Backend

```bash
cd backend
./mvnw test
```

El backend cuenta con **13 clases de test** que cubren servicios y controladores. Los tests usan **H2 (base in-memory)** para no depender de MySQL.

### Android

```bash
./gradlew test
```

Tests unitarios del módulo Android ejecutados como parte del pipeline de CI local (ver Git Hooks).

---

## Git Hooks y flujo de trabajo

El proyecto incluye un hook `pre-push` que se instala ejecutando:

```bash
# Windows
setup-hooks.bat

# Linux/Mac
chmod +x hooks/pre-push && cp hooks/pre-push .git/hooks/
```

### Comportamiento del hook `pre-push`

| Verificación | Acción |
|---|---|
| **Branch protection** | Bloquea pushes directos a `main`; requiere Pull Request |
| **Auto-rebase** | Antes de pushear, hace `git pull --rebase` automático |
| **Test gate** | Ejecuta `./gradlew test`; si falla, aborta el push |

Esto garantiza que `main` siempre tenga código que compila y pasa los tests.

---

## Equipo

Proyecto desarrollado por el Grupo 2 de Desarrollo de Aplicaciones I — UADE, 2025.

- Santiago Emanuel Gonzalez 
- Isnardo Julián Ezequiel 
- Agustin Chiaravalli
- Valentin Gonzalez 
- Michelle Perna Jazmin 

---

*Ronda — Donde lo que ya no usás encuentra un nuevo dueño.*
