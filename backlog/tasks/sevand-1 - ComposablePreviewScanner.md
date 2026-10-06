---
id: SEVAND-1
title: ComposablePreviewScanner
status: Done
assignee:
  - '@claude'
created_date: '2026-10-06 15:38'
updated_date: '2026-10-06 23:01'
labels: []
dependencies: []
type: enhancement
ordinal: 1000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Investigate https://github.com/sergio-sastre/ComposablePreviewScanner

Te intent is to generate screenshots automatically from compose Previews. We're using compose official plugin, which requires creating files
in screenshotTest source set, with functions annotated with @PreviewTest. This require manual updates to the screenshot sources, which can
lead to forgotten screenshots or cases. (eg ScreensScreenshotTests)

I want to avoid this duplication by directly using previews from the main source set, adding a special annotation to them, and having them
generate the screenshots code necessary.

ComposablePreviewScanner sounds like a project that does precisely that. Investigate it to check if it suits what I want. If not, another
alternative is writing a custom kotlin compiler plugin to auto generate the screenshots code.
<!-- SECTION:DESCRIPTION:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [x] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Crear anotación @ScreenshotTest en main, con el suite como parámetro (SCREEN / COMPONENT).
2. Tarea Gradle en buildSrc que lee los .kt de main (sin compilar) y genera ScreensScreenshotTests.kt y ComponentsScreenshotTests.kt en build/generated/screenshotTest, con @Preview(locale = "es") + @PreviewTest. Cacheable y compatible con configuration cache.
3. Conectarla solo a la compilación de screenshotTest (registro lazy), para que assembleDebug, installDebug y test no la ejecuten.
4. Fallar el build si una preview anotada es private o no se puede interpretar.
5. Anotar las 62 previews que hoy tienen wrapper y borrar los wrappers escritos a mano.
6. Medir con --scan o --profile que assembleDebug no ejecuta la tarea y cuánto tarda en validateDebugScreenshotTest.
7. Validar con validateDebugScreenshotTest y actualizar CLAUDE.md.

8. Respetar previews día/noche: @PreviewLightDark o uiMode nocturno generan también un @Preview oscuro. Arreglar en la misma PR el stub dependiente del reloj que hacía fallar LineElementPreview.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Investigación ComposablePreviewScanner (CPS):
- CPS solo escanea previews (ClassGraph) en runtime y devuelve objetos ComposablePreview; no renderiza. Hay que combinarlo con Roborazzi, Paparazzi o tests instrumentados.
- No se integra con Compose Preview Screenshot Testing (plugin oficial): ese plugin solo descubre funciones @PreviewTest compiladas en el source set screenshotTest. Tampoco en alpha16 con AGP test suites (requiere AGP 9.5.0-alpha03; el proyecto usa 9.4.1).
- Conclusión: CPS no sirve tal cual con el setup actual. Adoptarlo implica cambiar de motor de render.
Situación actual: 62 wrappers @PreviewTest, todos con @Preview(locale = "es") idéntico, y 92 previews en main.
Opciones:
A) Roborazzi + generateComposePreviewRobolectricTests (usa CPS por dentro, opt-in con @RoboPreviewInclude). Robolectric: hay que regenerar todas las referencias y adaptar el workflow de CI y screenshot_report.py.
B) Paparazzi + paparazzi-plugin de CPS (layoutlib, pero es un ejemplo sin soporte que hay que copiar). Compatibilidad con AGP 9 sin verificar.
C) Mantener el plugin oficial y generar los wrappers con una tarea Gradle en buildSrc: escanea las clases compiladas de main (ClassGraph) buscando una anotación propia, escribe los wrappers en build/generated y los añade al source set screenshotTest. Sin cambiar motor, referencias ni CI. Recomendada.
D) Plugin de compilador Kotlin: descartado; API de K2 inestable y no puede generar código en otra compilación.
Extra barato: un test que falle si una preview anotada no tiene wrapper.

Implementado: anotación @ScreenshotTest(ScreenshotSuite.X) en main, GenerateScreenshotTestsTask en buildSrc y conexión vía androidComponents.onVariants + addGeneratedSourceDirectory. 62 previews anotadas, wrappers a mano borrados y referencias renombradas al nombre de la preview (mismo hash b2db1d68).
Verificación local: validateDebugScreenshotTest da 61/62 antes y después; el único fallo, LineElementPreview, ya existía y viene de que el stub usa la hora actual.
Rendimiento: assembleDebug, installDebug, testDebugUnitTest, lint y check no ejecutan el generador (--dry-run). Tarda 33 ms forzado y queda UP-TO-DATE sin cambios en los .kt.
Error controlado comprobado con una preview private.

Día/noche: 11 previews generan variante oscura (*_f6f1fda3_0.png). La variante clara se mantiene, con el mismo hash.
Stubs: horario fijo LocalTime.MIN–MAX en lugar de LocalTime.now(). Se actualizan las referencias de LineElementPreview, LinesScreenPreview y SearchScreenResultsPreview, que dependían de la hora.
Validación local: validateDebugScreenshotTest 73/73 y testDebugUnitTest OK. CI de la PR Sloy/sevibus-android#18: Run Tests, Build APK y Screenshot test results en verde.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Los tests de screenshots se generan a partir de previews anotadas con @ScreenshotTest(ScreenshotSuite.X), con una tarea Gradle en buildSrc que lee los .kt de main y solo se ejecuta al compilar screenshotTest (33 ms; assembleDebug, test, lint y check no la ejecutan). Se mantiene el plugin oficial y se soportan previews día/noche. Se arregla un stub dependiente del reloj. Verificado con 73/73 screenshots en local y en la CI de la PR Sloy/sevibus-android#18. El DoD #1 (en master) se completa al mergear la PR.
<!-- SECTION:FINAL_SUMMARY:END -->
