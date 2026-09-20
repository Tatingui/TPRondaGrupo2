# Refactor incremental del detalle y Cómo llegar

Base inicial: `main` en `312ac6c` (arreglo de desplegables al publicar).
Durante el trabajo se integró `main` en `58b7975`: correcciones de DataStore y tarjetas
de Perfil/Historial, sin conflictos ni modificaciones a esos cambios.
Al cerrar el incremento 4 también se integró `dd418b7` (PR #59): aceptación
de ofertas mediante transacciones y navegación desde Mis Ofertas al detalle.
Se conserva R1: esa navegación no habilita Cómo llegar al propietario, aunque
los comentarios nuevos de Mis Ofertas describan ese comportamiento.
Se trabaja un incremento por vez. Los cambios de comportamiento se identifican
aparte de las extracciones que deben preservar el funcionamiento existente.

## Ramas e incrementos

| Incremento | Alcance | Rama | Estado |
| --- | --- | --- | --- |
| 0 | Recuperar pruebas y verificar compatibilidad de IDs | `features/refactor-detalle-base` | Implementado y verificado localmente |
| 1 | Extraer autorización de la acción y resolución del destino | `features/refactor-detalle-base` | Implementado y verificado localmente |
| 2 | Extraer integración con aplicaciones de mapas | `features/refactor-detalle-base` | Implementado y verificado localmente |
| 3 | Corregir ciclo de vida, callbacks y cancelación | `features/refactor-detalle-base` | Implementado y verificado localmente |
| 4 | Separar carga del detalle mediante repositorio y estado de pantalla | `features/refactor-detalle-base` | Implementado y verificado localmente |
| 5 | Extraer acciones en entregas separadas: preguntas, ofertas, gestión y favoritos | `features/refactor-detalle-acciones` | Pendiente |
| 6 | Ordenar persistencia, ejecución fuera del hilo principal y favoritos | `features/refactor-detalle-acciones` | Pendiente |
| 7 | Separar presentación en secciones y diálogos | `features/refactor-detalle-acciones` | Pendiente |

La segunda rama se creará desde una base que incluya los incrementos 0 a 4.

## Requisitos y criterios de aceptación

| ID | Requisito verificable | Incrementos previstos |
| --- | --- | --- |
| R1 | Dirección oculta nunca habilita mapas, aunque existan coordenadas. El botón sigue oculto al propietario. El backend conserva la autorización sobre datos privados. | 1, 2, 4 |
| R2 | Comprador autorizado con destino válido mantiene Cómo llegar en estado SOLD, ya que aceptar una oferta marca la publicación como vendida. | 1, 4, 5 |
| R3 | Priorizar coordenadas completas, finitas y dentro de rango; usar dirección no vacía como alternativa. El punto 0,0 es válido. | 1 |
| R4 | El Fragment delega URLs, paquetes e intents a un componente de mapas; presenta un error controlado si no se puede abrir el destino. | 2 |
| R5 | No actualizar vistas destruidas, acumular callbacks ni repetir escrituras por recrear la pantalla. Cancelación por operación, sin afectar a otras pantallas. | 3, 4, 5 |
| R6 | Respuestas antiguas no pisan estado reciente; cada acción evita envíos duplicados y representa carga y errores. | 4, 5 |
| R7 | Conservar información pública offline y compatibilidad del almacenamiento. No agregar permisos ni direcciones privadas a la caché compartida. Adaptar Home antes de retirar allowMainThreadQueries. | 0 (solo compatibilidad de IDs), 6 |
| R8 | Separar presentación, datos, reglas y navegación externa sin trasladar todas las responsabilidades a otra clase gigante. | 1 a 7 |
| R9 | Conservar los cambios integrados, contratos de datos e inyección existente. Reutilizar PublicationRepository; sin migración tecnológica de Hilt. | Todos |
| R10 | Cada incremento compila, pasa sus pruebas y registra alcance, evidencia y límites. | Todos |

## Incremento 0: evidencia y límites

- Corregidos los cinco usos de String incompatibles con Vendedor.id Long.
- Actualizadas las expectativas de IDs, incluida PerfilPublico, que hereda Vendedor.
- Verificada la identidad del vendedor desde campos resumidos y desde el JSON del detalle.
- ConvertersTest verifica lectura del ID numérico guardado como String en la caché anterior,
  escritura numérica y recuperación de sus campos, y lectura de un vendedor sin ID.
- No fue necesario modificar modelos, convertidores, esquema Room ni código de producción.
- Verificación: `./gradlew.bat test :app:assembleDebug --console=plain`.
- Resultado: 102 pruebas unitarias debug, cero fallos, errores u omisiones; APK debug compilado.
- `git diff --check` sin errores de whitespace.
- La prueba de compatibilidad ejercita el convertidor JSON usado por Room; no es una
  prueba instrumentada de una base instalada. No se ejecutó validación visual en dispositivo.
- Requisitos abordados: R9 y R10 para esta entrega; compatibilidad de IDs de R7.
  Las demás partes de R7 y los requisitos de mapas, ciclo de vida y arquitectura siguen pendientes.

## Incremento 1: reglas de Cómo llegar

- ComoLlegarResolver reúne R1, R2 y R3 sin depender de Android ni de Google Maps.
- Se priorizan coordenadas completas y válidas, incluida 0,0; ante valores inválidos
  se usa la dirección recortada. El permiso se verifica antes de resolver el destino.
- Publicacion conserva los datos y deja de construir destinos para mapas.
- Al ocultar el botón se elimina también su listener anterior.
- 21 casos de reglas; suite total: 120 pruebas aprobadas y APK debug compilado.

## Incremento 2: apertura de mapas

- MapaNavigator decide Google Maps y fallback geo; AndroidMapaLauncher adapta a intents.
- El Fragment solo solicita la apertura y presenta el error (R4). Se conserva el fallback
  existente; no se incorporó navegación web ni permisos de ubicación.
- Pruebas de orden de alternativas, fallos, destino vacío y codificación de caracteres.
- Suite total: 125 pruebas aprobadas; APK debug y APK de pruebas instrumentadas compilados.
- Dos pruebas instrumentadas cubren ACTION_VIEW, paquete, URI y ActivityNotFoundException;
  quedan para ejecución en dispositivo junto con la prueba manual de aplicaciones reales.

## Incremento 3: ciclo de vida

- ViewRequestScope cancela solicitudes pendientes y descarta resultados de una vista cerrada.
  Cada vista tiene su propio grupo; no se cancelan solicitudes ajenas.
- onDestroyView libera referencias, adaptador, callback de galería y diálogo activo.
- NetworkObserver registra al primer observador activo y desregistra al salir el último.
  Reconsulta conectividad validada en vez de inferirla de la pérdida de una red secundaria.
- Siete pruebas de cancelación, respuestas tardías y observadores. Suite total: 132 aprobadas;
  APK debug y pruebas instrumentadas compilados.
- R5 queda cubierto respecto de recursos de la vista. La conservación del estado de carga
  y el registro de visita frente a recreaciones se completan en el incremento 4.

## Incremento 4: lectura, estado y recreación

- PublicationRepository implementa PublicationDetailSource para detalle, preguntas y visita,
  con errores de red/HTTP diferenciados y cancelación por operación. Los métodos existentes
  que usa Perfil mantienen sus firmas.
- DetalleViewModel conserva la lectura y la foto seleccionada durante recreaciones; recibe
  el repositorio por factory y no retiene vistas, Context ni objetos de Retrofit.
- Cada nueva carga invalida la anterior y sus preguntas. No se aceptan respuestas de otro ID.
  Las preguntas se solicitan después del detalle, cuando ya se conoce al propietario.
- La vista observa con getViewLifecycleOwner y muestra carga, errores y reintento.
  Sin detalle confirmado, o ante 401/403/404, oculta acciones y dirección exacta.
  Un error transitorio de red conserva la información confirmada de la misma pantalla.
- La visita remota y los efectos locales pendientes no se repiten por recrear la vista.
  Al salir definitivamente se cancelan las lecturas. Tras cancelar una escritura pendiente,
  se revalida el detalle al volver, sin reenviar esa escritura automáticamente.
- Las acciones aún pertenecen al Fragment (incremento 5), pero notifican favorito/oferta al
  ViewModel para que una lectura iniciada antes de su confirmación no deshaga el resultado.
  Se preserva el bloqueo de botones durante operaciones en curso al renderizar nuevo estado.
- Se mantienen R1/R2/R3/R4; se completan R5 y R6 en la lectura y su integración con acciones.
  La extracción completa de acciones, persistencia asíncrona y división visual siguen en 5/6/7.
- 20 pruebas nuevas de repositorio y ViewModel; total: 152 unitarias aprobadas, cero omitidas.
  APK debug y APK de pruebas instrumentadas compilados; lintDebug finalizado correctamente.
  No se cambiaron dependencias ni configuración de Hilt.
- No se ejecutaron pruebas instrumentadas en dispositivo. La retención comprobada usa
  ViewModelStore; tras muerte del proceso se reconstruye desde argumentos y se consulta
  nuevamente al backend, no se promete retención en memoria entre procesos.

## Comprobación manual antes del push

- [ ] Abrir el detalle desde Home, favoritos y perfil público; verificar carga, vendedor y preguntas.
- [ ] Como propietario o comprador sin permiso, comprobar que no aparece Cómo llegar.
- [ ] Como comprador con oferta aceptada, abrir mapas incluso con la publicación vendida.
- [ ] Probar con Google Maps y con otra app como alternativa; sin apps, verificar el mensaje.
- [ ] Cambiar de foto, rotar y volver desde el perfil del vendedor: conservar foto y detalle.
- [ ] Salir durante una carga, volver y reintentar sin conexión; no cerrar la app ni mostrar datos viejos sobre nuevos.
- [ ] Favorito, oferta y pausar/reactivar: botones no se habilitan por una respuesta de lectura en curso.
- [ ] Comprobar en los registros del backend que rotar no repite POST de visita ni otras escrituras.
- [ ] Verificar Home offline y Perfil tras las correcciones de DataStore integradas de main.
- [ ] En Mis Ofertas, aceptar una oferta y comprobar la navegación al detalle y el historial;
  como vendedor, Cómo llegar sigue oculto. Verificar el destino desde la cuenta compradora.

Comandos utilizados para verificar cada entrega:

```powershell
.\gradlew.bat test :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

Con un dispositivo de pruebas disponible, el adaptador Android de mapas también se puede
verificar con `./gradlew.bat :app:connectedDebugAndroidTest`.

## Mejoras funcionales propuestas, separadas del refactor

- Fallback web después de las alternativas de mapas, con sus pruebas específicas.
- Captura de dirección exacta: implementada posteriormente por pedido del usuario,
  como texto copiado de Google Maps (sin autocompletado). Ver `entrega-y-ofertas.md`
  para alcance, pruebas y comprobaciones pendientes en el entorno real.

Estas mejoras son entregas funcionales separadas de los incrementos 0 a 4.
El fallback web continúa pendiente.
