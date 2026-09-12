# Implementación: Manejo de Token Vencido

## Qué hice

Agregué el manejo automático de token JWT vencido. Antes, si el token expiraba, la app quedaba en una pantalla rota sin poder hacer nada. Ahora detecta el 401 del backend, le avisa al usuario con un mensaje y lo manda al login para que se loguee de nuevo.

## Cómo funciona

1. El usuario está usando la app normalmente.
2. El token JWT vence (está configurado a 24hs en el backend).
3. La próxima llamada al backend devuelve un 401 (Unauthorized).
4. El interceptor de OkHttp en `ApiClient` detecta el 401, limpia el token guardado y le avisa al `SessionManager`.
5. `MainActivity` está observando ese evento — muestra un Toast "Tu sesión expiró. Ingresá de nuevo." y navega al login limpiando todo el backstack.
6. El usuario se loguea de nuevo y sigue normalmente.

## Archivos modificados/creados

- **`network/SessionManager.java`** (nuevo) — singleton con `LiveData` que notifica a la UI cuando el backend devuelve 401.
- **`network/ApiClient.java`** — el interceptor de OkHttp ahora revisa el código de respuesta; si es 401, limpia el token y dispara el evento de sesión expirada.
- **`MainActivity.java`** — observa el `LiveData` de `SessionManager` y redirige al login cuando se dispara.
