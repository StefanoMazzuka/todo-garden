# TO DO Garden — Guía visual en píxeles

**Versión:** 0.1.  
**Base de producción:** tiles de 32 × 32 px, vista cenital 2D y PNG con transparencia.  
Esta guía define tamaños propios del proyecto; no reproduce las medidas de Stardew Valley. La referencia es su composición con elementos que sobresalen del suelo y se ocultan entre sí.

## 1. Tres medidas distintas

Todas las medidas son **ancho × alto en píxeles del recurso original**, antes del zoom.

- **Celda o huella lógica:** espacio reservado en el terreno. Una celda mide 32 × 32 px.
- **Fotograma:** rectángulo transparente reservado a cada dibujo en la hoja PNG. Su tamaño permanece constante entre estados y animaciones de una misma familia.
- **Silueta visible:** espacio que ocupa el dibujo dentro del fotograma, sin contar la sombra. Puede crecer o encogerse sin cambiar la huella ni el fotograma.

Flores y monstruos usan fotogramas de **64 × 64 px** aunque su huella sea de **32 × 32 px**. El margen transparente permite hojas, pétalos, brazos y bastones que invaden visualmente las celdas vecinas. Sustituye la propuesta preliminar de fotogramas de 32 × 64 px.

El crecimiento se dibuja con sprites distintos. No ampliar una semilla con escalado para convertirla en adulta: todos los recursos conservan la misma densidad de píxel.

## 2. Tiles y elementos del terreno

| Elemento | Fotograma o pieza PNG | Dibujo visible | Huella lógica |
| --- | --- | --- | --- |
| Hierba, tierra, agua | 32 × 32 | 32 × 32, cubre la celda | 32 × 32 |
| Camino y transición de terrenos | 32 × 32 | Hasta 32 × 32 | 32 × 32 |
| Esquina o borde de parcela | 32 × 32 | Hasta 32 × 32 | 32 × 32 |
| Hierba decorativa pequeña | 32 × 32 | 12 × 12 | Sin reserva adicional |
| Grupo de hierba | 32 × 32 | 24 × 20 | Sin reserva adicional |
| Piedra pequeña decorativa | 32 × 32 | 16 × 12 | Sin reserva adicional |
| Roca que bloquea el paso | 64 × 64 | 40 × 28 | 32 × 32 |
| Valla recta, esquina o poste | 32 × 64 | Hasta 32 × 40 | 32 × 32 bloqueados |

Preparar 4 variantes de hierba base y 3 de tierra base. Las variaciones no cambian sus propiedades de paso. Para caminos, dibujar centro, cuatro bordes, cuatro esquinas exteriores y cuatro interiores como piezas de 32 × 32; ampliar el catálogo solo si el mapa lo necesita.

Los bordes aparentes pueden ser curvos o irregulares dentro de estas piezas. Los caminos principales tendrán 64 px de ancho (dos celdas); los secundarios, 32 px. Evitar llenar todo el jardín con filas idénticas de parcelas: alternar grupos de plantas, claros y caminos.

## 3. Flores y plantas permanentes

**Fotograma común: 64 × 64 px. Huella: una celda de 32 × 32 px. Pivote: (32, 56).**

El pivote marca la raíz en contacto con el suelo. Las coordenadas del PNG comienzan arriba a la izquierda; X crece a la derecha e Y hacia abajo. El dibujo se centra alrededor de X = 32 y crece hacia arriba desde Y = 56.

| Estado visual | Silueta de referencia | Caja dentro del fotograma (x, y, ancho, alto) | Sombra separada |
| --- | --- | --- | --- |
| Semilla plantada | 10 × 8 | (27, 48, 10, 8) | 10 × 4 |
| Brote pequeño | 12 × 16 | (26, 40, 12, 16) | 12 × 4 |
| Planta joven | 20 × 28 | (22, 28, 20, 28) | 16 × 6 |
| Planta crecida con capullo | 28 × 40 | (18, 16, 28, 40) | 20 × 8 |
| Flor común abierta | 36 × 44 | (14, 12, 36, 44) | 24 × 8 |
| Flor rara abierta | 48 × 52 | (8, 4, 48, 52) | 28 × 10 |
| Planta sencilla, recompensa final sin flor | 28 × 36 | (18, 20, 28, 36) | 20 × 8 |

