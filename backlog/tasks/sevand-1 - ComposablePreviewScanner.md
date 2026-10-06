---
id: SEVAND-1
title: ComposablePreviewScanner
status: To Do
assignee: []
created_date: '2026-10-06 15:38'
updated_date: '2026-10-06 15:46'
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
