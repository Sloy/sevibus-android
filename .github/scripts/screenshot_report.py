#!/usr/bin/env python3
"""
Builds a PR comment from the screenshot test results.

Reads the JUnit XML written by validateDebugScreenshotTest, copies the reference,
new and diff images of every failing test into an output directory (to be pushed
to a companion branch), and writes:
  - comment.md: markdown with an image table per failing test. Image URLs use the
    {IMAGES_URL} placeholder, replaced once the images are pushed.
  - update-filters.txt: Gradle --tests filters for the failing tests, used to
    regenerate only their references.
"""
import argparse
import glob
import os
import re
import shutil
import xml.etree.ElementTree as ET

REFERENCE_DIR = "app/src/screenshotTestDebug/reference/"
RENDERED_DIR = "app/build/outputs/screenshotTest-results/preview/debug/rendered/"
MARKER = "<!-- screenshot-tests-report -->"


def parse_failures(results_dir):
    results = sorted(glob.glob(os.path.join(results_dir, "*.xml")))
    if not results:
        raise SystemExit(f"No test results in {results_dir}, the tests didn't run")
    failures = []
    for path in results:
        for case in ET.parse(path).getroot().iter("testcase"):
            problem = case.find("failure")
            if problem is None:
                problem = case.find("error")
            if problem is None:
                continue
            message = problem.get("message") or problem.text or ""
            failure = {
                "test_class": case.get("classname").split(".")[-1],
                "test_name": case.get("name").split(" ")[0],
                "summary": message.splitlines()[0] if message else "Unknown error",
                "reference": None,
                "actual": None,
                "diff": None,
            }
            expected = re.search(r"Expected: (\S+)", message)
            actual = re.search(r"Actual: (\S+)", message)
            diff = re.search(r"Diff Image: (\S+)", message)
            missing = re.search(r"Reference image file does not exist \((\S+?)\)", message)
            if expected:
                failure["reference"] = expected.group(1)
            if actual:
                failure["actual"] = actual.group(1)
            if diff:
                failure["diff"] = diff.group(1)
            if missing:
                failure["summary"] = "New screenshot, no reference image yet"
                failure["actual"] = missing.group(1).replace(REFERENCE_DIR, RENDERED_DIR)
            failures.append(failure)
    return failures


def copy_image(source, images_dir, name):
    if not source or not os.path.isfile(source):
        return None
    os.makedirs(images_dir, exist_ok=True)
    shutil.copyfile(source, os.path.join(images_dir, name))
    return name


def image_cell(name):
    return f'<img src="{{IMAGES_URL}}/{name}" width="250">' if name else "—"


def build_comment(failures, images_dir, run_url):
    lines = [MARKER]
    if not failures:
        lines.append("### ✅ Screenshot tests passed")
        lines.append("")
        lines.append(f"All screenshots match their references. [Workflow run]({run_url})")
        return "\n".join(lines)

    lines.append(f"### ❌ {len(failures)} screenshot test(s) failed")
    lines.append("")
    lines.append(
        "If the changes are expected, add the `update-screenshots` label to this PR "
        "to regenerate the references in a new commit."
    )
    lines.append("")
    for failure in failures:
        test_id = f"{failure['test_class']}.{failure['test_name']}"
        reference = copy_image(failure["reference"], images_dir, f"{test_id}.reference.png")
        actual = copy_image(failure["actual"], images_dir, f"{test_id}.new.png")
        diff = copy_image(failure["diff"], images_dir, f"{test_id}.diff.png")
        lines.append(f"<details open><summary><b>{test_id}</b>: {failure['summary']}</summary>")
        lines.append("")
        lines.append("| Reference | New | Diff |")
        lines.append("|---|---|---|")
        lines.append(f"| {image_cell(reference)} | {image_cell(actual)} | {image_cell(diff)} |")
        lines.append("")
        lines.append("</details>")
        lines.append("")
    lines.append(f"[Workflow run]({run_url})")
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--results-dir", required=True)
    parser.add_argument("--images-dir", required=True)
    parser.add_argument("--output-dir", required=True)
    parser.add_argument("--run-url", required=True)
    args = parser.parse_args()

    failures = parse_failures(args.results_dir)
    os.makedirs(args.output_dir, exist_ok=True)
    with open(os.path.join(args.output_dir, "comment.md"), "w") as f:
        f.write(build_comment(failures, args.images_dir, args.run_url))
    with open(os.path.join(args.output_dir, "update-filters.txt"), "w") as f:
        for failure in failures:
            f.write(f"--tests *.{failure['test_class']}.{failure['test_name']}\n")
    print(f"{len(failures)} failing screenshot test(s)")


if __name__ == "__main__":
    main()
