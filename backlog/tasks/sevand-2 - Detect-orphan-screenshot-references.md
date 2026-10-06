---
id: SEVAND-2
title: Detect orphan screenshot references
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-06 23:04'
updated_date: '2026-10-06 23:12'
labels: []
dependencies: []
type: enhancement
ordinal: 2000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
When a @ScreenshotTest preview is deleted, renamed, or loses its dark variant, its reference images stay in app/src/screenshotTestDebug/reference without any test using them. Detect them and remove them in a controlled way.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 validateDebugScreenshotTest fails and lists the orphan references
- [x] #2 updateDebugScreenshotTest and a dedicated task delete the orphan references
- [ ] #3 The CI comment shows the orphans and the update-screenshots label deletes them
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. El generador escribe la lista de referencias esperadas (<paquete>/<clase>/<test>_<hash>), con los hashes de sus dos configuraciones @Preview.
2. OrphanScreenshotReferencesTask compara esa lista con src/screenshotTest<Variant>/reference: falla listando las huérfanas o las borra.
3. check<Variant>ScreenshotReferences finaliza validate (falla) y delete<Variant>OrphanScreenshotReferences finaliza update (borra).
4. CI: el comentario lista las huérfanas y la etiqueta update-screenshots (y la PR de master) las borra.
5. CLAUDE.md.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Decisión: no borrar en validate ni en la generación; solo fallar. El borrado es un cambio en el código fuente y solo ocurre con acciones explícitas (update, la tarea delete o la etiqueta de CI).
Verificado en local: validate 73/73 con 0 huérfanas. Quitar @ScreenshotTest de AlertWidgetPreview hace fallar check listando la imagen, y la tarea delete la borra. Cambiar @PreviewLightDark por @Preview en StopTimelineElementPreview y lanzar update de ese test borra la referencia oscura y deja la clara idéntica. assembleDebug, testDebugUnitTest y check no ejecutan las tareas nuevas. Script del informe probado con y sin huérfanas.

Bloqueo: la GitHub App de la sesión no tiene permiso workflows, así que no pudo subir .github/workflows/screenshot-tests.yml. Los cambios de CI (screenshot_report.py, workflow y las frases de CI en CLAUDE.md) quedan en un parche para aplicarlo a mano. El AC #3 sigue pendiente de ese parche.
<!-- SECTION:NOTES:END -->
