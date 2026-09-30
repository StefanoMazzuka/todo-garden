# TO DO Garden — Arquitectura

**Versión:** 0.4 — arquitectura orientada a tiles.  
**Objetivo:** aplicación Android local, con código pequeño y contenido visual editable.  
**Desarrollo:** Linux, editor a elección, JDK, Android SDK de consola y Gradle Wrapper. Android Studio es opcional.

Las medidas de producción, pivotes, hojas PNG y estados visuales se definen en la [Guía visual en píxeles](TO_DO_Garden_Guia_Visual.md).

## 1. Decisión principal

Mantener Kotlin, Jetpack Compose, libGDX y Room. Compose resuelve listas, formularios y ajustes; libGDX dibuja el jardín. El jardín utiliza una cuadrícula ortogonal y recursos pixel art construidos con tiles.

Las criaturas usan fotogramas de una hoja de tiles, pero son objetos móviles independientes del mapa. Esto permite animarlas y desplazarlas suavemente entre celdas sin modificar el mapa en cada fotograma. Una criatura puede dibujarse con varios tiles y ocupar una sola celda lógica.

No incorporar inicialmente física, ECS, generación procedural, un motor de scripts ni una clase diferente por especie. Un único comportamiento de criatura interpreta las definiciones de cada especie.

## 2. Estructura mínima

Dos módulos Gradle al inicio:

```text
:app                         Android
  ui/                        Compose, navegación y ViewModels
  data/                      Room, DataStore y repositorio
  integration/               alojamiento de libGDX y comunicación con UI
  background/                revisión de vencimientos y notificaciones

:garden                      Kotlin/JVM + libGDX, sin dependencias Android
  model/                     modelos sencillos y reglas temporales puras
  content/                   definiciones de mapas, especies y animaciones
  render/                    mapa, sprites, cámara y selección
  movement/                  paseo por celdas transitables
```

Dentro de `:garden`, `model/` no importa libGDX: las reglas se pueden probar sin gráficos. No crear un módulo por capa ni un caso de uso por cada operación CRUD. Extraer módulos adicionales únicamente cuando exista una necesidad real.

La separación facilita reutilizar conceptos y reglas, pero no garantiza una versión web automática: Compose Android, Room y la compatibilidad del destino web requerirán decisiones posteriores.

## 3. Flujo de datos

```text
Compose → ViewModel → GardenRepository → Room
                          │
                     reglas puras
                          │
Room → Flow → estado inmutable del jardín → libGDX
                                             │
                          evento de selección de un ID
                                             ↓
                                     ViewModel → Compose
```

Room es la fuente de verdad para tareas y resultados permanentes. El renderer mantiene solo movimiento, animación, cámara y selección temporales. No consulta la base de datos durante el dibujo ni decide si una tarea ha sido completada.

El repositorio ofrece operaciones concretas: crear, editar, completar, cancelar, reconciliar vencimientos y colocar un resultado pendiente. No necesita una jerarquía de repositorios genéricos.

### Integración Android

La pantalla de jardín aloja una `AndroidFragmentApplication` de libGDX en un contenedor de fragments. Compose muestra la navegación y los formularios fuera de la superficie del jardín. Evitar inicialmente superponer formularios sobre OpenGL y repartir un mismo gesto entre ambos sistemas.

Enviar los nuevos estados al hilo de libGDX mediante `Gdx.app.postRunnable`; devolver eventos a la UI en el hilo principal. Los estados son inmutables y no comparten listas mutables entre hilos. Cargar recursos una vez, pausar al pasar a segundo plano y liberarlos al destruir el motor.

Validar esta integración en una primera APK antes de implementar el resto del producto.

## 4. Contrato del mapa

Decisiones iniciales ajustables tras el prototipo artístico:

- Vista cenital ortogonal; sin isometría.
- Tile base de 32 × 32 píxeles; el mundo mide en celdas, no en píxeles de pantalla.
- Mapa finito de 24 × 24 celdas para el primer jardín (768 × 768 píxeles base), más compacto para móvil.
- Mapas `.tmx`, tilesets `.tsx` e imágenes `.png`, editados en Tiled.
- Coordenadas lógicas con origen abajo a la izquierda. Convertir las coordenadas de objetos de Tiled al cargar; no repartir conversiones por el código.
- Filtrado de textura nearest-neighbor. Cámara limitada al mapa y rango acotado de zoom.

Capas y convenciones:

| Capa | Tipo | Función |
| --- | --- | --- |
| `ground` | Tiles | Suelo y caminos |
| `decoration_back` | Tiles | Decoración dibujada bajo las criaturas |
| `blocked` | Tiles, invisible | Una celda ocupada impide el paseo |
| `plantable` | Tiles, invisible | Celdas destinadas a resultados permanentes |
| `decoration_front` | Tiles | Copas y detalles dibujados sobre las criaturas |
| `anchors` | Objetos | Aparición y accesos a carpetas, con ID estable |
| `props` | Objetos | Árboles, arbustos y edificios que se ordenan junto a las criaturas; ID, recurso, pivote y huella |

