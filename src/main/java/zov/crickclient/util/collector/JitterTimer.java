package zov.crickclient.util.collector;

public class JitterTimer {
    private long lastMs;
    private long jitterSeed;

    public JitterTimer() {
        reset();
    }

    public boolean elapsed(long delayMs) {
        return System.currentTimeMillis() - delayMs >= lastMs;
    }

    public boolean elapsed(long delayMs, long jitterMs) {
        return System.currentTimeMillis() - (delayMs + (jitterSeed % (jitterMs + 1))) >= lastMs;
    }

    public void reset() {
        lastMs = System.currentTimeMillis();
        jitterSeed = (long) (Math.random() * Long.MAX_VALUE);
    }
}
