import pathlib

src = pathlib.Path(r"G:\zenith3.0final\src\main\java\zenith\particle\RotationModelManager.java")
dst = pathlib.Path(r"G:\Alphadcp\src\main\java\zov\crickclient\util\player\combat\zenith\model\ZenithRotationModelManager.java")
text = src.read_text(encoding="utf-8")

text = text.replace("package zenith.particle;", "package zov.crickclient.util.player.combat.zenith.model;")
text = text.replace("import zenith.render.ColorWidgetCore;\n", "")
text = text.replace("import zenith.render.ZenithClient;\n", "")
text = text.replace("public class RotationModelManager", "public final class ZenithRotationModelManager")
text = text.replace("RotationModelManager", "ZenithRotationModelManager")
text = text.replace("RotationModel ", "ZenithRotationModel ")
text = text.replace("RotationModel.", "ZenithRotationModel.")
text = text.replace("RotationPrediction", "ZenithRotationPrediction")
text = text.replace("RotationModelHeader", "ZenithRotationModelHeader")
text = text.replace(
    "/assets/zenith/models/particles/6d9e3f4a.dat",
    "/assets/crickclient/models/particles/6d9e3f4a.dat",
)
text = text.replace(
    "/assets/zenith/models/rotation_model_v2.json",
    "/assets/crickclient/models/rotation_model_v2.json",
)
text = text.replace(
    'return ZenithClient.method01181().method00886().getUsername().equals("port by lokets547");',
    "return false;",
)
text = text.replace("ColorWidgetCore.method01076(s);", "// debug chat disabled")

singleton = """
    private static final ZenithRotationModelManager INSTANCE = new ZenithRotationModelManager();

    public static ZenithRotationModelManager getInstance() {
        return INSTANCE;
    }

"""
text = text.replace(
    "public final class ZenithRotationModelManager {",
    "public final class ZenithRotationModelManager {" + singleton,
)

alias = """
    public void loadModel() { method00800(); }
    public boolean isLoaded() { return method01297(); }
    public ZenithRotationModel getModel() { return method01027(); }
    public int getSequenceLength() { return method00344(); }
    public int getInputSize() { return method00395(); }
    public void resetBuffer() { method00423(); }
    public ZenithRotationPrediction predict(float[] features) { return method00811(features); }
    public float[] predictArray(float[] features) { return method00280(features); }
"""

if text.rstrip().endswith("}"):
    text = text.rstrip()[:-1] + alias + "}\n"
else:
    text = text + alias

dst.write_text(text, encoding="utf-8")
print("written", dst)
