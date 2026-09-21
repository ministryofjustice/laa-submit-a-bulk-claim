#!/usr/bin/env python3
"""Publish a condensed Snyk code scan summary.

Reads a Snyk SARIF report and writes a Markdown summary table to
GITHUB_STEP_SUMMARY.

Usage: ./snyk-code-summary.py
"""

import json
import os

REPORT_PATH = "snyk-code.sarif"


def main() -> None:
    if not os.path.exists(REPORT_PATH):
        print("No Snyk SARIF output found.")
        return

    with open(REPORT_PATH) as f:
        root = json.load(f)

    severity_order = ["note", "warning", "error"]
    severity_label = {"note": "Low", "warning": "Medium", "error": "High"}

    rows = []
    for run in root.get("runs", []):
        rules = {
            rule.get("id"): rule
            for rule in run.get("tool", {}).get("driver", {}).get("rules", [])
        }

        for result in run.get("results", []):
            rule_id = result.get("ruleId", "unknown")
            rule = rules.get(rule_id, {})

            level = result.get("level") or rule.get("defaultConfiguration", {}).get("level", "warning")
            severity = severity_label.get(level, level.title())

            title = rule.get("shortDescription", {}).get("text") or rule_id
            message = result.get("message", {}).get("text", "")

            for location in result.get("locations", []) or [{}]:
                physical = location.get("physicalLocation", {})
                uri = physical.get("artifactLocation", {}).get("uri", "unknown")
                line = physical.get("region", {}).get("startLine", "?")

                rows.append({
                    "severity": level,
                    "severity_label": severity,
                    "rule": title,
                    "file": uri,
                    "line": line,
                    "message": message,
                })

    summary_rows = [
        "## Snyk code scan",
        "",
        "| Severity | Rule | File | Line | Message |",
        "| --- | --- | --- | --- | --- |",
    ]

    if rows:
        rows.sort(key=lambda r: (-severity_order.index(r["severity"]) if r["severity"] in severity_order else 0, r["file"], r["line"]))
        for row in rows:
            message = row["message"].replace("|", "\\|").replace("\n", " ")
            summary_rows.append(
                f"| {row['severity_label']} | {row['rule']} | {row['file']} | {row['line']} | {message} |"
            )
    else:
        summary_rows.append("| - | - | - | - | No issues found |")

    print("\n".join(summary_rows))


if __name__ == "__main__":
    main()
