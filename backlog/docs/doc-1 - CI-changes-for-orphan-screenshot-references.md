---
id: doc-1
title: CI changes for orphan screenshot references
type: specification
created_date: '2026-10-07 06:19'
updated_date: '2026-10-07 07:49'
---
Pending part of SEVAND-9. The Gradle side (orphan check and delete tasks) is part of PR Sloy/sevibus-android#18. This diff makes the screenshot CI use it: the PR comment lists orphan references, and the `update-screenshots` label (and the master update PR) deletes them.

It could not be pushed from a Claude Code remote session: GitHub rejects changes to `.github/workflows/` from a GitHub App without the `workflows` permission. Push it from a local clone with `git apply` on this diff.

The report script and the workflow must land together: the script stops writing `update-filters.txt` and writes `update-args.txt` instead.

```diff
diff --git a/.github/scripts/screenshot_report.py b/.github/scripts/screenshot_report.py
index 24e8d26..bc4a3b4 100644
--- a/.github/scripts/screenshot_report.py
+++ b/.github/scripts/screenshot_report.py
@@ -2,13 +2,13 @@
 """
 Builds a PR comment from the screenshot test results.
 
-Reads the JUnit XML written by validateDebugScreenshotTest, copies the reference,
-new and diff images of every failing test into an output directory (to be pushed
-to a companion branch), and writes:
-  - comment.md: markdown with an image table per failing test. Image URLs use the
-    {IMAGES_URL} placeholder, replaced once the images are pushed.
-  - update-filters.txt: Gradle --tests filters for the failing tests, used to
-    regenerate only their references.
+Reads the JUnit XML written by validateDebugScreenshotTest and the orphan references
+listed by checkDebugScreenshotReferences, copies the reference, new and diff images of
+every failing test into an output directory (to be pushed to a companion branch), and writes:
+  - comment.md: markdown with an image table per failing test and the orphan references.
+    Image URLs use the {IMAGES_URL} placeholder, replaced once the images are pushed.
+  - update-args.txt: Gradle arguments that regenerate only the failing references and
+    delete the orphan ones. Empty when there is nothing to update.
 """
 import argparse
 import glob
@@ -72,21 +72,38 @@ def image_cell(name):
     return f'<img src="{{IMAGES_URL}}/{name}" width="250">' if name else "—"
 
 
-def build_comment(failures, images_dir, run_url):
+def parse_orphans(orphans_file):
+    if not orphans_file or not os.path.isfile(orphans_file):
+        return []
+    with open(orphans_file) as f:
+        return [line.strip() for line in f if line.strip()]
+
+
+def build_comment(failures, orphans, images_dir, run_url):
     lines = [MARKER]
-    if not failures:
+    if not failures and not orphans:
         lines.append("### ✅ Screenshot tests passed")
         lines.append("")
         lines.append(f"All screenshots match their references. [Workflow run]({run_url})")
         return "\n".join(lines)
 
-    lines.append(f"### ❌ {len(failures)} screenshot test(s) failed")
+    if failures:
+        lines.append(f"### ❌ {len(failures)} screenshot test(s) failed")
+    else:
+        lines.append(f"### ❌ {len(orphans)} orphan screenshot reference(s)")
     lines.append("")
     lines.append(
         "If the changes are expected, add the `update-screenshots` label to this PR "
-        "to regenerate the references in a new commit."
+        "to regenerate the references and delete the orphan ones in a new commit."
     )
     lines.append("")
+    if orphans:
+        lines.append("<details open><summary><b>Orphan references</b>: no @ScreenshotTest preview uses them</summary>")
+        lines.append("")
+        lines.extend(f"- `{orphan}`" for orphan in orphans)
+        lines.append("")
+        lines.append("</details>")
+        lines.append("")
     for failure in failures:
         test_id = f"{failure['test_class']}.{failure['test_name']}"
         reference = copy_image(failure["reference"], images_dir, f"{test_id}.reference.png")
@@ -110,16 +127,23 @@ def main():
     parser.add_argument("--images-dir", required=True)
     parser.add_argument("--output-dir", required=True)
     parser.add_argument("--run-url", required=True)
+    parser.add_argument("--orphans-file")
     args = parser.parse_args()
 
     failures = parse_failures(args.results_dir)
+    orphans = parse_orphans(args.orphans_file)
     os.makedirs(args.output_dir, exist_ok=True)
     with open(os.path.join(args.output_dir, "comment.md"), "w") as f:
-        f.write(build_comment(failures, args.images_dir, args.run_url))
-    with open(os.path.join(args.output_dir, "update-filters.txt"), "w") as f:
-        for failure in failures:
-            f.write(f"--tests *.{failure['test_class']}.{failure['test_name']}\n")
-    print(f"{len(failures)} failing screenshot test(s)")
+        f.write(build_comment(failures, orphans, args.images_dir, args.run_url))
+    with open(os.path.join(args.output_dir, "update-args.txt"), "w") as f:
+        # Updating the references also deletes the orphan ones
+        if failures:
+            f.write(":app:updateDebugScreenshotTest\n")
+            for failure in failures:
+                f.write(f"--tests *.{failure['test_class']}.{failure['test_name']}\n")
+        elif orphans:
+            f.write(":app:deleteDebugOrphanScreenshotReferences\n")
+    print(f"{len(failures)} failing screenshot test(s), {len(orphans)} orphan reference(s)")
 
 
 if __name__ == "__main__":
diff --git a/.github/workflows/screenshot-tests.yml b/.github/workflows/screenshot-tests.yml
index 87c2f79..7e747fd 100644
--- a/.github/workflows/screenshot-tests.yml
+++ b/.github/workflows/screenshot-tests.yml
@@ -86,14 +86,17 @@ jobs:
             --results-dir app/build/test-results/validateDebugScreenshotTest \
             --images-dir "$IMAGES_DIR" \
             --output-dir "$REPORT_DIR" \
-            --run-url "${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}"
+            --run-url "${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}" \
+            --orphans-file app/build/reports/screenshotTest/orphans-debug.txt
 
       - name: Update failing references
         id: update
         if: env.UPDATE == 'true' && steps.validate.outcome == 'failure' && github.event_name == 'pull_request'
         run: |
           set -f  # keep the --tests patterns from being expanded by the shell
-          ./gradlew :app:updateDebugScreenshotTest $(cat "$REPORT_DIR/update-filters.txt")
+          if [ -s "$REPORT_DIR/update-args.txt" ]; then
+            ./gradlew $(cat "$REPORT_DIR/update-args.txt")
+          fi
           git add app/src/screenshotTestDebug/reference
           if git diff --cached --quiet; then
             echo "No reference changed"
@@ -119,7 +122,9 @@ jobs:
           ACTOR: ${{ github.actor }}
         run: |
           set -f  # keep the --tests patterns from being expanded by the shell
-          ./gradlew :app:updateDebugScreenshotTest $(cat "$REPORT_DIR/update-filters.txt")
+          if [ -s "$REPORT_DIR/update-args.txt" ]; then
+            ./gradlew $(cat "$REPORT_DIR/update-args.txt")
+          fi
           base=$(git rev-parse HEAD)
           git switch -c "$MASTER_UPDATE_BRANCH"
           git add app/src/screenshotTestDebug/reference
diff --git a/CLAUDE.md b/CLAUDE.md
index 612c6da..7401e3a 100644
--- a/CLAUDE.md
+++ b/CLAUDE.md
@@ -366,9 +366,9 @@ app/src/
 
 `.github/workflows/screenshot-tests.yml` validates the screenshots on every pull request and every push to master.
 
-On pull requests, failures are reported in a single PR comment (updated on each run) with the reference, new and diff images. The images are pushed to a `screenshots/pr-<number>` companion branch, deleted when the PR is closed. Add the `update-screenshots` label to regenerate the references of the failing tests, commit them to the PR branch and remove the label. Use it instead of running `updateDebugScreenshotTest` locally. Commits pushed by the workflow don't trigger new runs, so push again to validate them. PRs from forks are skipped, since the workflow needs write access.
+On pull requests, failures are reported in a single PR comment (updated on each run) with the reference, new and diff images. The images are pushed to a `screenshots/pr-<number>` companion branch, deleted when the PR is closed. Orphan references are listed in the same comment. Add the `update-screenshots` label to regenerate the references of the failing tests, delete the orphan ones, commit them to the PR branch and remove the label. Use it instead of running `updateDebugScreenshotTest` locally. Commits pushed by the workflow don't trigger new runs, so push again to validate them. PRs from forks are skipped, since the workflow needs write access.
 
-On pushes to master, if any screenshot fails, it regenerates the failing references on the `screenshots/master-update` branch and opens a PR assigned to the pusher (or refreshes the open one and comments on it). Merge it if the changes are expected, otherwise close it and fix the UI. When master passes again, an open update PR is closed. Opening PRs requires **Settings → Actions → General → Allow GitHub Actions to create and approve pull requests**.
+On pushes to master, if any screenshot fails or a reference is orphan, it regenerates the failing references, deletes the orphan ones on the `screenshots/master-update` branch and opens a PR assigned to the pusher (or refreshes the open one and comments on it). Merge it if the changes are expected, otherwise close it and fix the UI. When master passes again, an open update PR is closed. Opening PRs requires **Settings → Actions → General → Allow GitHub Actions to create and approve pull requests**.
 
 The comment is built by `.github/scripts/screenshot_report.py` from the JUnit results. The full HTML report is also uploaded as the `screenshot-report` artifact.
 
```
