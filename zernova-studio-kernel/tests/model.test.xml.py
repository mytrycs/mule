"""Optional independent XML parser check against retained serializer fixtures.

Run: python3 tests/model.test.xml.py evidence/model/xml-fixtures.json
This tests presentation output only; it does not build or run an application.
"""
import json
import pathlib
import sys
import xml.etree.ElementTree as ET

fixture_path = pathlib.Path(sys.argv[1])
cases = json.loads(fixture_path.read_text(encoding="utf-8"))
namespaces = {
    "core": "https://schemas.zernova.dev/core",
    "http": "https://schemas.zernova.dev/http",
}
checks = []
for case in cases:
    document = ET.fromstring(case["xml"])
    flow = document.find("core:flow", namespaces)
    assert flow is not None
    assert flow.attrib["name"] == case["flowName"]
    logger = flow.find("core:logger", namespaces)
    listener = flow.find("http:listener", namespaces)
    if case.get("message") is not None:
        assert logger is not None and logger.attrib["message"] == case["message"]
    else:
        assert logger is None
    if case.get("path") is not None:
        assert listener is not None and listener.attrib["path"] == case["path"]
    else:
        assert listener is None
    assert not document.findall(".//evil")
    checks.append({"case": case["index"], "wellFormed": True, "roundTripAttributes": True, "noInjectedElements": True})
print(json.dumps({"status": "PASS", "parser": "Python stdlib xml.etree.ElementTree", "cases": checks}, indent=2))
