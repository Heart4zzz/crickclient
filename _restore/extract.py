import json
from pathlib import Path

path = Path(r"C:\Users\Sergei\.cursor\projects\g-Alphadcp\agent-transcripts\c1d1d68a-67f1-4523-aba6-98bb6aa113cd\c1d1d68a-67f1-4523-aba6-98bb6aa113cd.jsonl")
out_dir = Path(r"G:\Alphadcp\_restore")
out_dir.mkdir(exist_ok=True)

targets = [
    "KillAura.java",
    "SpookyTestMode.java",
    "MincedAuraModes.java",
    "MincedWeaponUtil.java",
    "MincedAuraDistanceUtil.java",
    "MincedRotationTarget.java",
    "MincedAimRotation.java",
    "MincedEntityPoseUtil.java",
    "LegitMode.java",
    "MatrixMode.java",
    "ReallyWorldMode.java",
    "MincedAuraMode.java",
    "EventFrame.java",
    "ModuleSettingDefinitions.java",
]

saved = {}

with path.open("r", encoding="utf-8") as f:
    for line in f:
        if "переноси все полностью" in line:
            break
        if '"Write"' not in line:
            continue
        try:
            obj = json.loads(line)
        except json.JSONDecodeError:
            continue
        for part in obj.get("message", {}).get("content", []):
            inp = part.get("input", {})
            if part.get("name") != "Write":
                continue
            p = inp.get("path", "")
            for target in targets:
                if target in p:
                    saved[target] = inp["contents"]

for target, contents in saved.items():
    (out_dir / target).write_text(contents, encoding="utf-8")
    print("saved", target, len(contents))

for target in targets:
    if target not in saved:
        print("missing", target)
