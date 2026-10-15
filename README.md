# BolsilloSano

App Android para anotar ingresos y gastos y ver cuánto queda. Los datos se guardan en el teléfono. No hay cuenta, ni login, ni servidor.

Paquete: `org.bolsillosano.project`. Versión `1.0.0`.

La probé en un ABR-LX3 con Android 15.

## Qué hace cada pantalla

La barra de abajo tiene cinco secciones. Cada una guarda su sitio al cambiar de pestaña. Atrás, en una de esas cinco, sale de la app. Crear, editar y el detalle no están en la barra: atrás vuelve a la lista.

**Inicio.** Saludo con el nombre, si lo escribiste. Saldo de todo el historial. Ingresos y gastos del mes elegido (este mes o el anterior). Si el mes pasado también tiene movimientos y su neto no es cero, muestra si subió o bajó respecto a ese periodo. Barras de gasto por categoría y los cinco movimientos más recientes. Desde aquí se añade un gasto o un ingreso.

Si todavía no hay ningún movimiento, Inicio pide el primero. También ofrece cargar datos de ejemplo.

**Movimientos.** Lista agrupada por día (Hoy, Ayer, o la fecha). Búsqueda por descripción. Filtro Todos / Ingresos / Gastos. Rango de fechas: se toca el campo y se elige en el calendario. El botón para quitar el rango solo sale si ya elegiste una fecha. Cada fila abre el detalle.

**Detalle.** Muestra descripción, monto, categoría, fecha y nota. Desde ahí se edita o se elimina. Si el movimiento ya no existe, lo dice y vuelve a la lista. Eliminar pide confirmación.

**Nuevo movimiento y edición.** Tipo ingreso o gasto. Importe obligatorio, mayor que cero. Descripción obligatoria. Categoría del mismo tipo. Fecha. Nota opcional. El teclado del importe es numérico y acepta coma o punto. Mientras guarda, el botón no deja pulsar otra vez. Al terminar, la pantalla anterior muestra "Movimiento guardado" o "Movimiento actualizado".

**Estadísticas.** Mismo selector de mes. Compara ingresos y gastos del periodo, reparte el gasto por categoría y agrupa por semana (la semana empieza en lunes). Si el mes no tiene movimientos, no inventa porcentajes: muestra el vacío.

**Presupuestos.** Un tope por categoría de gasto y por mes. No se pueden crear dos presupuestos de la misma categoría en el mismo mes. El gastado sale de los gastos de esa categoría en ese mes. Los ingresos no cuentan. A partir del 80 % avisa que te acercas. Al 100 % o más avisa que lo superaste. Se edita tocando la tarjeta y se elimina con confirmación. Los movimientos no se borran al quitar el presupuesto.

**Perfil.** Nombre (hay que pulsar Guardar nombre). Moneda: PEN, USD o EUR. Cambiar la moneda solo cambia el símbolo; no convierte lo ya guardado. Tema claro, oscuro o el del sistema. Interruptor para mostrar u ocultar céntimos. Cargar o quitar datos de demostración. Versión de la app.

## Dinero

Un sol son 100 céntimos. En la base el monto es un `Long`, no un `Double`.

Al escribir:

1. Se acepta coma o punto.
2. Se redondea a 2 decimales con `HALF_UP`. `10,555` queda en `10,56`.
3. Se multiplica por 100 y se guarda el entero.
4. Cero, negativo o texto inválido no se guardan.

El saldo de Inicio es la suma de todos los ingresos menos la suma de todos los gastos. No es el saldo de un banco. Los totales del mes, el gráfico y las estadísticas usan solo el periodo elegido.

El formato visible usa español de Perú (`es-PE`) y el código de moneda elegido. Los composables no arman el `S/` a mano.

## Datos

Base Room `bolsillosano.db`, esquema versión 1. Tres tablas:

| Tabla | Qué guarda |
| --- | --- |
| `categories` | 12 categorías fijas. Se insertan la primera vez que se leen, si la tabla está vacía. |
| `transactions` | Tipo, céntimos, descripción, categoría, día, nota y si es ejemplo. |
| `budgets` | Categoría, año, mes y límite. Índice único de categoría + año + mes. |

Categorías de ingreso: Salario, Freelance, Inversiones, Otros ingresos.

Categorías de gasto: Alimentación, Transporte, Vivienda, Salud, Educación, Ocio, Servicios, Otros gastos.

Las preferencias van en DataStore, archivo `bolsillosano_preferences`: nombre, moneda, tema y si se muestran céntimos. Por defecto la moneda es PEN, el tema es el del sistema y los céntimos se ven.

Los datos de ejemplo los arma `DemoTransactions`. Llevan `isDemo = true` y la palabra "ejemplo" en la descripción. Cargar ejemplo borra los ejemplos anteriores y mete otros del mes actual. Quitar ejemplo no toca los movimientos normales.

`android:allowBackup` está en false. La app no pide permisos.

## Código

Todo está en un módulo, `app`. Paquete `org.bolsillosano.project`.

```text
feature/          pantallas y ViewModels
domain/model      Money, Transaction, Budget, preferencias
domain/money      parser, saldo, filtros, presupuestos
domain/repository interfaces
data/local        entidades, DAO y base
data/repository   Room y DataStore
data/demo         movimientos de ejemplo
data/mapper       entidad <-> dominio
core/designsystem tema y componentes
core/common       formato de montos y fechas
core/navigation   rutas
di                Hilt
```

Las pantallas no llaman a Room. El ViewModel expone un `StateFlow` y funciones (`selectPeriod`, `save`, `delete`). Las cuentas de saldo, filtro y presupuesto están en `domain/money`, sin Android, y tienen pruebas en `app/src/test`.

Rutas en `core/navigation/Routes.kt`: `dashboard`, `transactions`, `statistics`, `budgets`, `profile`, alta, detalle, edición y presupuestos.

## Cómo correrla

Hace falta Android Studio, JDK 17 y un teléfono o emulador con Android 8 o superior (`minSdk` 26). `compileSdk` 37, `targetSdk` 36.

En el teléfono: opciones de desarrollador y depuración USB. Acepta el aviso al conectar el cable.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
.\gradlew.bat :app:installDebug
.\gradlew.bat :app:testDebugUnitTest
```

`installDebug` deja la app en el dispositivo. `testDebugUnitTest` corre el parser de montos, el saldo, la variación, los filtros y el cálculo de presupuestos. Hay una prueba de interfaz de `EmptyState` en `androidTest`; esa sí necesita emulador o celular:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Si Gradle dice que no encuentra el SDK, en `local.properties` (no se sube a git) tiene que estar la ruta, por ejemplo:

```properties
sdk.dir=C\:\\Users\\USER\\AppData\\Local\\Android\\Sdk
```

## Versiones

Están en `gradle/libs.versions.toml`. Las que usa el proyecto ahora: AGP 9.1.1, Gradle 9.5.1, Kotlin 2.4.20, KSP 2.3.12, Compose BOM 2026.09.00, Navigation 2.10.2, Hilt 2.60.1, Room 3.0.3, DataStore 1.2.1.

## Qué no hace

No inicia sesión, no cifra la base por su cuenta, no sincroniza y no convierte divisas. Cambiar de PEN a USD no recalcula los céntimos guardados.

## Fotos

Aún no están en el repo. Van en `docs/evidencia/`: inicio, movimientos, estadísticas, presupuestos y perfil.

## Lo que sigue

La misma app en Angular, con Angular Material y SCSS, calculando el saldo con la misma regla. Spring Boot entra cuando las dos pantallas muestren los mismos números.
