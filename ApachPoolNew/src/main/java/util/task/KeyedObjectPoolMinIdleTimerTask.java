package util.task;

import java.util.TimerTask;

import bean.inter.pool.KeyedObjectPool;

public class KeyedObjectPoolMinIdleTimerTask<K, V> extends TimerTask {
    private final int minIdle;

    private final K key;

    private final KeyedObjectPool<K, V> keyedPool;

    public KeyedObjectPoolMinIdleTimerTask(final KeyedObjectPool<K, V> keyedPool, final K key, final int minIdle) throws IllegalArgumentException {
        if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
        }
        this.keyedPool = keyedPool;
        this.key = key;
        this.minIdle = minIdle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        boolean success = false;
        try {
            if (keyedPool.getNumIdle(key) < minIdle) {
                keyedPool.addObject(key);
            }
            success = true;

        } catch (Exception e) {
            cancel();

        } finally {
            // detect other types of Throwable and cancel this Timer
            if (!success) {
                cancel();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("KeyedObjectPoolMinIdleTimerTask");
        sb.append("{minIdle=").append(minIdle);
        sb.append(", key=").append(key);
        sb.append(", keyedPool=").append(keyedPool);
        sb.append('}');
        return sb.toString();
    }
}
