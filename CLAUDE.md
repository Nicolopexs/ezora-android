# EZORA Android — guía para agentes

App nativa (Kotlin + Jetpack Compose) del visitador médico. Un solo código, dos clientes:

- Sabor `cassara`: applicationId `com.cassara.app` (reemplaza la APK de Capacitor, misma firma), API `https://cassaraecapp.com/api/v1/`.
- Sabor `ezora`: applicationId `com.ezora.app`, dominio propio que define Nicolás (no usar el de Cassará).

## Reglas
- Todo texto visible en español, sin emojis.
- Nada específico de un cliente dentro de `main/`: usar `Brand` (BuildConfig) o recursos del sabor.
- Offline primero: la pantalla lee de Room / `KvDao`; la red solo actualiza. Toda escritura lleva cabecera `X-Offline-Request-Id`.
- GPS: separar mañana/tarde solo por tiempo (`lunchStartTs`/`lunchEndTs`); nunca enviar ni dibujar puntos en (0, 0).
- No subir llaves de firma, `keystore.properties` ni `google-services.json`.
- La especificación completa (IDs AUTH-, INI-, GPS-, VIS-, …) está en el documento maestro del proyecto.

## Compilar
`./gradlew assembleCassaraDebug` (se instala como `com.cassara.app.dev`, junto a la app real).
GitHub Actions compila ambos sabores en cada push a `main` y deja las APK como artefactos.

## Backend
Rama `feat/api-v1` del repo `cassara-app`: rutas en `routes/api.php`, controladores en `app/Http/Controllers/Api/V1`.
