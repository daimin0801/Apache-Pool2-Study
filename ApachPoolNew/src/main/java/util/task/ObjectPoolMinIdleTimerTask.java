package util.task;

import java.util.TimerTask;

import bean.inter.pool.ObjectPool;

/**
 * 定时任务，用来检查池中的空闲的对象数量，如果少于配置的最小个数会调用增加方法 
 */
public class ObjectPoolMinIdleTimerTask<T> extends TimerTask {

    private final int minIdle;

    private final ObjectPool<T> pool;

    public ObjectPoolMinIdleTimerTask(final ObjectPool<T> pool, final int minIdle) throws IllegalArgumentException {
        if (pool == null) {
            throw new IllegalArgumentException("pool must not be null.");
        }
        this.pool = pool;
        this.minIdle = minIdle;
    }

    @Override
    public void run() {
        boolean success = false;
        try {
            if (pool.getNumIdle() < minIdle) {
                pool.addObject();
            }
            success = true;

        } catch (Exception e) {
            cancel();
        } finally {
            if (!success) {
                cancel();
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ObjectPoolMinIdleTimerTask");
        sb.append("{minIdle=").append(minIdle);
        sb.append(", pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
