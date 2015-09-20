package util.erode;

/**
 * Encapsulate the logic for when the next poolable object should be discarded. Each time update is called, the next time to shrink is recomputed, based on the
 * float factor, number of idle instances in the pool and high water mark. Float factor is assumed to be between 0 and 1. Values closer to 1 cause less frequent
 * erosion events. Erosion event timing also depends on numIdle. When this value is relatively high (close to previously established high water mark), erosion
 * occurs more frequently.
 */
public class ErodingFactor {
    /** Determines frequency of "erosion" events */
    private final float factor;

    /** Time of next shrink event */
    private transient volatile long nextShrink;

    /** High water mark - largest numIdle encountered */
    private transient volatile int idleHighWaterMark;

    /**
     * Create a new ErodingFactor with the given erosion factor.
     * 
     * @param factor
     *            erosion factor
     */
    public ErodingFactor(final float factor) {
        this.factor = factor;
        nextShrink = System.currentTimeMillis() + (long) (900000 * factor); // now
                                                                            // +
                                                                            // 15
                                                                            // min
                                                                            // *
                                                                            // factor
        idleHighWaterMark = 1;
    }

    /**
     * Updates internal state using the supplied time and numIdle.
     * 
     * @param now
     *            current time
     * @param numIdle
     *            number of idle elements in the pool
     */
    public void update(final long now, final int numIdle) {
        final int idle = Math.max(0, numIdle);
        idleHighWaterMark = Math.max(idle, idleHighWaterMark);
        final float maxInterval = 15f;
        final float minutes = maxInterval + ((1f - maxInterval) / idleHighWaterMark) * idle;
        nextShrink = now + (long) (minutes * 60000f * factor);
    }

    /**
     * Returns the time of the next erosion event.
     * 
     * @return next shrink time
     */
    public long getNextShrink() {
        return nextShrink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ErodingFactor{" + "factor=" + factor + ", idleHighWaterMark=" + idleHighWaterMark + '}';
    }
}
