package zov.crickclient.util.player.combat.zenith.model;

public final class ZenithRotationPrediction {
    public final float rotationPredictionAmount;
    public final float rotationPredictionAmountSecondary;
    public final float rotationPredictionAmountTertiary;
    public final float rotationPredictionAmountAlternate;
    public final float rotationPredictionAmountPrevious;
    public final float rotationPredictionAmountCurrent;
    public final float[] rotationPredictionAmountArray;
    public final float[] rotationPredictionAmountArraySecondary;
    public final int rotationPredictionValue;
    public final int rotationPredictionValueSecondary;

    public ZenithRotationPrediction(float yawStep, float pitchStep, float yawMoveWeight, float pitchMoveWeight,
                                    float rawYawStep, float rawPitchStep, float[] yawGateWeights, float[] pitchGateWeights,
                                    int yawExpertIndex, int pitchExpertIndex) {
        this.rotationPredictionAmount = yawStep;
        this.rotationPredictionAmountSecondary = pitchStep;
        this.rotationPredictionAmountTertiary = yawMoveWeight;
        this.rotationPredictionAmountAlternate = pitchMoveWeight;
        this.rotationPredictionAmountPrevious = rawYawStep;
        this.rotationPredictionAmountCurrent = rawPitchStep;
        this.rotationPredictionAmountArray = yawGateWeights;
        this.rotationPredictionAmountArraySecondary = pitchGateWeights;
        this.rotationPredictionValue = yawExpertIndex;
        this.rotationPredictionValueSecondary = pitchExpertIndex;
    }

    public float[] method00887() {
        return new float[]{rotationPredictionAmount, rotationPredictionAmountSecondary};
    }

    public float method00742() {
        return rotationPredictionAmount;
    }

    public float method01165() {
        return rotationPredictionAmountSecondary;
    }
}