Las celdas `plantable` quedan fuera del paseo para que una nueva planta no encierre criaturas. El primer mapa tendrá caminos conectados y parcelas alrededor. Las plantas y lápidas no se escriben en el TMX: se cargan de Room y se dibujan sobre el mapa base.

Los edificios que representan carpetas son anclajes estáticos con un `anchorId`. La asociación carpeta–anclaje se guarda en Room. Una carpeta sin anclaje sigue siendo accesible desde la lista; no limitar el número de carpetas al número de edificios.

Guardar `mapId` y `mapVersion`. No cambiar el significado de una celda o eliminar anclajes en una actualización sin migrar las posiciones y asociaciones guardadas.

## 5. Criaturas y catálogo visual

El contenido se define en archivos JSON y hojas PNG, incluidos en la APK:

```text
assets/
  maps/garden_01.tmx
  tilesets/garden.tsx
  textures/garden.png
  creatures/sprout.png
  catalog/species.json
  catalog/decorations.json
```

Cada especie define un ID estable, hoja de tiles, tamaño de fotograma, punto de apoyo en los pies y animaciones por etapa y acción. Cada animación indica los índices de los fotogramas, su duración y si se repite. La etapa configura también velocidad y tiempo de descanso.

Contrato inicial: `sprout`, fotogramas de 64 × 64 px, pivote (32, 56) medido desde arriba a la izquierda y huella lógica de 1 × 1 celda (32 × 32 px), animaciones `seed.idle`, `young.walk`, `adult.walk` y `old.walk`. Los detalles como el bastón pertenecen al dibujo. Una animación ausente utiliza un fotograma de reposo definido como alternativa.

Los anclajes de carpeta pueden referenciar el ID de un objeto de `props`; no duplican su dibujo.

Las recompensas apuntan a IDs de decoración del catálogo. Las etapas de semilla, brote, planta joven y capullo de una flor permanente son poses de su transición de plantado (600 ms); después se mantiene el resultado final. No añaden un segundo temporizador de crecimiento ni estados persistidos. Guardar IDs semánticos, nunca índices de tiles o posiciones de un atlas en la base de datos: el artista puede reorganizar las hojas sin romper partidas.

Validar los recursos al compilar o en una comprobación de contenido: IDs únicos, referencias existentes, fotogramas dentro de la imagen, capas obligatorias y posiciones transitables válidas. No construir un editor propio.

## 6. Movimiento y dibujo

Una criatura joven, adulta o anciana alterna entre reposar y caminar. La semilla permanece en reposo animado. Al terminar una pausa elige una celda vecina transitable en cuatro direcciones (solo X o Y, sin diagonales) e interpola su posición hasta ella. Si no tiene salida, permanece en reposo. Inicialmente las criaturas pueden compartir celda: evita atascos y reservas de rutas.

No necesita A* para pasear sin destino. La edad modifica velocidad y pausas; el mismo algoritmo sirve para todas las especies. Solo se mueve mientras el jardín está visible; al reabrir no se simulan los pasos perdidos.

Orden de dibujo: suelo, decoración plana y sombras; después plantas, criaturas y objetos altos del mapa (árboles y edificios) en una lista común ordenada por Y del anclaje de mayor a menor, dado el origen inferior izquierdo; por último decoración siempre frontal y efectos. Los objetos altos se declaran en una capa de objetos `props`, con recurso, pivote y huella; no se dibujan también como tiles frontales. Los empates se resuelven por ID estable. La guía visual concreta los solapamientos y la conversión de píxeles a celdas. Usar `OrthogonalTiledMapRenderer`, `SpriteBatch`, `OrthographicCamera` y un `Viewport`. Scene2D no es necesario para este alcance.

Convertir pulsaciones de pantalla a coordenadas del mundo con la cámara. Seleccionar por la huella o zona táctil del objeto, recorriendo los objetos visibles en orden inverso al dibujo. Distinguir toque, arrastre y pinza para no abrir una tarea al mover la cámara.

## 7. Modelo persistente

| Entidad | Datos esenciales |
| --- | --- |
| `Task` | ID, título, descripción, carpeta, creación, vencimiento, estado, finalización, especie |
| `Folder` | ID, nombre y anclaje opcional |
| `GardenItem` | ID, `sourceTaskId` único, tipo, decoración, creación, mapa y celda opcionales |
| `GardenState` | Mapa activo y versión |

