# Implementación: Login con OTP, Sesión Persistente y Cerrar Sesión

## Login con OTP real por email

El backend valida que el usuario verifique su email antes de poder loguearse. Cuando alguien se registra, se le genera un código OTP de 6 dígitos que se envía por email usando SMTP de Gmail. Hasta que no ingrese ese código, no puede entrar.

**Flujo completo:**

1. El usuario se registra con email y contraseña → el backend genera un OTP, lo guarda en la base y lo envía al email usando `JavaMailSender`.
2. El usuario ingresa el OTP en la app → el backend lo verifica, marca `emailVerified = true` y devuelve un JWT.
3. Si intenta loguearse sin haber verificado, el backend le reenvía un nuevo OTP automáticamente y responde con "Email no verificado". La app Android detecta ese mensaje y lo manda directo a la pantalla de OTP en vez de mostrar un error genérico.

### Configuración de credenciales SMTP — `application.properties` + `start-backend.bat`

El problema que tuvimos fue que Maven (mediante `spring-boot:run`) sobreescribía `application.properties` en cada ejecución, pisando las credenciales reales con los valores fuente. La solución fue usar la sustitución de variables de Spring Boot:

```properties
spring.mail.username=${MAIL_USERNAME:TU_EMAIL@gmail.com}
spring.mail.password=${MAIL_PASSWORD:TU_APP_PASSWORD_ACA}
```

Y en `start-backend.bat`, antes del comando de Maven, se setean las variables de entorno con las credenciales reales:

```bat
REM --- Credenciales de email (cada dev pone las suyas aca) ---
set MAIL_USERNAME=santiagoemanuelgonzalez6@gmail.com
set MAIL_PASSWORD=csqnofxmymmofxcm
```

Así Maven puede sobreescribir el archivo cuantas veces quiera: los valores por defecto son placeholders, y las credenciales reales entran por variable de entorno al ejecutar el `.bat`. Cada desarrollador pone las suyas en su copia local del `.bat` (que ya está en `.gitignore`).

El email se envía con la cuenta de Gmail del desarrollador como emisor. Para que funcione hay que generar una "Contraseña de aplicación" desde la cuenta de Google (Seguridad → Verificación en 2 pasos → Contraseñas de aplicaciones).

---

## Mantener sesión iniciada

En `MainActivity.java`, al iniciar la app se revisa si hay un token JWT guardado en `SharedPreferences` (a través de `TokenManager`). Si existe, se salta el login y se navega directo al Home:

```java
String token = TokenManager.getInstance().getToken();
if (token != null && !token.isEmpty()) {
    navController.navigate(R.id.action_login_to_home);
}
```

El token se guarda automáticamente cuando el login o la verificación de OTP son exitosos (ya estaba implementado en `LoginFragment` y `OtpFragment`).

---

## Botón de cerrar sesión

Se agregó un botón rojo "Cerrar sesión" al final de la pantalla de perfil (`fragment_perfil_usuario.xml`). En `ProfileFragment.java`, el click handler limpia el token y navega al login limpiando todo el backstack para que no pueda volver atrás con el botón de Android:

```java
view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
    TokenManager.getInstance().clearToken();
    NavHostFragment.findNavController(this)
            .navigate(R.id.action_profile_to_login);
});
```

La acción `action_profile_to_login` en `nav_graph.xml` usa `popUpTo="@id/nav_graph"` con `popUpToInclusive="true"`, lo que destruye todos los fragments del stack y deja el login como única pantalla.

---

## Archivos modificados

- `backend/src/main/resources/application.properties` — variables de entorno para SMTP
- `backend/start-backend.bat` — seteo de `MAIL_USERNAME` y `MAIL_PASSWORD`
- `backend/.../service/AuthService.java` — bloqueo de login sin email verificado + reenvío automático de OTP
- `app/.../MainActivity.java` — chequeo de token al iniciar
- `app/.../ui/auth/LoginFragment.java` — redirección a OTP si el email no está verificado
- `app/.../ui/profile/ProfileFragment.java` — handler del botón cerrar sesión
- `app/.../res/layout/fragment_perfil_usuario.xml` — botón de cerrar sesión en el layout
- `app/.../res/navigation/nav_graph.xml` — acción de navegación profile → login
