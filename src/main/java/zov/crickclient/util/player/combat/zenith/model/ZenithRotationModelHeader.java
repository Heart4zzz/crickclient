package zov.crickclient.util.player.combat.zenith.model;

public final class ZenithRotationModelHeader {
    public final float rotationModelHeaderAmount;
    public final float[] rotationModelHeaderAmountArray;
    public final int rotationModelHeaderValue;
    public final boolean rotationModelHeaderEnabled;

    public ZenithRotationModelHeader(float amount, float[] weights, int expertIndex, boolean valid) {
        this.rotationModelHeaderAmount = amount;
        this.rotationModelHeaderAmountArray = weights;
        this.rotationModelHeaderValue = expertIndex;
        this.rotationModelHeaderEnabled = valid;
    }

    public static ZenithRotationModelHeader method00392(int expertCount) {
        return new ZenithRotationModelHeader(0.0F, new float[Math.max(0, expertCount)], -1, false);
    }
}
