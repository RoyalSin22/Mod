#!/usr/bin/env python3
"""Conserva instantáneas exactas del Osario antes de V84; nunca sobrescribe archivos distintos."""
import hashlib
import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
REFS = ROOT / "tools/models/blockbench/ref"
MAIN = "20bc23030ff90660b2c50ac9e50e707a3f462ca9"
PREVIOUS = "1534389a752513da383ea9bc5a0f172644cac6bd"


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT)


def preserve(path, data):
    if path.exists():
        if path.read_bytes() != data:
            raise RuntimeError(f"Se conserva sin sobrescribir el archivo distinto: {path}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("xb") as stream:
        stream.write(data)


def archive(revision, directory):
    paths = git("ls-tree", "-r", "--name-only", revision).decode().splitlines()
    selected = [p for p in paths if "osario" in p.lower() and "gran_osario" not in p.lower()
                and "/boss/" not in p and p != "docs/V68_JEFE_OSARIO.md"]
    selected += ["tools/models/gen_mob_models.py", "src/client/java/dev/aptitudes/client/render/MobRenderers.java"]
    entries = []
    for name in sorted(set(selected)):
        data = git("show", f"{revision}:{name}")
        preserve(directory / name, data)
        entries.append({"path": name, "bytes": len(data), "sha256": hashlib.sha256(data).hexdigest(),
                        "git_blob": git("rev-parse", f"{revision}:{name}").decode().strip()})
    manifest = {"revision": revision, "files": entries,
                "note": "Instantánea histórica; no integrar ni usar como generador de los recursos activos."}
    preserve(directory / "manifest.json", (json.dumps(manifest, ensure_ascii=False, indent=2) + "\n").encode())
    print(f"{directory.relative_to(ROOT)}: {len(entries)} archivos, bytes y SHA-256 verificados")


if __name__ == "__main__":
    archive(PREVIOUS, REFS / "osario_v65_codex")
    archive(MAIN, REFS / "placeholder_main")
