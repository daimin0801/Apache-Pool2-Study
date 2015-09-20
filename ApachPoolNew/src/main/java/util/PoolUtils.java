package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import util.Synchronize.SynchronizedKeyedObjectPool;
import util.Synchronize.SynchronizedKeyedPooledObjectFactory;
import util.Synchronize.SynchronizedObjectPool;
import util.Synchronize.SynchronizedPooledObjectFactory;
import util.erode.ErodingKeyedObjectPool;
import util.erode.ErodingObjectPool;
import util.erode.ErodingPerKeyKeyedObjectPool;
import util.task.KeyedObjectPoolMinIdleTimerTask;
import util.task.ObjectPoolMinIdleTimerTask;
import bean.inter.factory.KeyedPooledObjectFactory;
import bean.inter.factory.PooledObjectFactory;
import bean.inter.pool.KeyedObjectPool;
import bean.inter.pool.ObjectPool;

public final class PoolUtils {

    /**
     * 定时器用来周期性的检查池中空闲的对象个数，
     */
    static class TimerHolder {
        static final Timer MIN_IDLE_TIMER = new Timer(true);
    }

    private static Timer getMinIdleTimer() {
        return TimerHolder.MIN_IDLE_TIMER;
    }

    public PoolUtils() {
    }

    /**
     * 
     * 不能接受ThreadDeath，VirtualMachineError的异常
     */
    public static void checkRethrow(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
    }

    public static <T> TimerTask checkMinIdle(final ObjectPool<T> pool, final int minIdle, final long period) throws IllegalArgumentException {
        if (pool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
        }
        if (minIdle < 0) {
            throw new IllegalArgumentException("minIdle must be non-negative.");
        }
        final TimerTask task = new ObjectPoolMinIdleTimerTask<T>(pool, minIdle);
        getMinIdleTimer().schedule(task, 0L, period);
        return task;
    }

    public static <K, V> TimerTask checkMinIdle(final KeyedObjectPool<K, V> keyedPool, final K key, final int minIdle, final long period)
            throws IllegalArgumentException {
        if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null.");
        }
        if (minIdle < 0) {
            throw new IllegalArgumentException("minIdle must be non-negative.");
        }
        final TimerTask task = new KeyedObjectPoolMinIdleTimerTask<K, V>(keyedPool, key, minIdle);
        getMinIdleTimer().schedule(task, 0L, period);
        return task;
    }

    public static <K, V> Map<K, TimerTask> checkMinIdle(final KeyedObjectPool<K, V> keyedPool, final Collection<K> keys, final int minIdle, final long period)
            throws IllegalArgumentException {
        if (keys == null) {
            throw new IllegalArgumentException("keys must not be null.");
        }
        final Map<K, TimerTask> tasks = new HashMap<K, TimerTask>(keys.size());
        final Iterator<K> iter = keys.iterator();
        while (iter.hasNext()) {
            final K key = iter.next();
            final TimerTask task = checkMinIdle(keyedPool, key, minIdle, period);
            tasks.put(key, task);
        }
        return tasks;
    }

    public static <T> void prefill(final ObjectPool<T> pool, final int count) throws Exception, IllegalArgumentException {
        if (pool == null) {
            throw new IllegalArgumentException("pool must not be null.");
        }
        for (int i = 0; i < count; i++) {
            pool.addObject();
        }
    }

    public static <K, V> void prefill(final KeyedObjectPool<K, V> keyedPool, final K key, final int count) throws Exception, IllegalArgumentException {
        if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null.");
        }
        for (int i = 0; i < count; i++) {
            keyedPool.addObject(key);
        }
    }

    public static <K, V> void prefill(final KeyedObjectPool<K, V> keyedPool, final Collection<K> keys, final int count) throws Exception,
            IllegalArgumentException {
        if (keys == null) {
            throw new IllegalArgumentException("keys must not be null.");
        }
        final Iterator<K> iter = keys.iterator();
        while (iter.hasNext()) {
            prefill(keyedPool, iter.next(), count);
        }
    }

    public static <T> ObjectPool<T> synchronizedPool(final ObjectPool<T> pool) {
        if (pool == null) {
            throw new IllegalArgumentException("pool must not be null.");
        }
        /*
         * assert !(pool instanceof GenericObjectPool) : "GenericObjectPool is already thread-safe"; assert !(pool instanceof SoftReferenceObjectPool) :
         * "SoftReferenceObjectPool is already thread-safe"; assert !(pool instanceof StackObjectPool) : "StackObjectPool is already thread-safe"; assert
         * !"org.apache.commons.pool.composite.CompositeObjectPool" .equals(pool.getClass().getName()) : "CompositeObjectPools are already thread-safe";
         */
        return new SynchronizedObjectPool<T>(pool);
    }

    public static <K, V> KeyedObjectPool<K, V> synchronizedPool(final KeyedObjectPool<K, V> keyedPool) {
        /*
         * assert !(keyedPool instanceof GenericKeyedObjectPool) : "GenericKeyedObjectPool is already thread-safe"; assert !(keyedPool instanceof
         * StackKeyedObjectPool) : "StackKeyedObjectPool is already thread-safe"; assert !"org.apache.commons.pool.composite.CompositeKeyedObjectPool"
         * .equals(keyedPool.getClass().getName()) : "CompositeKeyedObjectPools are already thread-safe";
         */
        return new SynchronizedKeyedObjectPool<K, V>(keyedPool);
    }

    public static <T> PooledObjectFactory<T> synchronizedPooledFactory(final PooledObjectFactory<T> factory) {
        return new SynchronizedPooledObjectFactory<T>(factory);
    }

    public static <K, V> KeyedPooledObjectFactory<K, V> synchronizedKeyedPooledFactory(final KeyedPooledObjectFactory<K, V> keyedFactory) {
        return new SynchronizedKeyedPooledObjectFactory<K, V>(keyedFactory);
    }

    public static <T> ObjectPool<T> erodingPool(final ObjectPool<T> pool) {
        return erodingPool(pool, 1f);
    }

    public static <T> ObjectPool<T> erodingPool(final ObjectPool<T> pool, final float factor) {
        if (pool == null) {
            throw new IllegalArgumentException("pool must not be null.");
        }
        if (factor <= 0f) {
            throw new IllegalArgumentException("factor must be positive.");
        }
        return new ErodingObjectPool<T>(pool, factor);
    }

    public static <K, V> KeyedObjectPool<K, V> erodingPool(final KeyedObjectPool<K, V> keyedPool) {
        return erodingPool(keyedPool, 1f);
    }

    public static <K, V> KeyedObjectPool<K, V> erodingPool(final KeyedObjectPool<K, V> keyedPool, final float factor) {
        return erodingPool(keyedPool, factor, false);
    }

    public static <K, V> KeyedObjectPool<K, V> erodingPool(final KeyedObjectPool<K, V> keyedPool, final float factor, final boolean perKey) {
        if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
        }
        if (factor <= 0f) {
            throw new IllegalArgumentException("factor must be positive.");
        }
        if (perKey) {
            return new ErodingPerKeyKeyedObjectPool<K, V>(keyedPool, factor);
        }
        return new ErodingKeyedObjectPool<K, V>(keyedPool, factor);
    }

}