Las cajas son objetivos de tamaño para la pose de reposo, no rectángulos que deban rellenarse por completo. Las distintas especies cambian silueta, color y pétalos dentro de ese presupuesto.

En animación, reservar una caja máxima de **56 × 56 px**, desde (4, 0) hasta (60, 56), para flores y plantas. Permite una oscilación de 1–2 px sin recortar los pétalos. La raíz permanece fija y no cambia la huella.

### Relación entre estados visuales y tareas

Las etapas de esta tabla son recursos artísticos. No añaden otro ciclo obligatorio de cuidados ni otro contador de crecimiento a las tareas completadas.

Al completar, una breve animación de plantado puede recorrer semilla, brote, joven y capullo hasta llegar a la recompensa correspondiente. Propuesta: 4 poses intermedias de 150 ms, seguidas de la pose final; duración de transición de 600 ms. La planta sencilla termina sin flor. El resultado final ya se guarda en la transacción de completado; cerrar la app durante el efecto no lo pierde y al volver se muestra terminado.

Las plantas completadas permanecen en su estado final. Una flor rara es un tipo de recompensa, no la siguiente edad de una flor común. Un crecimiento posterior durante horas o días sería una mecánica futura y necesitaría una decisión independiente.

## 4. Monstruos-planta por edad

**Fotograma común a todos los estados y direcciones: 64 × 64 px. Huella lógica: 32 × 32 px. Pivote: (32, 56), entre los pies.**

| Estado de tarea activa | Silueta de reposo | Caja dentro del fotograma (x, y, ancho, alto) | Sombra separada | Lectura visual |
| --- | --- | --- | --- | --- |
| Semilla | 12 × 12 | (26, 44, 12, 12) | 10 × 4 | Semilla con ojos y una pequeña raíz |
| Joven | 24 × 28 | (20, 28, 24, 28) | 16 × 6 | Cabeza clara, dos hojas y patas cortas |
| Adulta | 40 × 48 | (12, 8, 40, 48) | 24 × 8 | Follaje desarrollado y brazos que sobresalen |
| Anciana | 44 × 42 | (10, 14, 44, 42) | 26 × 8 | Cuerpo encorvado, hojas caídas y bastón incluido en el ancho |

La anciana es más baja por la postura, no una adulta escalada. Su bastón permanece dentro de la silueta y no reserva otra celda.

Caja máxima durante animaciones: **48 × 52 px**, desde (8, 4) hasta (56, 56). Todas las direcciones usan el mismo pivote y los mismos límites. Los pies pueden alternarse alrededor del pivote, pero la posición de suelo de la entidad no salta entre fotogramas. El rebote del cuerpo ocurre dentro de la caja; la sombra permanece sobre el terreno.

El paso entre centros de celdas mide **32 px**. Cada tramo cambia solo X o solo Y: arriba, abajo, izquierda o derecha, sin diagonales. La posición se interpola continuamente durante ese tramo.

La semilla permanece en reposo con una animación pequeña. Joven, adulta y anciana utilizan el mismo paseo, con estos valores iniciales:

| Etapa | Velocidad en píxeles base/segundo | Duración de un tramo de 32 px | Pausa al acabar un tramo |
| --- | --- | --- | --- |
| Semilla | 0 | No camina | Reposo continuo |
| Joven | 48 | Aproximadamente 0,67 s | 0,3–1 s |
| Adulta | 32 | 1 s | 0,8–2 s |
| Anciana | 16 | 2 s | 2–4 s |

Estos valores controlan el paseo visual, no la duración de la tarea. El estado por edad sigue calculándose con las fechas de la arquitectura.

## 5. Hojas PNG y animaciones

No confundir el tamaño de un fotograma con el tamaño del archivo que reúne muchos fotogramas. PNG RGBA, fondo transparente, sin suavizado al exportar y sin márgenes ni separación entre fotogramas en las hojas fuente.

### Monstruos

Un archivo por especie y etapa: `sprout_seed.png`, `sprout_young.png`, `sprout_adult.png` y `sprout_old.png`.

