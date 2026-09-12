# Mejoras UX en registro y perfil

Hola equipo, les dejo el resumen de los cambios que hice en esta tanda. Todo está en el branch `features/token-vencido`.

---

## Validación de contraseña en tiempo real

Agregué un indicador visual debajo del campo de contraseña en el registro. A medida que el usuario escribe, le muestra qué tan fuerte es su contraseña con texto y color.

**Cómo funciona:**

El usuario empieza a escribir
> si tiene menos de 8 caracteres aparece "Mínimo 8 caracteres" en rojo
> si cumple 1 criterio aparece "Débil" en rojo
> si cumple 2 criterios aparece "Media" en naranja
> si cumple 3 o más aparece "Fuerte" en verde

Los criterios que evalúa son: minúsculas, mayúsculas, números y símbolos. Para poder registrarse necesita al menos nivel "Media", si no le muestra un error.

**Archivos tocados:**

- `fragment_register.xml` - agregué el TextView `tvPasswordStrength` debajo del campo de contraseña
- `RegisterFragment.java` - agregué un TextWatcher que evalúa la fuerza y actualiza el indicador
- `strings.xml` - agregué los strings `password_strength_weak`, `password_strength_medium`, `password_strength_strong` y `password_too_short`

---

## Autocomplete de zona con provincias argentinas

Reemplacé el EditText de zona por un AutoCompleteTextView. Cuando el usuario empieza a escribir le aparece un dropdown con sugerencias que se van filtrando.

**Las opciones que cargué son:**

Las 23 provincias, CABA, y Buenos Aires lo dividí en GBA Norte, GBA Oeste, GBA Sur e Interior para que sea más específico sin llegar a nivel localidad.

**Dónde aplica:**

- En el formulario de registro
- En el diálogo de editar perfil (también lo cambié ahí)

**Archivos tocados:**

- `strings.xml` - agregué el `string-array` con las 27 zonas
- `fragment_register.xml` - cambié el EditText de zona por AutoCompleteTextView con `completionThreshold="1"`
- `dialog_edit_profile.xml` - mismo cambio, de EditText a AutoCompleteTextView
- `RegisterFragment.java` - agregué `setupZonaAutoComplete()` que carga el ArrayAdapter con las zonas
- `ProfileFragment.java` - en `showEditDialog()` ahora configura el AutoCompleteTextView con el mismo adapter de zonas

---

## Formateo automático de teléfono

El campo de teléfono ahora formatea los dígitos automáticamente mientras el usuario escribe.

El usuario escribe `1130509485`
> se muestra como `11 3050 9485`

Es un TextWatcher manual que extrae los dígitos puros, los limita a 10, y los separa en formato argentino (2 + 4 + 4). El número se guarda con los espacios pero eso no afecta nada del backend.

**Archivos tocados:**

- `RegisterFragment.java` - agregué `setupPhoneFormatter()` con el TextWatcher

---

## ScrollView en el registro

Envolví todo el formulario de registro en un ScrollView porque con los campos nuevos (teléfono, zona, indicador de contraseña) el contenido se podía cortar en pantallas chicas.

**Archivos tocados:**

- `fragment_register.xml` - el LinearLayout ahora está dentro de un ScrollView

---

## Resumen de archivos modificados

| Archivo | Qué cambió |
|---------|-----------|
| `strings.xml` | Nuevos strings de contraseña + array de zonas argentinas |
| `fragment_register.xml` | ScrollView, indicador de contraseña, AutoCompleteTextView para zona |
| `dialog_edit_profile.xml` | AutoCompleteTextView para zona |
| `RegisterFragment.java` | TextWatcher contraseña, autocomplete zona, formatter teléfono |
| `ProfileFragment.java` | AutoCompleteTextView en diálogo de edición |

Nada de esto toca la lógica de reputación ni las entidades del backend, solo es UX del lado Android.
