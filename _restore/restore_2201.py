import json
import shutil
from pathlib import Path

TRANSCRIPT = Path(
    r"C:\Users\Sergei\.cursor\projects\g-Alphadcp\agent-transcripts\c1d1d68a-67f1-4523-aba6-98bb6aa113cd\c1d1d68a-67f1-4523-aba6-98bb6aa113cd.jsonl"
)
SEED_ROOT = Path(r"G:\crickclient\src")
PROJECT_ROOT = Path(r"G:\Alphadcp")
OUT_DIR = PROJECT_ROOT / "_restore" / "2201"
OUT_DIR.mkdir(parents=True, exist_ok=True)

STOP_MARKERS = (
    "И опять фоткает",
    "Оу ноу это ужас",
    "переноси все полностью",
)

FILES = {
    "zov/crickclient/module/list/combat/KillAura.java": "main/java/zov/crickclient/module/list/combat/KillAura.java",
    "zov/crickclient/util/player/combat/FunTimeAuraUtil.java": "main/java/zov/crickclient/util/player/combat/FunTimeAuraUtil.java",
    "zov/crickclient/util/player/combat/SpookyTimeAuraUtil.java": "main/java/zov/crickclient/util/player/combat/SpookyTimeAuraUtil.java",
    "zov/crickclient/util/player/combat/IdealHitUtils.java": "main/java/zov/crickclient/util/player/combat/IdealHitUtils.java",
    "zov/crickclient/mixin/ClientPlayerEntityMixin.java": "main/java/zov/crickclient/mixin/ClientPlayerEntityMixin.java",
    "zov/crickclient/mixin/MinecraftClientMixin.java": "main/java/zov/crickclient/mixin/MinecraftClientMixin.java",
    "zov/crickclient/mixin/KeyboardInputMixin.java": "main/java/zov/crickclient/mixin/KeyboardInputMixin.java",
    "zov/crickclient/module/ModuleSettingDefinitions.java": "main/java/zov/crickclient/module/ModuleSettingDefinitions.java",
    "zov/crickclient/util/rotation/RotationComponent.java": "main/java/zov/crickclient/util/rotation/RotationComponent.java",
    "zov/crickclient/CrickClient.java": "main/java/zov/crickclient/CrickClient.java",
    "test/java/zov/crickclient/util/player/combat/SpookyTimeAuraUtilTest.java": "test/java/zov/crickclient/util/player/combat/SpookyTimeAuraUtilTest.java",
}


def normalize_path(raw: str) -> str | None:
    p = raw.replace("\\", "/")
    for key in FILES:
        if key in p:
            return key
    return None


files: dict[str, str] = {}
for key, rel in FILES.items():
    seed = SEED_ROOT / rel
    if seed.exists():
        files[key] = seed.read_text(encoding="utf-8")
        print(f"seed {key} ({len(files[key])} chars)")

applied_writes = 0
applied_replaces = 0
skipped_replaces = 0

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
            rel = normalize_path(inp.get("path", ""))
            if rel is None:
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

for key, contents in files.items():
    out = OUT_DIR / key
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(contents, encoding="utf-8")
    print(f"saved {key} ({len(contents)} chars)")

print()
print(f"writes={applied_writes} replaces={applied_replaces} skipped_replaces={skipped_replaces}")
for key in FILES:
    if key not in files:
        print(f"missing {key}")

# Deploy into project
deploy_map = {
    "zov/crickclient/module/list/combat/KillAura.java": PROJECT_ROOT / "src/main/java/zov/crickclient/module/list/combat/KillAura.java",
    "zov/crickclient/util/player/combat/FunTimeAuraUtil.java": PROJECT_ROOT / "src/main/java/zov/crickclient/util/player/combat/FunTimeAuraUtil.java",
    "zov/crickclient/util/player/combat/SpookyTimeAuraUtil.java": PROJECT_ROOT / "src/main/java/zov/crickclient/util/player/combat/SpookyTimeAuraUtil.java",
    "zov/crickclient/util/player/combat/IdealHitUtils.java": PROJECT_ROOT / "src/main/java/zov/crickclient/util/player/combat/IdealHitUtils.java",
    "zov/crickclient/mixin/ClientPlayerEntityMixin.java": PROJECT_ROOT / "src/main/java/zov/crickclient/mixin/ClientPlayerEntityMixin.java",
    "zov/crickclient/mixin/MinecraftClientMixin.java": PROJECT_ROOT / "src/main/java/zov/crickclient/mixin/MinecraftClientMixin.java",
    "zov/crickclient/mixin/KeyboardInputMixin.java": PROJECT_ROOT / "src/main/java/zov/crickclient/mixin/KeyboardInputMixin.java",
    "zov/crickclient/module/ModuleSettingDefinitions.java": PROJECT_ROOT / "src/main/java/zov/crickclient/module/ModuleSettingDefinitions.java",
    "zov/crickclient/util/rotation/RotationComponent.java": PROJECT_ROOT / "src/main/java/zov/crickclient/util/rotation/RotationComponent.java",
    "zov/crickclient/CrickClient.java": PROJECT_ROOT / "src/main/java/zov/crickclient/CrickClient.java",
    "test/java/zov/crickclient/util/player/combat/SpookyTimeAuraUtilTest.java": PROJECT_ROOT / "src/test/java/zov/crickclient/util/player/combat/SpookyTimeAuraUtilTest.java",
}

for key, target in deploy_map.items():
    if key in files:
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(files[key], encoding="utf-8")
        print(f"deployed {target.name}")

# Remove incorrect full Minced port artifacts
remove_paths = [
    PROJECT_ROOT / "src/main/java/zov/crickclient/util/player/combat/minced",
    PROJECT_ROOT / "src/main/java/zov/crickclient/integration",
    PROJECT_ROOT / "src/main/java/minced",
    PROJECT_ROOT / "src/main/java/sLM",
    PROJECT_ROOT / "src/main/java/sg",
    PROJECT_ROOT / "src/main/java/zov/crickclient/event/list/EventMotion.java",
    PROJECT_ROOT / "src/main/resources/minced.mixins.json",
]
for path in remove_paths:
    if path.is_dir():
        shutil.rmtree(path, ignore_errors=True)
        print(f"removed dir {path}")
    elif path.is_file():
        path.unlink(missing_ok=True)
        print(f"removed file {path}")

# Restore fabric.mod.json minced mixins entry if present
fabric = PROJECT_ROOT / "src/main/resources/fabric.mod.json"
if fabric.exists():
    text = fabric.read_text(encoding="utf-8")
    if "minced.mixins.json" in text:
        text = text.replace(',\n\t\t"minced.mixins.json"', "")
        text = text.replace('"minced.mixins.json",\n\t\t', "")
        fabric.write_text(text, encoding="utf-8")
        print("cleaned fabric.mod.json")
