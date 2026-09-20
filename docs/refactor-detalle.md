# Refactor incremental del detalle y Cómo llegar

Base de trabajo: `main` en `312ac6c` (incluye el arreglo de desplegables al publicar).
Se trabaja un incremento por vez. Los cambios de comportamiento se identifican
aparte de las extracciones que deben preservar el funcionamiento existente.

## Ramas e incrementos

| Incremento | Alcance | Rama | Estado |
| --- | --- | --- | --- |
| 0 | Recuperar pruebas y verificar compatibilidad de IDs | `features/refactor-detalle-base` | Implementado y verificado localmente |
| 1 | Extraer autorización de la acción y resolución del destino | `features/refactor-detalle-base` | Pendiente |
| 2 | Extraer integración con aplicaciones de mapas | `features/refactor-detalle-base` | Pendiente |
| 3 | Corregir ciclo de vida, callbacks y cancelación | `features/refactor-detalle-base` | Pendiente |
| 4 | Separar carga del detalle mediante repositorio y estado de pantalla | `features/refactor-detalle-base` | Pendiente |
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

## Mejoras funcionales propuestas, separadas del refactor

- Fallback web después de las alternativas de mapas, con sus pruebas específicas.
- Captura de dirección exacta al publicar, persistencia en borrador y envío al backend.
  Hoy el formulario y PublicationCreateRequest solo envían la zona. El cierre funcional
  requiere probar crear publicación, ofertar, aceptar y abrir destino como comprador,
  manteniendo la dirección oculta para terceros.

Estas mejoras no se consideran implementadas ni incluidas automáticamente en el incremento 0.
