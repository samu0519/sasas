# Dynamic Island (Xiaomi 15T Pro / HyperOS)

App propia inspirada en la Dynamic Island de iPhone, hecha con Kotlin + Jetpack Compose + Material 3.
No reimplementa funciones nativas de HyperOS (música, temporizadores, reloj, mapas...): se centra
exclusivamente en mostrar de forma elegante las notificaciones que tú elijas.

## Cómo abrir el proyecto

1. Abre Android Studio (Koala o superior recomendado).
2. `File > Open` y selecciona la carpeta `DynamicIsland`.
3. Deja que Gradle sincronice. **Nota:** el proyecto no incluye el JAR del Gradle Wrapper
   (`gradle/wrapper/gradle-wrapper.jar`) porque este entorno no tiene acceso a `services.gradle.org`.
   Android Studio lo generará automáticamente al sincronizar, o puedes ejecutar
   `gradle wrapper --gradle-version 8.7` una vez tengas Gradle instalado localmente.
4. Ejecuta en tu Xiaomi 15T Pro (Android 15 / HyperOS).

## Compilar en GitHub (CI)

El proyecto incluye un workflow de GitHub Actions en `.github/workflows/android-build.yml` que
compila el APK automáticamente en cada `push`/`pull request` a `main` o `master`, y también se
puede lanzar a mano desde la pestaña **Actions** del repo (botón "Run workflow").

Qué hace el workflow:

1. Instala JDK 17 y configura Gradle con la acción oficial `gradle/actions/setup-gradle`, usando
   la versión indicada en `gradle/wrapper/gradle-wrapper.properties` (no hace falta que el JAR del
   wrapper esté commiteado).
2. Compila `assembleDebug` y `assembleRelease` (el release sale sin firmar).
3. Sube ambos APKs como *artifacts* descargables desde la propia ejecución del workflow (pestaña
   Actions → la ejecución → sección "Artifacts").

Pasos para usarlo:

1. Sube el proyecto a un repositorio de GitHub (incluida la carpeta `.github`).
2. Ve a la pestaña **Actions**: el workflow se ejecutará solo, o dale a "Run workflow" para
   lanzarlo manualmente.
3. Cuando termine, descarga el APK desde los *artifacts* de esa ejecución.

Si en algún momento quieres firmar el APK de release automáticamente, hay que añadir un keystore
como *secret* del repo y un paso extra de firma en el workflow — dímelo si lo necesitas y lo añado.

## Primer uso: permisos obligatorios

La app te los pedirá desde la pantalla principal (son 3 switches):

1. **Superposición de pantalla** (`SYSTEM_ALERT_WINDOW`) — necesario para dibujar la isla sobre
   cualquier app.
2. **Acceso a notificaciones** (`NotificationListenerService`) — necesario para leer las
   notificaciones que quieras mostrar.
3. **Ignorar optimización de batería** — recomendado en HyperOS/MIUI para que el sistema no mate
   el servicio en segundo plano.

Después, marca en la lista de apps cuáles quieres que aparezcan en la isla (por defecto ninguna
está activada, para no saturarte con notificaciones no deseadas).

## Arquitectura (MVVM)

```
data/
  model/        -> IslandNotification, IslandSettings (inmutables)
  datastore/     -> SettingsDataStore (Preferences DataStore)
  repository/    -> SettingsRepository, NotificationRepository (fuente única de verdad)
service/
  IslandNotificationListenerService -> escucha notificaciones del sistema
  IslandOverlayService              -> Foreground Service que dibuja la isla con WindowManager
ui/
  island/   -> IslandViewModel (cola de notificaciones, auto-colapso), IslandComposable (UI),
               IslandOverlayHost (Lifecycle/ViewModelStore/SavedState manual para el overlay)
  settings/ -> SettingsViewModel + SettingsScreen (Material 3)
  theme/    -> tema oscuro AMOLED
util/
  PermissionUtils, AppIconProvider, Constants
```

### Por qué un `Service` y no solo una `Activity`

La isla debe verse **encima de cualquier app**, no solo dentro de la tuya. Por eso
`IslandOverlayService` es un Foreground Service que añade una ventana `TYPE_APPLICATION_OVERLAY`
con `WindowManager`, independiente del ciclo de vida de `MainActivity`. Como un `ComposeView` fuera
de una Activity no tiene `LifecycleOwner`/`ViewModelStoreOwner`/`SavedStateRegistryOwner` propios,
`IslandOverlayHost` los provee manualmente.

### Rendimiento

- Todo es reactivo (`Flow`/`StateFlow`), sin *polling* ni `Handler` en bucle.
- Iconos de apps cacheados en memoria (`LruCache`) vía `AppIconProvider`.
- Animaciones con `spring()` (Compose), aceleradas por hardware, sin lógica de frames manual.
- `START_STICKY` en el servicio para recuperarse tras ser matado por el sistema, sin *wake locks*
  innecesarios.

## Personalización

Todo se guarda con DataStore y se aplica en caliente (sin reiniciar la app):
ancho/alto colapsado y expandido, radio de bordes, posición vertical, velocidad de animación,
duración visible y transparencia del fondo.

## Limitaciones conocidas

- El *blur* real (`RenderEffect.createBlurEffect`) solo está disponible desde API 31; por debajo se
  usa un fondo semitransparente como *fallback*. Tu Xiaomi 15T Pro (Android 15) tendrá blur nativo.
  Si quieres forzarlo también en el fallback, dímelo y lo añadimos en `IslandComposable`.
- Algunos fabricantes (incluida HyperOS/MIUI) restringen agresivamente los overlays y servicios en
  segundo plano. Si la isla desaparece tras un rato, revisa "Inicio automático" y "Ahorro de
  batería" para esta app en los ajustes del sistema.
