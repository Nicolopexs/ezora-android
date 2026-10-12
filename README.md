# EZORA Android

App nativa de Android para visitadores médicos: jornada, GPS en vivo, visitas, plan del ciclo,
directorio, facturas y reportes. Funciona sin señal y sincroniza sola.

Un solo código con dos sabores (product flavors):

| Sabor | Cliente | Servidor |
| --- | --- | --- |
| `cassara` | Cassará (Dympharma Ecuador) | https://cassaraecapp.com |
| `ezora` | EZORA | dominio propio (por definir) |

- Documento maestro (inventario completo y plan por fases):
  https://claude.ai/code/artifact/69d4707b-cd1b-41b9-806b-9455cdf0c6f7
- Stack: Kotlin, Jetpack Compose (Material 3), Room, WorkManager, Retrofit/OkHttp, Hilt, OpenStreetMap.
- Servidor: API `/api/v1` con token (Laravel Sanctum) en el backend de cada cliente.

## Compilar

1. Abrir esta carpeta en Android Studio.
2. Elegir la variante `cassaraRelease` (o `cassaraDebug` para pruebas).
3. *Build → Generate Signed App Bundle / APK* con la llave de firma de Cassará.
