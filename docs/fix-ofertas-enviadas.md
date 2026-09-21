# Ofertas enviadas: vendedores heredados y estado de pantalla

El registro real muestra `EntityNotFoundException: User with id 0` durante
`OfferRepository.findByBuyer`, y un fallo al crear la FK `offers.seller_id`.
Una referencia invalida impide cargar la lista completa, aunque la compra nueva
exista en el historial. No se atribuye el dato invalido a una oferta concreta sin
inspeccionar la base.

## Reparacion al iniciar el backend MySQL

`LegacyOfferSellerRepair` trabaja con JDBC para no hidratar entidades invalidas.
Solo reemplaza vendedores inexistentes (incluido NULL) por el vendedor existente
de su publicacion. Preserva vendedores validos, estados, importes, compradores,
transacciones e IDs. Es idempotente.

La actualizacion y la comprobacion de referencias pendientes comparten una
transaccion. Si alguna publicacion no permite recuperar un vendedor valido, se
revierte toda la reparacion y se interrumpe el arranque con un error descriptivo.
No se eliminan registros ni se inventan usuarios.

`OfferSellerIntegrityStartup` ejecuta la reparacion despues de la inicializacion
de Hibernate. Luego comprueba la FK mediante information_schema
(independientemente de su nombre) y la crea solo si falta. Esta parte es especifica
de MySQL. El DDL se ejecuta despues del commit de la reparacion: si falla, los datos
ya reparados permanecen, pero el arranque falla para no anunciar integridad falsa.

En el primer arranque puede aparecer el warning previo de Hibernate sobre la FK:
la reparacion se realiza despues. El mensaje final esperado es
`Integridad de vendedores de ofertas verificada. Ofertas reparadas: N`.
El siguiente arranque debe informar cero reparaciones.

La propiedad `ronda.maintenance.offer-seller-repair=false` permite desactivar este
paso; esta desactivado para el perfil de pruebas H2. No desactivarlo para ocultar
errores de datos en la instancia afectada.

## Android

El ViewModel limpia la lista al iniciar cada consulta, invalida la llamada anterior
antes de cancelarla y acepta callbacks solo de la llamada vigente. Al destruir el
ViewModel cancela la carga. No se agregan reglas de red al Fragment ni al adaptador.

## Verificacion y puesta en uso

Pruebas JDBC H2: referencias cero, inexistentes y NULL, preservacion de vendedores
validos/estado/importe, idempotencia y rollback ante publicacion invalida.
Pruebas del arranque: orden reparacion/FK, no duplicacion y propagacion de errores.
Pruebas Android: error 500 tras cambiar de pestaña y respuestas tardias.

Actualizar y reiniciar el backend con esta rama y actualizar el APK. Verificar
el mensaje de reparacion, luego comprador → Mis Ofertas → Enviadas → oferta
aceptada → Ver publicacion → Como llegar. No hace falta recrear la compra.
Las pruebas automatizadas no sustituyen la comprobacion de la base MySQL real
ni la apertura de Maps en el dispositivo.
