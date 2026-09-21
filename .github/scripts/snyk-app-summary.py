#!/usr/bin/env python3
"""Publish a condensed Snyk application dependency scan summary.

Reads a Snyk JSON report and writes a Markdown summary table to
GITHUB_STEP_SUMMARY, plus a `has_fixable_vulns` output to GITHUB_OUTPUT.

Usage: ./snyk-app-summary.py <snyk-json-path>
"""

import json
import os
import sys


def main() -> None:
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <snyk-json-path>", file=sys.stderr)
        raise SystemExit(1)

    path = sys.argv[1]
    if not os.path.exists(path):
        raise SystemExit("No Snyk JSON output found.")

    with open(path) as f:
        root = json.load(f)

    results = root if isinstance(root, list) else [root]
    findings = {}
    fixable_found = False

    for result in results:
        if not isinstance(result, dict):
            continue

        project = (
            result.get("projectName")
            or result.get("displayTargetFile")
            or result.get("targetFile")
            or "unknown"
        )

        for vuln in result.get("vulnerabilities", []):
            if not isinstance(vuln, dict):
                continue

            pkg = vuln.get("packageName", "unknown")
            sev = str(vuln.get("severity", "unknown")).lower()
            current_version = vuln.get("version") or vuln.get("packageVersion") or "unknown"

            fixed = vuln.get("fixedIn") or vuln.get("nearestFixedInVersion")
            if isinstance(fixed, list):
                fixed = ", ".join(str(x) for x in fixed if x)
            elif fixed in (None, "", "None"):
                fixed = "Not available"

            upgrade = vuln.get("upgradePath") or []
            if isinstance(upgrade, list):
                upgrade = " -> ".join(str(x) for x in upgrade if x and x is not False)
            if not upgrade:
                upgrade = "No direct upgrade path"

            has_fix = False
            if isinstance(fixed, str) and fixed not in ("Not available", "None"):
                has_fix = True
            elif isinstance(upgrade, str) and upgrade and upgrade != "No direct upgrade path":
                has_fix = True

            if has_fix:
                fixable_found = True

            key = (project, pkg)
            item = findings.setdefault(key, {
                "severity": sev,
                "fixed": set(),
                "upgrade": set(),
                "paths": 0,
                "current_version": set(),
            })
            item["severity"] = max([item["severity"], sev], key=["low", "medium", "high", "critical"].index)
            if isinstance(fixed, str):
                item["fixed"].add(fixed)
            if isinstance(upgrade, str):
                item["upgrade"].add(upgrade)
            item["paths"] += 1
            item["current_version"].add(str(current_version))

    rows = [
        "## Snyk application dependency scan",
        "",
    ]

    if findings:
        rows.extend([
            "| Project | Dependency | Current version | Highest severity | Fixed in | Upgrade path | Affected paths |",
            "| --- | --- | --- | --- | --- | --- | --- |"
        ])

        for (project, pkg), info in sorted(findings.items(), key=lambda kv: (kv[0][0], kv[0][1])):
            fixed = ", ".join(sorted(info["fixed"]))
            upgrade = ", ".join(sorted(info["upgrade"])) or "No direct upgrade path"
            current_version = ", ".join(sorted(info["current_version"]))
            rows.append(f"| {project} | {pkg} | {current_version} | {info['severity'].title()} | {fixed} | {upgrade} | {info['paths']} |")

    with open(os.environ["GITHUB_STEP_SUMMARY"], "a") as out:
        out.write("\n".join(rows) + "\n")

    with open(os.environ["GITHUB_OUTPUT"], "a") as out:
        out.write(f"has_fixable_vulns={'true' if fixable_found else 'false'}\n")


if __name__ == "__main__":
    main()
