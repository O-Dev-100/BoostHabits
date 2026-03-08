## BoostHabits — MVP para TFG (CFGS DAM)

BoostHabits es una aplicación Android pensada como Trabajo Fin de Grado para CFGS DAM. El objetivo es ayudar a los usuarios a crear y mantener buenos hábitos mediante un sistema de puntos, rachas (streaks), estadísticas visuales y un catálogo de recompensas virtuales.

La app está desarrollada en **Kotlin** siguiendo arquitectura **MVVM**, con capas bien separadas y código comentado para facilitar la memoria y la defensa del proyecto.

---

## Tecnologías principales

- **Plataforma**: Android (minSdk 21, targetSdk 36)
- **Lenguaje**: Kotlin
- **UI**: Layouts XML + Material Design
- **Arquitectura**: MVVM, Repositorios, Room (persistencia local)
- **Backend**: Firebase (Authentication, Firestore, Storage, Cloud Functions)
- **Servicios Google**:
  - Google Fit (registro de actividad)
  - Google Play Billing (suscripción premium en modo test)
  - Google Mobile Ads (AdMob Rewarded, opt‑in)
  - Play Integrity / SafetyNet (prevención básica de fraude)
- **Gráficas**: MPAndroidChart

> Nota: la configuración concreta de dependencias ya está aplicada en los `build.gradle.kts` del proyecto.

---

## Clonado del proyecto y apertura en Android Studio

1. Clonar el repositorio (ejemplo con HTTPS):

```bash
git clone <URL_DEL_REPO> BoostHabits
cd BoostHabits
```

2. Abrir el proyecto en **Android Studio**:
   - `File` → `Open...` → seleccionar la carpeta `BoostHabits`.
3. Esperar a que Gradle sincronice y descargue dependencias.
4. Seleccionar un dispositivo/emulador y ejecutar la app.

---

## Variables de entorno y configuración local

Para poder usar correctamente Firebase, AdMob y otros servicios, se recomienda centralizar las claves en un archivo `local.properties` o en variables de entorno del sistema. Para el TFG se usarán **claves de prueba** o proyectos de Firebase de desarrollo.

Lista sugerida de claves (todas pueden ser de **entorno de desarrollo / test**):

- `FIREBASE_WEB_API_KEY` — API key del proyecto Firebase.
- `FIREBASE_PROJECT_ID` — ID del proyecto Firebase.
- `FIREBASE_APP_ID_ANDROID` — App ID Android de Firebase.
- `ADMOB_APP_ID` — ID de aplicación AdMob (modo test).
- `ADMOB_REWARDED_AD_UNIT_ID` — ID de bloque de anuncio recompensado (modo test).
- `PLAY_BILLING_LICENSE_KEY` — Clave de licencia para billing (si aplica en modo test).
- `PLAY_INTEGRITY_TOKEN_PROVIDER_URL` — Endpoint de validación en Cloud Functions (futuro).

En la versión de defensa del TFG estas claves podrán estar:

- Codificadas como valores de prueba en `google-services.json` de un proyecto Firebase de desarrollo.
- O leídas desde `local.properties` (no se sube al repositorio).

---

## Flujo general de la aplicación (visión de alto nivel)

1. **Onboarding + Autenticación**
   - Primera ejecución: pantalla de bienvenida simple (onboarding) con explicación breve.
   - Login/registro con email/contraseña y Google Sign‑In (Firebase Auth).
2. **Gestión de hábitos**
   - Crear, editar y eliminar hábitos.
   - Registro diario de cumplimiento (manual + integración con Google Fit).
3. **Sistema de puntos y rachas**
   - Puntos acumulados por cumplir hábitos.
   - Rachas diarias con penalización si se rompe la secuencia.
4. **Estadísticas**
   - Gráficas de progreso usando MPAndroidChart.
5. **Recompensas y canje**
   - Catálogo de recompensas virtuales.
   - Canje con “vouchers” simulados generados desde Cloud Functions (Node.js).
6. **Monetización test / anti‑fraude**
   - Ads recompensados (opt‑in).
   - Suscripción premium en modo test.
   - Validaciones básicas con Play Integrity / SafetyNet y lógica en Cloud Functions.

---

## Fases del desarrollo (rama por fase)

Cada fase se desarrolla en una rama propia y se cierra con un commit claro. Nomenclatura sugerida:

- `feat/phase-1-base`
- `feat/phase-2-auth`
- `feat/phase-3-data-model`
- `feat/phase-4-habits-ui`
- (Fases posteriores: estadísticas, recompensas, billing/ads, anti‑fraude, etc.)

