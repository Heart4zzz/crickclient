package zov.crickclient.util.player.combat.zenith.model;

public class ActivationLayer {
    public final float[] activationLayerAmountArray;
    public final Long activationLayerLongValue;
    public final Integer activationLayerIntegerValue;
    public final Long activationLayerLongValueSecondary;
    public final Float activationLayerFloatValue;
    public final Float activationLayerFloatValueSecondary;
    public final int activationLayerValue;

    public ActivationLayer(float[] features, Long sessionId, Integer playerAge, Long timestampMs,
                           Float targetYawStep, Float targetPitchStep, int lineNumber) {
        this.activationLayerAmountArray = features;
        this.activationLayerLongValue = sessionId;
        this.activationLayerIntegerValue = playerAge;
        this.activationLayerLongValueSecondary = timestampMs;
        this.activationLayerFloatValue = targetYawStep;
        this.activationLayerFloatValueSecondary = targetPitchStep;
        this.activationLayerValue = lineNumber;
    }
}
