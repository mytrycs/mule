"""Read-only, timed sibling content comparison; never runs a runtime.

The initial snapshot was taken before installed-host tests, after UI work had
begun. This comparison cannot establish a retroactive task-start snapshot.
"""
import hashlib
import json
import os
from pathlib import Path
from datetime import datetime, timezone

project = Path(__file__).resolve().parents[1]
before_path = project / "evidence/sibling-snapshot-before.json"
before = json.loads(before_path.read_text(encoding="utf-8"))
allowed_roots = {"mule-kernel-evaluation", "zernova-runtime-foundation-phase1"}
assert set(before["roots"]) == allowed_roots


def digest_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def snapshot(root):
    assert root.is_dir(), f"Snapshot root unavailable: {root.name}"
    entries = []
    for directory, directories, files in os.walk(root, followlinks=False):
        directory_path = Path(directory)
        for name in directories[:]:
            path = directory_path / name
            if path.is_symlink():
                entries.append({"path": path.relative_to(root).as_posix(), "symlink": os.readlink(path)})
                directories.remove(name)
        for name in files:
            path = directory_path / name
            entry = {"path": path.relative_to(root).as_posix()}
            if path.is_symlink():
                entry["symlink"] = os.readlink(path)
            else:
                entry["size"] = path.stat().st_size
                entry["sha256"] = digest_file(path)
            entries.append(entry)
    return sorted(entries, key=lambda item: item["path"])


def aggregate(entries):
    normalized = sorted(entries, key=lambda item: item["path"])
    data = json.dumps(normalized, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(data).hexdigest()


roots = {}
for name, previous in before["roots"].items():
    current = snapshot(project.parent / name)
    old = {entry["path"]: entry for entry in previous}
    new = {entry["path"]: entry for entry in current}
    added = sorted(new.keys() - old.keys())
    removed = sorted(old.keys() - new.keys())
    changed = sorted(key for key in old.keys() & new.keys() if old[key] != new[key])
    roots[name] = {
        "status": "PASS" if not added and not removed and not changed else "FAIL",
        "entriesBefore": len(previous),
        "entriesAfter": len(current),
        "regularFiles": sum("sha256" in entry for entry in current),
        "symlinks": sum("symlink" in entry for entry in current),
        "bytesHashed": sum(entry.get("size", 0) for entry in current),
        "beforeAggregateSha256": aggregate(previous),
        "afterAggregateSha256": aggregate(current),
        "added": added,
        "removed": removed,
        "changed": changed,
    }

result = {
    "status": "PASS" if all(root["status"] == "PASS" for root in roots.values()) else "FAIL",
    "baselineObservedAt": before["observedAt"],
    "comparisonObservedAt": datetime.now(timezone.utc).isoformat(),
    "scope": before["scope"],
    "roots": roots,
    "limitations": [
        "The initial inventory was taken before installed-host tests, not before the entire task.",
        "These are the available Mule evaluation and new Runtime Foundation siblings; no previous Studio/native Runtime source was present in the initial top-level inventory.",
        "This checks regular-file bytes, added/removed paths and symlink targets. It does not compare filesystem timestamps, permissions or external installations.",
    ],
}
output_path = project / "evidence/sibling-isolation-results.json"
output_path.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
print(json.dumps(result, indent=2))
raise SystemExit(0 if result["status"] == "PASS" else 1)