Cada archivo mide **256 × 256 px**: cuatro columnas y cuatro filas de fotogramas de 64 × 64.

| Fila, de arriba abajo | Dirección |
| --- | --- |
| 0 | Abajo |
| 1 | Izquierda |
| 2 | Derecha |
| 3 | Arriba |

Las columnas 0–3 contienen cuatro poses de caminar. Reposo usa la columna 0, con opción de alternar 0 y 2 si la especie tiene respiración dibujada. En la semilla, las cuatro columnas son poses de reposo; repetirlas en las cuatro filas evita casos especiales de tamaño. El bastón debe dibujarse de forma coherente en cada dirección; no depender de reflejar automáticamente una imagen asimétrica.

Duraciones iniciales por fotograma de caminar: joven 120 ms, adulta 160 ms, anciana 240 ms. Reposo de semilla: 300 ms por pose. Se configuran en el catálogo y se pueden ajustar sin cambiar el código del motor.

### Flores y plantas

Un archivo por especie de planta o flor de **256 × 448 px**: cuatro columnas y siete filas de fotogramas de 64 × 64. Orden de filas: semilla, brote, joven, capullo, flor común, flor rara, planta sencilla; corresponde a la tabla anterior.

Las cuatro columnas son poses de oscilación, a 250 ms por fotograma. Mientras no haya animación dibujada, repetir la misma pose en las cuatro columnas. Una especie que no utilice una fila puede repetir una pose válida; el catálogo solo selecciona las filas que correspondan a su recompensa.

Los nombres y el orden son la convención de producción inicial. El catálogo guarda explícitamente archivo, tamaño, pivote, filas/fotogramas, tiempos, caja de selección y huella. Si se reorganiza una hoja, se actualiza el catálogo conservando el ID de especie o decoración.

## 6. Lápidas, árboles, arbustos y edificios

| Elemento | Fotograma | Silueta de referencia | Huella bloqueada o reservada | Pivote PNG |
| --- | --- | --- | --- | --- |
| Lápida | 64 × 64 | 28 × 36 | 32 × 32, parcela | (32, 56) |
| Lápida con musgo | 64 × 64 | 36 × 40 | 32 × 32, parcela | (32, 56) |
| Arbusto pequeño | 64 × 64 | 40 × 32 | 32 × 32 | (32, 56) |
| Arbusto grande | 96 × 96 | 72 × 56 | 64 × 32 | (48, 80) |
| Árbol joven decorativo | 96 × 128 | 64 × 96 | 32 × 32 | (48, 112) |
| Árbol adulto | 128 × 160 | 112 × 144 | 64 × 32 | (64, 152) |
| Árbol seco | 128 × 160 | 88 × 128 | 64 × 32 | (64, 152) |
| Edificio pequeño de carpeta | 128 × 160 | 112 × 136 | 96 × 64 | (64, 144) |
| Torre de carpeta | 128 × 192 | 96 × 168 | 64 × 64 | (64, 176) |

Las siluetas se centran horizontalmente en el pivote y terminan verticalmente en él. El árbol seco comparte la huella y el pivote del adulto para poder sustituir su arte sin moverlo. Los tamaños grandes quedan definidos para contenido posterior; el primer prototipo no necesita producirlos todos.

Flores, monstruos y lápidas anclan su pivote al centro de su celda. Para objetos estáticos de varias celdas, el anclaje se sitúa en el centro del borde delantero de su huella (el borde inferior en pantalla). Tiled registra por separado el rectángulo de celdas bloqueadas y el anclaje de dibujo; el tamaño de la copa o el tejado no determina las colisiones.

Edificios y árboles pueden almacenarse como PNG individuales o como conjuntos de tiles de 32 × 32. Si se usan varios tiles, se agrupan como un solo objeto para ordenar su profundidad. Los anclajes de carpeta conservan sus IDs al sustituir el arte.

## 7. Solapamiento y profundidad

Para una flor de 48 px de ancho situada en el centro de una celda de 32 px, el dibujo sobresale **8 px por cada lado**. Para un monstruo adulto de 40 px, sobresale **4 px por cada lado**. No se amplía su huella por ello.

