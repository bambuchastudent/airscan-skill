#!/usr/bin/env python3
"""Shared, fail-closed Graphify Java index for OpenCode agents.

Single writer via flock; multiple readers consume the atomically published JSON.
Unchanged source trees reuse the existing graph without extraction.
Changed trees currently rebuild the small Java AST (not incremental per-file merge).
"""
import argparse
import fcntl
import hashlib
import json
import os
from pathlib import Path
import tempfile

VERSION = "graphifyy-0.1.14-java-ast-v1"

def sources(root):
    return sorted(set(root.glob("src/**/*.java")) | set(root.glob("worker/src/**/*.java")))

def fingerprint(root, files):
    h = hashlib.sha256()
    h.update(VERSION.encode())
    for file in files:
        h.update(str(file.relative_to(root)).encode())
        h.update(hashlib.sha256(file.read_bytes()).digest())
    return h.hexdigest()

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--root", default=".")
    p.add_argument("--cache-dir", default=None)
    p.add_argument("--output", default=None, help="Optional evidence copy")
    a = p.parse_args()
    root = Path(a.root).resolve()
    key = hashlib.sha256(str(root).encode()).hexdigest()[:16]
    cache = Path(a.cache_dir).expanduser().resolve() if a.cache_dir else Path.home() / ".cache" / "airscan" / "graphify" / key
    cache.mkdir(parents=True, exist_ok=True)
    files = sources(root)
    assert files, "No Java sources to index"
    signature = fingerprint(root, files)
    graph = cache / "graph.json"
    meta = cache / "manifest.json"
    with (cache / "index.lock").open("a+") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        old = json.loads(meta.read_text()) if meta.exists() else {}
        hit = old.get("fingerprint") == signature and graph.exists()
        if not hit:
            from graphify.extract import extract
            result = extract(files)
            assert result.get("nodes"), "Graphify returned empty graph"
            manifest = {
                "version": VERSION, "root": str(root), "fingerprint": signature,
                "files": len(files), "nodes": len(result["nodes"]), "edges": len(result["edges"]),
                "update_strategy": "content-hash-skip-or-full-ast-rebuild",
            }
            for dest, payload in ((graph, result), (meta, manifest)):
                fd, tmp = tempfile.mkstemp(prefix=".graphify-", dir=cache)
                try:
                    with os.fdopen(fd, "w") as out:
                        json.dump(payload, out, indent=2)
                    os.replace(tmp, dest)
                finally:
                    if os.path.exists(tmp):
                        os.unlink(tmp)
        manifest = json.loads(meta.read_text())
        result = json.loads(graph.read_text())
        assert "BootstrapResource" in json.dumps(result)
        assert "BootstrapMarker" in json.dumps(result)
        if a.output:
            dest = Path(a.output)
            dest.mkdir(parents=True, exist_ok=True)
            (dest / "graph.json").write_text(graph.read_text())
            (dest / "manifest.json").write_text(meta.read_text())
        print(json.dumps({"cache_hit": hit, "cache_dir": str(cache), **manifest}, indent=2))

if __name__ == "__main__":
    main()
