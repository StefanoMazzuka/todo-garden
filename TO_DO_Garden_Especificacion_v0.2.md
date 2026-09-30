# TO DO Garden — Especificación del Proyecto

**Versión:** 0.4  
**Plataforma inicial:** Android  
**Estado:** Diseño inicial revisado para tiles

El nombre del archivo se conserva para mantener los enlaces existentes. Las decisiones técnicas y los valores iniciales de balance se detallan en [Arquitectura](TO_DO_Garden_Arquitectura.md).

## 1. Concepto

**TO DO Garden** es una aplicación de gestión de tareas representada como un jardín interactivo en 2D. El estado del jardín refleja los hábitos de productividad del usuario.

Cada tarea se convierte en un monstruo-planta con un ciclo de vida limitado por su fecha de finalización. Las tareas completadas generan plantas y flores permanentes; las tareas vencidas generan lápidas y deterioran visualmente el jardín.

La primera versión será una aplicación Android completamente local, sin servidor ni cuenta de usuario. La arquitectura se preparará para poder incorporar sincronización y una versión web en el futuro.

## 2. Objetivo

Transformar una lista de tareas tradicional en una experiencia visual y emocional, donde cada tarea tenga una evolución visible y deje una huella permanente en el jardín.

## 3. Mecánica principal

Al crear una tarea, el usuario define:

- Título.
- Descripción opcional.
- Carpeta o categoría.
- Fecha y hora límite.

La tarea nace como una semilla y evoluciona hasta convertirse en un monstruo-planta.

### 3.1 Ciclo de vida

- **Semilla:** tarea recién creada, en reposo animado.
- **Joven:** criatura rápida y activa.
- **Adulta:** movimiento normal.
- **Anciana:** movimiento lento, pausas frecuentes y animación con bastón.
- **Completada:** se transforma en una planta permanente.
- **Expirada:** muere y genera una lápida.

Las flores permanentes tendrán poses de semilla, brote, planta joven, capullo y flor abierta para una transición visual breve al plantarlas. Después conservan su forma final; estas poses no introducen un nuevo ciclo de cuidados o vencimientos. La planta sencilla termina sin flor.

### 3.2 Resultado de la tarea

Si la tarea se completa antes del límite, la criatura se planta definitivamente. La recompensa depende de la rapidez:

- Muy pronto: flor rara.
- En un tiempo medio: flor común.
- Cerca del límite: planta sencilla.

Si la tarea no se completa a tiempo:

- La criatura muere.
- Se crea una lápida permanente, colocada en una parcela libre o guardada en la colección si no hay espacio.
- Aumenta el deterioro ambiental del jardín.

## 4. Jardín interactivo

El jardín será un escenario 2D explorable sobre una cuadrícula ortogonal. Los mapas se diseñarán en Tiled con capas de suelo, decoración, obstáculos, parcelas y anclajes de edificios.

Las criaturas serán sprites animados a partir de hojas de tiles. Tendrán una celda lógica y podrán ocupar varios tiles visualmente. Pasearán entre celdas vecinas transitables con movimiento suave solo en X o Y, sin diagonales; en la primera versión podrán cruzarse entre sí. No habrá física ni búsqueda de caminos hacia destinos.

Las plantas y lápidas se colocarán en parcelas, separadas de los caminos. Su posición definitiva no dependerá de dónde estuviera paseando la criatura. Si el jardín se llena, los nuevos resultados quedarán en una colección: completar tareas seguirá funcionando. El usuario podrá guardar una decoración colocada en la colección y colocar otra en el hueco libre.

El usuario podrá:

- Desplazarse por el escenario arrastrando la cámara.
- Acercar y alejar mediante gestos de zoom.
- Pulsar criaturas, plantas, lápidas y edificios.
- Observar varias criaturas moviéndose al mismo tiempo.
- Acceder a carpetas mediante objetos físicos del jardín.

Los monstruos-planta caminarán de manera autónoma por las zonas permitidas. Su velocidad, animaciones y frecuencia de descanso dependerán de su edad.

## 5. Estado ambiental

El jardín cambia según la proporción de tareas completadas y expiradas.