Con el pivote en el centro de una celda, hay 16 px de terreno hasta su borde trasero. Una flor de 52 px de alto sobresale visualmente **36 px más allá de ese borde**: puede cubrir parte de las celdas que están detrás. Esa altura es dibujo, no una coordenada Z.

Ejemplo en píxeles de mundo, con Y creciendo hacia arriba:

```text
Flor rara en celda (5, 5): raíz en (176, 176).
Su dibujo de reposo ocupa X = 152..200 e Y = 176..228.
Monstruo detrás: pies en (176, 208) → dibujar antes de la flor.
Monstruo delante: pies en (176, 144) → dibujar después de la flor.
```

Orden común: suelo, detalles planos y sombras; después flores, lápidas, monstruos, arbustos, árboles y edificios ordenados por Y del anclaje **de mayor a menor**. Desempatar por ID estable para evitar parpadeos. Ordenar por los pies o la raíz, nunca por el borde superior del PNG.

Los objetos altos que deben permitir pasar por delante y por detrás participan en esta lista común. No colocar todo árbol en una capa que siempre tape a los monstruos. Reservar `decoration_front` para elementos que realmente deban estar siempre por delante; si un edificio necesita oclusión más elaborada, separar base y cubierta de forma explícita en su definición.

Para una posición de suelo `(worldX, worldY)` en píxeles base, un PNG de altura `H` y pivote `(px, py)` medido desde arriba a la izquierda, su esquina inferior izquierda al dibujar será:

```text
drawX = worldX - px
drawY = worldY - (H - py)
```

Con fotograma de 64 × 64 y pivote (32, 56), se dibuja en `(worldX - 32, worldY - 8)`. En el motor, que mide en celdas, dividir estas medidas entre 32. Aplicar la conversión de coordenadas del PNG una sola vez en el cargador/renderizador.

Para romper alineaciones rígidas, las decoraciones de una celda pueden tener un desplazamiento visual estable de **−3 a +3 px en X** y **−2 a +2 px en Y**, derivado del ID. Aplicarlo por igual a dibujo, sombra, selección y clave de profundidad; no altera la celda reservada. No aplicarlo al suelo ni a los destinos del paseo de los monstruos.

## 8. Sombras, selección y escalado

Las sombras son elipses suaves de pixel art en una capa separada, con las medidas de las tablas, centradas en el anclaje de suelo. Usar una opacidad inicial del 25 %. No hornearlas en cada fotograma: así no saltan con el follaje ni cambian al sustituir la hoja PNG.

La selección usa la caja visible de cada estado, no el fotograma transparente completo. Puede ampliarse hasta un mínimo de 24 × 24 px base para semillas y brotes, centrado en su dibujo. Resolver candidatos de delante hacia atrás; las listas de tareas siguen proporcionando acceso a criaturas ocultas o muy pequeñas por el zoom. La zona de selección no modifica las colisiones.

El zoom no cambia los archivos fuente: un tile de 32 × 32 se muestra a 64 × 64 píxeles físicos con escala 2× y a 96 × 96 con escala 3×. Son ejemplos de escalado, no tamaños Android en dp. Usar nearest-neighbor; los factores enteros producen píxeles uniformes. Durante una pinza puede haber escala fraccionaria, con píxeles de distinto ancho aparente.

## 9. Entrega mínima de arte y comprobación

Primer conjunto: un tileset de terreno, una especie con sus cuatro hojas de etapa, una hoja de planta/flor y una lápida. Probarlos juntos en un mapa pequeño antes de dibujar edificios y más especies.

Comprobar en el visor o APK: tamaños divisibles por el fotograma, transparencia limpia, raíces y pies alineados, animaciones dentro de sus márgenes, cuatro direcciones sin diagonales, selección sin capturar el margen transparente y paso por delante/detrás de una flor y un árbol de prueba. El prototipo debe mostrar parcelas contiguas para verificar que el solapamiento no oculta sistemáticamente las flores pequeñas.

Los tamaños quedan definidos como contrato de producción. Cambiar un dibujo manteniendo fotograma y pivote solo exige sustituir el PNG; cambiar dimensiones o distribución exige actualizar el catálogo. Los cambios de huella del mapa requieren además revisar ocupación y migración de posiciones.
