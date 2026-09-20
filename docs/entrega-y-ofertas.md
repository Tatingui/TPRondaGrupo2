# Dirección de entrega y consulta de ofertas

## Requisito confirmado

El vendedor busca el punto de entrega en Google Maps, usa **Copiar la dirección**
y pega el texto completo al publicar. No se usa el enlace de Compartir ni un
autocompletado dentro de Ronda. No se incorpora proveedor, API key ni permisos de ubicación.

## Implementación

- Zona pública y dirección exacta privada son campos distintos. El formulario pide
  la dirección completa, la muestra en la revisión y envía `address` en el JSON.
- Android y el DTO del backend rechazan dirección vacía, enlaces comunes y textos
  de más de 255 caracteres. Esto valida entrada, no confirma geográficamente el domicilio.
- Se conserva en el borrador durante la misma sesión. Una sesión distinta no restaura
  la dirección privada; debe pegarla nuevamente. Solo se guarda una huella SHA-256 de
  sesión junto al borrador, no otra copia del token. Los campos públicos del borrador
  anterior mantienen su compatibilidad. El borrador local no es almacenamiento cifrado.
- El backend persiste el texto recortado en el campo existente `address`. No se inventan
  coordenadas ni se cambia el esquema de la base para esta entrega.
- El detalle conserva su autorización: dirección visible al propietario o al comprador
  con oferta aceptada; no se devuelve a terceros ni se agrega al resumen público/caché Room.
- Cómo llegar usa las coordenadas válidas si ya existen; para publicaciones nuevas de
  este formulario, sin coordenadas, envía la dirección completa como destino a Maps.
  Google Maps interpreta ese texto; la app no determina la posición física actual.
- Mis Ofertas permite abrir la publicación, incluso vendida, mediante **Ver publicación**.
  El comprador puede acceder desde Enviadas y luego usar Cómo llegar si está autorizado.

## Ofertas: corrección y límite del diagnóstico

Se retiró `checkAndUpdateExpirations` de las consultas `readOnly`: recorría todas las
ofertas e intentaba persistir cambios durante una lectura. El DTO ahora calcula el estado
efectivo de vencimiento sin escrituras, incluyendo contraofertas vencidas. Responder una
oferta también comprueba ese estado efectivo para no responder una oferta ya vencida.

La pantalla diferencia lista vacía de error, muestra código HTTP y ofrece reintento.
Al cambiar de pestaña cancela la carga anterior y descarta respuestas viejas. Sus callbacks
quedan ligados a la vista y las respuestas a acciones recargan la lista, sin índices obsoletos.

No se ha confirmado que el error de la captura tenga esa misma causa: no se obtuvo el
código HTTP autenticado ni la excepción de la instancia real. La consulta de salud local
respondió 200 en `localhost:8081/api/health`. La lectura del token de depuración fue
bloqueada por revisión de seguridad y no se ejecutó. Se necesita el HTTP de la pantalla
actualizada o la excepción de la consola del backend, sin tokens ni contraseñas.

## Verificación

- Android: 158 pruebas unitarias aprobadas, APK debug y APK instrumentado compilados,
  lintDebug aprobado. No se instaló el APK ni se ejecutaron pruebas visuales en el emulador.
- Backend: 15 pruebas focalizadas aprobadas (`DeliveryFlowTest`, `OfferServiceTest`,
  `PublicationDetailTest`). Se ejercita HTTP con seguridad y persistencia H2:
  publicar, ofertar, listar por usuario, denegar aceptación a un tercero, aceptar como
  vendedor y recuperar la dirección como comprador, manteniéndola oculta a terceros.
- Se verifica serialización Android, conservación de dirección en borrador y exclusión
  de la dirección al cambiar de sesión, además del destino codificado enviado a Maps.
- La suite completa del backend no está verde: tiene seis casos fallidos ajenos a los
  cambios de esta entrega (cinco en `AuthServiceTest`, uno en `PublicationServiceTest`).
  Incluyen un mock de EmailService ausente, login de fixture no verificado y favoritos.
  No se cambiaron ni se deshabilitaron esos tests para ocultar sus fallos.

```powershell
# Desde la raíz
.\gradlew.bat test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --console=plain
# Desde backend, con Maven disponible
mvn -Dtest=DeliveryFlowTest,OfferServiceTest,PublicationDetailTest test
```

## Prueba local pendiente

1. Actualizar/compilar el backend local y volver a ejecutarlo; ejecutar la app actualizada.
   No borrar la base ni ejecutar scripts de reinicialización.
2. Usar dos cuentas del **mismo backend/base**. Los backends independientes de compañeros
   no comparten publicaciones, ofertas ni usuarios aunque tengan el mismo código.
3. Publicar un artículo nuevo pegando dirección completa (calle, altura, ciudad y provincia).
   Las publicaciones antiguas sin dirección no se rellenan automáticamente.
4. Ofertar como comprador. Como vendedor, abrir Recibidas y aceptar.
5. Como comprador, abrir Enviadas → Ver publicación → Cómo llegar y verificar el destino.
6. Probar sin conexión/cambiando de pestaña y verificar error/reintento y ausencia de mezclas.
7. Si falla Recibidas, registrar el HTTP que muestra la pantalla y la excepción del backend.

Esta entrega no implementa los incrementos 5–7 del refactor ni completa negociación de
contraofertas. No incluye despliegue, push ni PR.

## Seguimiento: comprador aceptado sin botón

La pantalla confundía autorización ausente con dirección ausente: incluso con
`addressVisible=true`, si `address` era null mostraba el mensaje de esperar aceptación.
Ahora explica que la publicación no tiene una dirección exacta guardada. La zona nunca
se convierte automáticamente en destino. Para probar publicaciones nuevas, pegar dirección
en el campo nuevo; una publicación antigua sin esos datos seguirá sin destino de mapas.

También se reprodujo con un test un caso distinto: la consulta tomaba solo la última oferta
del comprador y perdía la autorización si había otra posterior a una aceptada. Ahora busca
primero su oferta aceptada para esa publicación y, si no existe, su última oferta. El test
falló antes del cambio y pasa con la corrección, manteniendo ocultos los datos a terceros.
Esto no confirma que hubiera múltiples ofertas en la publicación de la captura.
