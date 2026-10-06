---
id: SEVAND-1
title: ComposablePreviewScanner
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-06 15:38'
updated_date: '2026-10-06 22:05'
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
- [ ] #2 Screenshot tests passed

<!-- DOD:END -->

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
<!-- SECTION:NOTES:END -->
