# PROMPTS.md — Fase 2 (rama `mejora-ia`)

Registro de los prompts importantes usados para mejorar **CentroTareas** sin romper la arquitectura MVVM + Repository.

> **Nota:** el documento sugiere Gemini en Android Studio; en mi caso usé **Claude** como asistente de IA. Cada propuesta la leí, la probé en el emulador y la corregí cuando rompía alguna regla del laboratorio.

| # | Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|---|
| 1 | «Agrega la mejora *Primos con progreso*: mostrar el porcentaje del cálculo mientras avanza.» | Agregar un parámetro `alAvanzar: (Float) -> Unit` a `contarPrimos` en el Repository, publicar el avance en un `StateFlow<Float>` del ViewModel y mostrar una barra con el porcentaje. | Acepté que el Repository avise **cada 1 %** y no en cada número. Mantuve `ensureActive()` dentro del ciclo. | Avisar en cada número (1 500 000 veces) saturaba la pantalla de actualizaciones. Sin `ensureActive()` el botón Cancelar dejaría de funcionar. |
| 2 | «¿Puedes aplicar otros colores aparte del azul?» y luego «mejora el color de la línea de carga, tipo trueno, también la de tarea de progreso». | Paleta propia (azul fuerte + naranja + turquesa) y `dynamicColor = false` en `Theme.kt`; barra dibujada con `Canvas`, degradado amarillo-naranja y un destello animado (`BarraRayo`). | Dejé la barra superior y los títulos en **azul**, como pide el diseño. Los colores los hice en la rama `mejora-ia` y no en `main`. Lo incluí en el mismo commit de la mejora 1 para no agregar un commit extra. | `theme/` era [E] en la Fase 1 y no debía tocarse ahí. La barra trueno es solo dibujo (no lanza corrutinas ni calcula), así que no rompe MVVM. |
| 3 | «Agrega la mejora *Tiempo máximo*: usar `withTimeout` en las cargas y si tardan más de 2 s mostrar *Tardó demasiado*.» | Envolver las cargas en `withTimeout(2000)` y capturar `TimeoutCancellationException` para mostrar el mensaje. | Lo puse detrás de un **interruptor** «Tiempo máximo 2 s». En el `catch` reviso `TimeoutCancellationException` **antes** de relanzar la `CancellationException`. | Las cargas tardan 3 s y 5 s: con el límite siempre activo, ninguna carga funcionaría nunca. `TimeoutCancellationException` es una `CancellationException`; si se relanzara primero, el usuario no vería el mensaje. |
| 4 | «Agrega la mejora *Reintento automático*: reintentar hasta 3 veces con una espera creciente antes de mostrar el error.» | Función `conReintento { }` en el ViewModel con un ciclo de 3 intentos y `delay` creciente (500 ms, 1000 ms); texto «Reintentando (2 de 3)...» en la pantalla. | Me aseguré de que **la cancelación nunca se reintente** (incluido el tiempo máximo): se relanza siempre. Solo se reintenta la carga de pedidos, que es la que falla. | La regla de la Fase 2 obliga a relanzar siempre la `CancellationException`. Reintentar una cancelación haría que el usuario no pudiera detener la carga. |
| 5 | «Agrega la mejora *Comparación de tiempos*: una tabla que acumule los tiempos de Una por una y A la vez.» | Clase `ComparacionTiempos` con las dos listas y sus promedios; el ViewModel agrega el tiempo cuando una carga termina bien; tabla en la pantalla con botón «Limpiar tabla». | Solo se guardan las cargas **exitosas**; los errores y «Tardó demasiado» no cuentan. El promedio se calcula en el modelo, no en el Composable. | Mezclar tiempos de cargas fallidas haría la comparación injusta. Calcular en la pantalla rompería la regla de que la View solo muestra el estado. |

## Mejoras y commits

| Mejora | Capas que cambian | Commit |
|---|---|---|
| Primos con progreso + paleta de colores y barra tipo trueno | Repository + ViewModel + View + theme | `feat(primos): muestra el progreso del calculo` |
| Tiempo máximo | ViewModel + View | `feat(carga): agrega tiempo maximo con withTimeout` |
| Reintento automático | ViewModel + View | `feat(carga): reintenta automaticamente hasta 3 veces con espera creciente` |
| Comparación de tiempos | Model + ViewModel + View | `feat(carga): agrega tabla para comparar tiempos de una por una y a la vez` |

## Reglas que revisé en todo el código de la Fase 2

- Sin `GlobalScope`, `runBlocking` ni `Thread.sleep`.
- La `CancellationException` siempre se relanza.
- El ViewModel no recibe `Context` ni conoce Composables; la View no lanza corrutinas ni calcula.