Estados persistidos de tarea: `ACTIVE`, `COMPLETED`, `EXPIRED`, `CANCELLED`. Semilla, joven, adulta y anciana son etapas calculadas, no estados guardados. No crear tablas de criaturas, flores y lápidas que dupliquen la misma información.

Una tarea activa produce una criatura visual. Completarla o expirar produce un `GardenItem`. La posición de paseo y el fotograma actual no se guardan; al abrir se asigna una celda transitable de forma estable a partir del ID. Las posiciones de resultados permanentes sí se guardan.

Room guarda fechas como instantes UTC; la UI utiliza la zona horaria del dispositivo. DataStore guarda preferencias. El funcionamiento local toma la hora del dispositivo y no pretende impedir su manipulación.

## 8. Reglas temporales y consistencia

Propuesta inicial de balance, configurable en un único lugar:

```text
progreso = clamp((ahora - creación) / (vencimiento - creación), 0, 1)
semilla: [0, 0.10)
joven:   [0.10, 0.40)
adulta:  [0.40, 0.80)
anciana: [0.80, 1)
expirada: ahora >= vencimiento
```

Exigir vencimiento posterior a creación y al instante de edición. La recompensa al completar usa el mismo progreso: flor rara antes de 0.40, flor común antes de 0.80 y planta sencilla antes de 1. Son valores de partida para probar el diseño, no requisitos artísticos definitivos.

Editar el vencimiento de una tarea activa conserva la creación y recalcula su etapa; puede rejuvenecer. Las tareas resueltas no se reabren en el MVP. Cancelar una tarea activa la oculta sin recompensa ni lápida; cancelar después del límite primero la resuelve como expirada. No implementar borrado irreversible en el MVP.

Completar y reconciliar vencimientos se ejecutan mediante transacciones: comprobar de nuevo el estado y la hora, resolver la tarea e insertar su resultado. La unicidad de `sourceTaskId` impide recompensas o lápidas duplicadas cuando coinciden una acción del usuario y un worker. En el instante exacto del límite la tarea expira.

Reconciliar al abrir o volver a la app, antes de modificar una tarea y al llegar al siguiente vencimiento mientras está visible. WorkManager añade una revisión oportunista en segundo plano; no garantiza la hora exacta ni sostiene una simulación. Los recordatorios exactos quedan fuera del MVP.

## 9. Capacidad y ambiente

Asignar a cada resultado la primera celda `plantable` libre en un orden estable, dentro de la misma transacción. Si no quedan celdas, guardar el resultado sin posición y mostrarlo en una colección de pendientes. Nunca eliminar resultados ni impedir completar tareas porque el mapa esté lleno. Permitir guardar una decoración colocada en la colección y colocar otra en la celda liberada.

El ambiente se deriva de las últimas 20 tareas resueltas, ordenadas por fecha de resolución e ID: proporción completada frente al total completado y expirado. Las canceladas se excluyen. Sin resultados, usar ambiente neutral. El estado visual interpola gradualmente hacia ese valor cuando el jardín está visible; no necesita persistir otro contador.

Para el MVP, representar el ambiente con variaciones de color y unos pocos elementos de un catálogo. Niebla, fantasmas, mariposas y otros efectos elaborados llegan después de validar la lectura visual y el rendimiento.

## 10. Orden de implementación

1. APK compilada por terminal: Compose abre un jardín libGDX, carga un TMX y permite desplazar y ampliar la cámara. Validar pausa, reanudación y volver desde un formulario.
2. Una especie de prueba con etapas y paseo por celdas; selección que abre su ficha. Probar varias decenas de criaturas en un móvil antes de ampliar el arte.
3. Lista y formulario de tareas con Room; crecimiento por fechas, completar y expirar sin duplicar resultados.
4. Parcelas, plantas, lápidas y colección cuando el mapa está lleno.
5. Carpetas, ambiente básico y revisión de vencimientos en segundo plano.
6. Más especies, animaciones y acabado artístico.

Pruebas prioritarias: límites de tiempo, edición de fecha, resolución concurrente, idempotencia, ocupación y mapa lleno. Verificación en dispositivo: ciclo de vida, gestos, orden visual y fluidez. Evitar tests de cada getter o de detalles de dibujo.

## 11. Referencias técnicas

- [Mapas de tiles en libGDX](https://libgdx.com/wiki/graphics/2d/tile-maps): carga de Tiled y renderizado ortogonal.
- [Integración de libGDX en Android](https://libgdx.com/wiki/app/starter-classes-and-configuration): alojamiento y ciclo de vida.
- [WorkManager: peticiones de trabajo](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work): ejecución diferida; el trabajo periódico tiene un mínimo de 15 minutos y no es exacto.
- [Compilar Android desde la consola](https://developer.android.com/build/building-cmdline): Gradle y APK sin depender del IDE.
