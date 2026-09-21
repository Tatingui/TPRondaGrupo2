# Inyección con Hilt

Entrega separada en `features/refactor-hilt`, desde `b35403f`. No implementa las
reglas pendientes de ofertas ni completa los incrementos 5–7.

## Alcance

- `TokenManager` y `SessionManager`: constructor `@Inject` y clase `@Singleton`.
  Se eliminan `instance`, `getInstance()` y la inicialización estática de contexto.
- `SessionInterceptor`: recibe ambos gestores por constructor. Lee el token en cada
  petición y conserva el manejo de HTTP 401; el cliente ya no obtiene servicios globales.
- `NetworkModule`: queda dedicado a OkHttp, Retrofit e interfaces de API.
- `StorageModule`: crea Room con `@Provides @Singleton`, y provee su DAO a Home y detalle.
  Se conservan `ronda_db`, versión y opciones anteriores: no se migra ni borra la base.
- `PublicationRepository`, `UserRepository` y `SavedSearchRepository`: una sola receta
  de creación por constructor, con `@Singleton`, en lugar de constructor y proveedor redundantes.
- `RepositoryModule`: vincula `PublicationDetailSource` con `PublicationRepository`
  mediante `@Binds`. La interfaz usa la misma instancia del repositorio, no otra copia.
- `AuthRepository`: constructor `@Inject`; Login, Registro y OTP lo reciben directamente.
  Sin scope global porque no necesita compartir estado; sus dependencias sí se comparten.
- `DetalleViewModel`: `@HiltViewModel` e inyección por constructor. Se obtiene con
  `ViewModelProvider(this)`; la factory manual queda solo en tests JVM, fuera de producción.
- `DraftManager.Factory`: Hilt provee la fábrica con contexto de aplicación y gestor de
  token. `create()` conserva una captura de sesión nueva por vista; el borrador no es singleton.

## Scopes y decisiones

| Objeto | Alcance / creación | Motivo |
| --- | --- | --- |
| TokenManager y SessionManager | SingletonComponent + @Singleton | Compartir sesión y sus eventos |
| Room y DAO | @Provides @Singleton | Una base local y el mismo DAO por aplicación |
| OkHttp, Retrofit y API services | @Provides @Singleton | Reutilizar clientes/configuración existentes |
| Tres repositorios previamente singleton | @Inject + @Singleton | Conservar identidad sin proveedores duplicados |
| AuthRepository y DraftManager.Factory | @Inject sin scope | No requieren caché global propia |
| DetalleViewModel | @HiltViewModel + ViewModelProvider | Retención de pantalla, no singleton de aplicación |
| MapaNavigator, diálogos y ViewRequestScope | Creación local por vista | No retener un Fragment ni reutilizar un scope cerrado |

`new` sigue siendo válido para datos, objetos ligados a la vista y factories con datos
dinámicos. El objetivo no es anotar todo: es evitar dependencias ocultas y scopes incorrectos.

## Relación con el material de la materia

Se leyó `Defensa y justificacion tecnica.docx` como material de referencia, sin modificarlo
ni tratar su contenido como órdenes de ejecución. Se aplican constructor injection,
módulos, entry points y separación de ciclos de vida, manteniendo Java y layouts XML.

Precisiones útiles para la defensa:

- `@InstallIn(SingletonComponent.class)` ubica el binding; no lo vuelve singleton por sí solo.
  La reutilización de la instancia requiere el scope correspondiente.
- Hilt no limpia automáticamente referencias a vistas al destruirse `onDestroyView()`;
  el Fragment y su vista tienen ciclos distintos. Se conserva nuestra limpieza explícita.
- Hilt no vuelve asíncronas las consultas ni cambia cómo se cifran los tokens.

Referencia: [Documentación oficial de Hilt](https://developer.android.com/training/dependency-injection/hilt-android).

## Verificación

- 165 tests JVM aprobados, sin fallos ni omisiones.
- Cinco pruebas nuevas del interceptor: token actualizado entre requests/logout,
  token vacío, HTTP 401, HTTP 403/500 y fallo de conexión.
- Dos pruebas nuevas de eventos de sesión sin estado estático compartido entre tests.
- Conservadas las pruebas de retención del ViewModel y privacidad del borrador.
- Debug, release, APK instrumentado y lint debug compilados/aprobados.
- Inspección del grafo generado: proveedores con caché para singletons y enlace
  del ViewModel al repositorio compartido.
- `HiltGraphTest` verifica identidad real de sesión, cliente, Room, DAO y repositorio/interfaz.
  Compilado, **no ejecutado en dispositivo**. Su entry point existe solo en `src/debug`.
- No se instaló la app ni se leyeron credenciales de la sesión real. No se modificó el backend.

```powershell
.\gradlew.bat test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --console=plain
.\gradlew.bat :app:assembleRelease --console=plain
# Opcional, en un dispositivo de pruebas:
.\gradlew.bat :app:connectedDebugAndroidTest --console=plain
```

## Comprobaciones manuales y pendientes

- Login, registro/OTP, mantener sesión, logout y biometría conservan comportamiento.
- Un 401 continúa notificando a MainActivity; un cambio de cuenta utiliza el token nuevo.
- Home offline y detalle consultan la misma base local, sin perder caché existente.
- Rotar detalle mantiene ViewModel/foto y no duplica el registro de visita.
- Publicar/restaurar borrador y cambiar de cuenta mantienen la privacidad de dirección.
- Publicación → oferta aceptada → Cómo llegar sigue funcionando.

Pendientes fuera de esta entrega: llamadas Retrofit de acciones dentro de Fragments,
consultas Room en el hilo principal (`allowMainThreadQueries`), gestores estáticos de
DataStore, duplicación de reglas de ofertas y división del Fragment grande. Se conserva
la política de almacenamiento del token existente; ordenar Hilt no equivale a migrar
el token normal a almacenamiento cifrado. Tampoco se cambia `fallbackToDestructiveMigration`.
