import json
from pathlib import Path

TRANSCRIPT = Path(
    r"C:\Users\Sergei\.cursor\projects\g-Alphadcp\agent-transcripts\c1d1d68a-67f1-4523-aba6-98bb6aa113cd\c1d1d68a-67f1-4523-aba6-98bb6aa113cd.jsonl"
)
OUT_DIR = Path(r"G:\Alphadcp\_restore\2201")
OUT_DIR.mkdir(parents=True, exist_ok=True)

# Stop right before the "И опять фоткает" message (22:09 fixes).
STOP_MARKERS = (
    "И опять фоткает",
    "Оу ноу это ужас",
    "переноси все полностью",
)

TARGET_SUFFIXES = (
    "zov/crickclient/module/list/combat/KillAura.java",
    "zov/crickclient/util/player/combat/SpookyTimeAuraUtil.java",
    "zov/crickclient/util/player/combat/FunTimeAuraUtil.java",
    "zov/crickclient/util/player/combat/IdealHitUtils.java",
    "zov/crickclient/mixin/ClientPlayerEntityMixin.java",
    "zov/crickclient/mixin/MinecraftClientMixin.java",
    "zov/crickclient/mixin/KeyboardInputMixin.java",
    "zov/crickclient/module/ModuleSettingDefinitions.java",
    "zov/crickclient/util/rotation/RotationComponent.java",
    "zov/crickclient/CrickClient.java",
    "zov/crickclient/event/list/EventMotion.java",
    "zov/crickclient/event/list/EventFrame.java",
    "test/java/zov/crickclient/util/player/combat/SpookyTimeAuraUtilTest.java",
)

files: dict[str, str] = {}
applied_writes = 0
applied_replaces = 0
skipped_replaces = 0


def normalize_path(raw: str) -> str | None:
    p = raw.replace("\\", "/")
    if "Alphadcp/" not in p and "Alphadcp\\" not in raw:
        return None
    idx = p.lower().find("alphadcp/")
    rel = p[idx + len("alphadcp/") :]
    for suffix in TARGET_SUFFIXES:
        if rel.endswith(suffix) or rel.replace("\\", "/").endswith(suffix):
            return suffix
    return None


def should_track(rel: str | None) -> bool:
    return rel is not None


with TRANSCRIPT.open("r", encoding="utf-8") as handle:
    for line in handle:
        if any(marker in line for marker in STOP_MARKERS):
            break
        if '"Write"' not in line and '"StrReplace"' not in line and '"Delete"' not in line:
            continue
        try:
            obj = json.loads(line)
        except json.JSONDecodeError:
            continue

        for part in obj.get("message", {}).get("content", []):
            name = part.get("name")
            inp = part.get("input", {})
            path = inp.get("path", "")
            rel = normalize_path(path)
            if not should_track(rel):
                continue

            if name == "Write":
                files[rel] = inp["contents"]
                applied_writes += 1
            elif name == "StrReplace":
                old = inp.get("old_string")
                new = inp.get("new_string")
                if rel not in files:
                    skipped_replaces += 1
                    continue
                if old not in files[rel]:
                    skipped_replaces += 1
                    continue
                files[rel] = files[rel].replace(old, new, 1)
                applied_replaces += 1
            elif name == "Delete":
                files.pop(rel, None)

for rel, contents in files.items():
    out = OUT_DIR / rel.replace("/", "\\")
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(contents, encoding="utf-8")
    print(f"saved {rel} ({len(contents)} chars)")

print()
print(f"writes={applied_writes} replaces={applied_replaces} skipped_replaces={skipped_replaces}")
for suffix in TARGET_SUFFIXES:
    if suffix not in files:
        print(f"missing {suffix}")