### Jardín saludable

- Vegetación abundante.
- Flores especiales.
- Mariposas.
- Arcoíris.
- Ambiente mágico.

### Jardín deteriorado

- Lápidas.
- Niebla.
- Fantasmas.
- Árboles secos.
- Ambiente sombrío.

Los cambios ambientales serán progresivos y representarán una tendencia, no el resultado aislado de una sola tarea. Inicialmente se calcularán a partir de las últimas 20 tareas completadas o expiradas; las canceladas no cuentan. Sin historial, el ambiente será neutral.

La primera versión representará el ambiente con color y decoración básica. Los efectos elaborados descritos arriba son objetivos posteriores de acabado artístico.

## 6. Organización por carpetas

Las tareas podrán agruparse en carpetas o categorías.

Cada carpeta estará representada por un objeto físico dentro del jardín, por ejemplo:

- Casa.
- Biblioteca.
- Taller.
- Laboratorio.
- Torre.

Al pulsar el objeto, el usuario accederá a las tareas asociadas a esa carpeta. Los edificios serán anclajes del mapa con asociaciones configurables. Las carpetas sin edificio seguirán disponibles desde la lista.

## 7. Dirección artística

- Gráficos 2D sobre tiles de 32 × 32 píxeles.
- Flores y monstruos en fotogramas de 64 × 64 px con pivote (32, 56); huella de una celda y siluetas variables por etapa.
- Tamaños de cada etapa, árboles, edificios, hojas PNG y reglas de solapamiento definidos en la [Guía visual en píxeles](TO_DO_Garden_Guia_Visual.md).
- Primer mapa finito de 24 × 24 celdas (768 × 768 píxeles base), más compacto para móvil; dimensiones revisables tras el prototipo.
- Catálogo de especies y decoraciones con IDs estables, hojas de tiles y animaciones definidas por datos.
- Una especie inicial con todas sus etapas antes de producir más contenido.
- Estética pixel art.
- Inspiración general en los jardines de criaturas de juegos clásicos, con identidad visual propia.
- Animaciones breves y expresivas.
- Escenario vivo y fácilmente legible.
- Diseño adaptable a diferentes tamaños de pantalla Android.

## 8. Arquitectura

La aplicación se dividirá en dos áreas principales:

### 8.1 Aplicación Android

Responsable de:

- Crear, editar y completar tareas.
- Gestionar carpetas.
- Mostrar listas, formularios y ajustes.
- Programar recordatorios.
- Guardar el progreso localmente.
- Iniciar y comunicar datos al jardín 2D.

### 8.2 Motor del jardín

Responsable de:

- Renderizar el escenario.
- Controlar la cámara y el zoom.
- Mostrar criaturas y elementos.
- Ejecutar animaciones.
- Gestionar el movimiento autónomo.
- Detectar pulsaciones sobre objetos.
- Representar el clima y el estado ambiental.

### 8.3 Capas de software

```text
Interfaz Android y jardín 2D
            ↓
ViewModels y controladores
            ↓
Casos de uso y reglas del dominio
            ↓
Repositorios
            ↓
Base de datos local y preferencias
```

Las reglas de las tareas y del jardín estarán separadas de la interfaz para facilitar las pruebas y una futura versión web.

## 9. Tecnologías

### Desarrollo Android

- **Kotlin:** lenguaje principal.
- **Linux + editor a elección:** entorno de desarrollo. Android Studio será opcional.
- **JDK + Android SDK de consola + Gradle Wrapper:** compilación e instalación de APK sin IDE obligatorio.
- **Jetpack Compose:** formularios, listas, menús, ajustes y navegación.
- **ViewModel, Coroutines y Flow:** gestión del estado y operaciones asíncronas.

### Motor 2D

- **libGDX:** renderizado y lógica del jardín.
- **OrthographicCamera:** desplazamiento y zoom.
- **Viewport:** adaptación a diferentes resoluciones.
- **SpriteBatch:** renderizado eficiente de sprites.
- **Listas de objetos y selección por coordenadas:** interacción sencilla, sin ECS ni Scene2D inicialmente.
- **Hojas de tiles y catálogo JSON:** especies, etapas, animaciones y decoraciones sin código específico por criatura.
- **Tiled:** diseño del mapa y definición de zonas transitables.

