# CentroTareas

## Observaciones

### Observación del Paso 1

**¿Qué pasa con la pantalla?**
Al tocar **Tarea larga (MAL)** la app se queda congelada unos 4 segundos: el botón **Tócame** no suma, nada responde a los toques y luego, de golpe, aparece **Terminó**.

**¿Por qué no se ve "Trabajando..."?**
Porque `Thread.sleep(4000)` se ejecuta en el **hilo principal**, que es el mismo que dibuja la pantalla. El mensaje cambia a "Trabajando...", pero el hilo queda dormido y no puede volver a dibujar; cuando despierta, el mensaje ya cambió a "Terminó", así que solo se ve el resultado final.

**¿Qué mostraría Android si la espera fuera de 10 segundos?**
Android mostraría el cuadro **ANR (Application Not Responding)**, "La app no responde", con las opciones de cerrar la app o esperar, porque el hilo principal estuvo bloqueado más de 5 segundos sin atender los toques del usuario.
