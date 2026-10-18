#!/usr/bin/env python3
"""Bootstrap Java source smoke; lexical evidence, not semantic reference resolution."""
import argparse
import json
from pathlib import Path

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--root", default=".")
    p.add_argument("--output", required=True)
    a = p.parse_args()
    root = Path(a.root)
    output = Path(a.output)
    output.mkdir(parents=True, exist_ok=True)
    files = sorted(root.glob("src/**/*.java"))
    resource = next((f for f in files if f.name == "BootstrapResource.java"), None)
    marker = next((f for f in files if f.name == "BootstrapMarker.java"), None)
    assert resource and marker, "Missing bootstrap Java source"
    source = resource.read_text()
    dependency = marker.read_text()
    checks = {
        "resource_class": "class BootstrapResource" in source,
        "resource_injects_marker": "BootstrapMarker marker" in source,
        "resource_calls_marker_status": "marker.status()" in source,
        "marker_declares_status": "String status()" in dependency,
    }
    report = {
        "kind": "lexical-source-smoke",
        "semantic_references_verified": False,
        "java_files": len(files),
        "resource": str(resource),
        "marker": str(marker),
        "checks": checks,
    }
    (output / "report.json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))
    assert all(checks.values()), "Java bootstrap source relationship smoke failed"

if __name__ == "__main__":
    main()
