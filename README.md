# TO DO Garden

Aplicación Android local de tareas representadas por criaturas-planta en un jardín 2D de tiles.

## Prototipo Android · 0.1

Proyecto importable desde la carpeta raíz en Android Studio, con dos módulos:

- `app/`: menú y controles en Jetpack Compose, jardines guardados con Room e integración Android.
- `garden/`: motor Kotlin/JVM + libGDX, carga de Tiled, cámara y agua animada.
- `assets/`: recursos originales compartidos; Gradle los incluye directamente en la APK.

### Qué se puede hacer

Crear jardines con nombre, seleccionarlos y volver a abrirlos después de cerrar la aplicación. Todos usan por ahora la plantilla `garden_01`, con su identificador y versión guardados en Room. Se puede arrastrar el mapa, ampliar con pinza o botones, recuperar la vista completa con **Centrar** o doble toque y pausar las ondas del estanque.

El agua se detecta mediante la propiedad `terrainId` del TSX (`water_*`). Las ondas se dibujan sobre esas celdas sin modificar el PNG. Se respetan el filtrado nearest-neighbor y las capas de decoración; las capas lógicas no se dibujan. La simulación se pausa al salir de la pantalla.

Esta entrega no incluye todavía tareas, criaturas, edición del terreno ni colocación de objetos de `props`. La cámara se reinicia al volver a entrar. Los jardines comparten el aspecto del mapa inicial, pero tienen registros independientes para ampliar el modelo después.

### Abrir en Android Studio

1. **Open** → seleccionar esta carpeta (`todo_garden`).
2. Usar **JDK 17** para Gradle e instalar **Android SDK 35** y **Build Tools 35.0.0** desde SDK Manager.
3. Sincronizar Gradle, seleccionar la configuración **app** y ejecutar en un dispositivo o emulador **Android 8.0 / API 26 o superior**.

La configuración fija Gradle 8.11.1, AGP 8.9.2, Kotlin 2.1.20 y libGDX 1.14.0. Se incluyen el Wrapper y las bibliotecas nativas para ARM y emuladores x86. La primera sincronización necesita acceso a los repositorios de dependencias.

### Compilar por terminal

Configura `JAVA_HOME` con JDK 17 y `ANDROID_HOME` con tu SDK, o crea un `local.properties` con `sdk.dir=/ruta/al/sdk`.

```sh
./gradlew :garden:test :app:assembleDebug :app:lintDebug
python3 scripts/validate_assets.py
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

### Validación realizada

- `:garden:test`: 3 pruebas de cámara correctas.
- `:app:assembleDebug`: APK generada y firma debug verificada.
- `:app:lintDebug`: 0 errores; 8 avisos sobre versiones de dependencias y uso de kapt.
- `scripts/validate_assets.py`: capas, referencias, terrenos, agua y aparición correctos.
- APK inspeccionada: TMX, TSX, PNG y librerías nativas de las cuatro ABI incluidas.

La ejecución visual queda pendiente: este entorno no permite usar KVM y el emulador por software no completó un arranque estable para instalar la APK. La compilación no sustituye la comprobación de gestos y ciclo de vida en un dispositivo.

### Comprobación en dispositivo

- Crear dos jardines; volver al menú y seleccionar cada uno.
- Cerrar y abrir la aplicación; comprobar que se conservan los nombres.
- Ampliar y arrastrar hasta las esquinas; la cámara debe detenerse en los límites.
- Usar pinza, botones y Centrar; probar también la orientación horizontal.
- Observar las ondas, pausarlas y reanudarlas.
- Mandar la app al fondo y volver; comprobar mapa, gestos y animación.

### Documentación de producto

- [Especificación del producto, versión 0.4](TO_DO_Garden_Especificacion_v0.2.md)
- [Arquitectura y plan de implementación](TO_DO_Garden_Arquitectura.md)
- [Guía visual: tamaños, estados y hojas PNG](TO_DO_Garden_Guia_Visual.md)

La integración sigue el [alojamiento en fragments de libGDX](https://libgdx.com/wiki/app/starter-classes-and-configuration); la combinación de Gradle y JDK corresponde a los [requisitos de AGP 8.9](https://developer.android.com/build/releases/agp-8-9-0-release-notes).

## Fondo de ejemplo

Abrir [preview/index.html](preview/index.html) directamente en el navegador, sin servidor ni instalación. Permite mostrar la cuadrícula y las zonas reservadas. También hay una [imagen del fondo](preview/background.png).

El ejemplo respeta las 24 × 24 celdas de 32 px. Contiene césped, un estanque con tres profundidades de color, caminos principales de dos celdas y tres grupos de parcelas. Agua y borde bloquean el paseo; las parcelas se reservan para plantas. No incluye criaturas ni edificios.

- `assets/maps/garden_01.tmx`: mapa editable en Tiled, con las siete capas de la arquitectura y un anclaje `spawn_main`.
- `assets/tilesets/garden.tsx`: tileset con identificadores de terreno.
- `assets/textures/garden.png`: hoja provisional de 416 × 32 px, con 13 tiles sin márgenes ni separación.

Para sustituir los colores por arte definitivo, reemplazar `garden.png` conservando el tamaño y el orden de los tiles: cuatro de césped, tres de tierra, camino, orilla, agua clara, agua media, agua profunda y borde. Tanto Tiled como la vista HTML leen ese PNG. Si se añaden transiciones o se reorganiza la hoja, actualizar el tileset y el mapa.

`python3 scripts/build_background.py` regenera los recursos provisionales, `preview/map.js` y la imagen estática. **Sobrescribe el PNG y el mapa**: usarlo solo durante esta fase de prueba, antes de editar el mapa en Tiled o incorporar arte definitivo. La vista HTML muestra una copia generada del mapa inicial; las modificaciones posteriores en Tiled se pueden previsualizar en Tiled.