Se recomienda que el **merge** a la rama principal se haga tras pasar las pruebas manuales de la fase.

---

## Fase 1 — Base del proyecto (skeleton)

- **Objetivo**:
  - Dejar el proyecto Android configurado y compilando.
  - Configurar tema Material con tipografía Montserrat y paleta azul celeste/blanco.
  - `MainActivity` actuando como contenedor de `NavHostFragment` con toolbar.
- **Estado**: En progreso / Completada (ajustar según avance).
- **Branch sugerida**: `feat/phase-1-base`
- **Commit sugerido**: `feat: fase 1 - base del proyecto`

### Pruebas manuales Fase 1

- [ ] La app compila sin errores.
- [ ] La app se instala y abre en un dispositivo/emulador.
- [ ] Se muestra `MainActivity` con toolbar y contenido principal.
- [ ] El tema de la app utiliza colores azul celeste y blanco.
- [ ] La tipografía general es Montserrat (descargable o embebida).

---

## Fase 2 — Autenticación

- **Objetivo**:
  - Integrar Firebase Authentication (email/contraseña + Google Sign‑In).
  - Pantallas XML para login, registro y recuperación de contraseña.
  - Onboarding simple (first run) usando `SharedPreferences`.
  - Crear documento `users/{uid}` en Firestore al registrarse.
- **Estado**: Pendiente.
- **Branch sugerida**: `feat/phase-2-auth`
- **Commit sugerido**: `feat: fase 2 - autenticación`

### Pruebas manuales Fase 2

- [ ] Registrar usuario con email/contraseña.
- [ ] Iniciar sesión con el usuario registrado.
- [ ] Recuperar contraseña por email.
- [ ] Login con Google en un dispositivo con Play Services.
- [ ] Verificar en Firebase Firestore el documento `users/{uid}` creado correctamente.
- [ ] La sesión se mantiene entre aperturas de la app.

---

## Fase 3 — Modelado de datos local y remoto

- **Objetivo**:
  - Configurar Room (`AppDatabase`) con entidades:
    - `HabitEntity`, `HabitLogEntity`, `StreakEntity`, `RewardEntity`, `RedemptionEntity`.
  - Diseñar los DAOs necesarios.
  - Definir estructura de colecciones en Firestore.
  - Implementar un repositorio que sincronice Room ↔ Firestore (estrategia local‑first).
- **Estado**: Pendiente.
- **Branch sugerida**: `feat/phase-3-data-model`
- **Commit sugerido**: `feat: fase 3 - modelo de datos`

### Pruebas manuales Fase 3

- [ ] Crear un hábito y comprobar que se guarda en la base de datos local.
- [ ] Verificar que el hábito se sincroniza con Firestore.
- [ ] Modificar un hábito y comprobar la sincronización en ambos sentidos.
- [ ] Simular uso offline y posterior sincronización.

---

## Fase 4 — UI: gestión de hábitos

- **Objetivo**:
  - Implementar pantallas XML y fragments:
    - `fragment_habits_list.xml` (lista de hábitos, `RecyclerView`).
    - `fragment_habit_detail.xml` (detalle/edición de hábito).
    - `activity_habit_create.xml` (formulario para crear hábito).
    - `item_habit.xml` (vista de cada hábito en la lista).
  - ViewModel `HabitsViewModel` con la lógica de creación/edición/borrado.
  - Guardar hábitos en Room y sincronizar con Firestore.
- **Estado**: Pendiente.
- **Branch sugerida**: `feat/phase-4-habits-ui`
- **Commit sugerido**: `feat: fase 4 - gestión de hábitos`

### Pruebas manuales Fase 4

- [ ] Crear un hábito con nombre, dificultad, icono y objetivo diario.
- [ ] Ver la lista de hábitos y que se actualiza tras crear/editar/borrar.
- [ ] Editar un hábito existente y ver cambios reflejados.
- [ ] Eliminar un hábito y comprobar que desaparece de la lista.

---

## Notas para la memoria y defensa

- Mantener el código **comentado en español**, explicando las decisiones de diseño (no solo “qué hace”, sino “por qué se hace así”).
- Documentar en la memoria:
  - Diagrama de arquitectura (capas: UI, dominio, datos local/remoto).
  - Justificación de uso de Firebase y Google services.
  - Medidas anti‑fraude y limitaciones (no hay pagos en efectivo, solo recompensas virtuales).
- Preparar un **guion de demo**:
  - Crear cuenta → configurar hábitos → registrar días → ver rachas y puntos → canjear una recompensa → mostrar anuncios/suscripción en modo test.