### Persistencia local

- **Room:** tareas, carpetas, criaturas, flores, lápidas y posiciones.
- **DataStore:** preferencias, sonido, música y configuración.
- **WorkManager:** revisiones periódicas de tareas vencidas.
- **Recordatorios:** diferidos a una fase posterior al MVP; si necesitan exactitud, se evaluará AlarmManager y sus requisitos de plataforma. WorkManager no garantiza una hora exacta.

### Recursos y desarrollo

- **Aseprite o LibreSprite:** creación de sprites y animaciones.
- **Git y GitHub:** control de versiones.
- **JUnit:** pruebas de reglas de negocio.

## 10. Datos principales

La aplicación almacenará localmente:

- Tareas.
- Fechas de creación, finalización y completado.
- Estado de cada tarea.
- Carpetas.
- Especie asociada a cada tarea; edad y etapa de la criatura calculadas a partir de las fechas.
- Resultados permanentes: plantas, flores y lápidas vinculadas de forma única a su tarea.
- Posición de elementos permanentes en el jardín, o ausencia de posición si están en la colección. El paseo y el fotograma de las criaturas son temporales.
- Mapa activo y versión. El estado ambiental se deriva del historial de tareas.
- Preferencias del usuario.

Cada elemento tendrá un identificador único para facilitar una futura sincronización con servidor.

## 11. Funcionamiento sin conexión

La primera versión funcionará completamente sin conexión.

- No será obligatorio crear una cuenta.
- El progreso se guardará en el dispositivo.
- Las tareas expiradas se comprobarán al abrir o retomar la aplicación, antes de modificar tareas y al alcanzar el siguiente vencimiento mientras está visible. Una revisión en segundo plano será complementaria.
- El crecimiento se calculará usando las fechas y la hora actual del dispositivo. No se ejecutará una simulación continua con la app cerrada.
- Desinstalar la aplicación o perder el dispositivo puede provocar la pérdida del progreso.

Se contempla añadir posteriormente exportación, copia de seguridad y sincronización entre dispositivos.

## 12. Principios de diseño

- El jardín representa el historial del usuario y no es solo decoración.
- Cada tarea deja una consecuencia visual.
- El estado debe entenderse de un vistazo.
- Las criaturas deben transmitir edad y personalidad mediante su comportamiento.
- La productividad debe sentirse motivadora, no punitiva.
- La primera versión evitará infraestructura innecesaria.
- El código deberá permitir añadir servidor, cuenta y versión web más adelante.


## 13. Alcance y reglas iniciales

El MVP incluye un jardín, una especie con sus etapas, tareas con fecha límite, carpetas, plantas y lápidas, colección de resultados sin colocar y ambiente básico. No incluye sincronización, editor de mapas dentro de la app, construcción libre, generación procedural, física ni recordatorios exactos.

- Cada tarea activa tiene una criatura; semilla, joven, adulta y anciana son etapas calculadas, no registros independientes.
- El vencimiento debe ser futuro al crear o editar. Cambiarlo conserva la fecha de creación y puede rejuvenecer a la criatura.
- Completar antes del límite crea un único resultado. En el instante del límite o después, la tarea expira y crea una única lápida.
- Cancelar antes del límite retira la criatura sin recompensa ni penalización. Una tarea ya vencida se resuelve antes de permitir otras acciones.
- Las tareas completadas o expiradas no se reabren en el MVP. El borrado irreversible queda fuera de esta versión.
- Las recompensas dependen de la fracción del plazo consumida, con umbrales iniciales definidos en la arquitectura y ajustables tras probar el juego.
- Añadir una especie o cambiar una animación debe requerir recursos y datos, sin una clase nueva de comportamiento.

La complejidad principal estará en crear contenido visual coherente y afinar las reglas. Los tiles reducen el trabajo de composición y movimiento, pero siguen siendo necesarias reglas explícitas para fechas, ocupación, selección, persistencia y capacidad del jardín.
