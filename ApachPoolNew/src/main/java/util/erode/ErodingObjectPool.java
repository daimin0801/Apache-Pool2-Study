package util.erode;

import java.util.NoSuchElementException;

import bean.inter.pool.ObjectPool;

/**
 * 逐步减少它的大小空闲时不再需要的对象
 */
public  class ErodingObjectPool<T> implements ObjectPool<T> {
    private final ObjectPool<T> pool;

    private final ErodingFactor factor;

    /**
     * Create an ErodingObjectPool wrapping the given pool using the specified erosion factor.
     * 
     * @param pool
     *            underlying pool
     * @param factor
     *            erosion factor - determines the frequency of erosion events
     * @see #factor
     */
    public ErodingObjectPool(final ObjectPool<T> pool, final float factor) {
        this.pool = pool;
        this.factor = new ErodingFactor(factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
        return pool.borrowObject();
    }

    /**
     * Returns obj to the pool, unless erosion is triggered, in which case obj is invalidated. Erosion is triggered when there are idle instances in the
     * pool and more than the {@link #factor erosion factor}-determined time has elapsed since the last returnObject activation.
     * 
     * @param obj
     *            object to return or invalidate
     * @see #factor
     */
    @Override
    public void returnObject(final T obj) {
        boolean discard = false;
        final long now = System.currentTimeMillis();
        synchronized (pool) {
            if (factor.getNextShrink() < now) { // XXX: Pool 3: move test
                                                // out of sync block
                final int numIdle = pool.getNumIdle();
                if (numIdle > 0) {
                    discard = true;
                }

                factor.update(now, numIdle);
            }
        }
        try {
            if (discard) {
                pool.invalidateObject(obj);
            } else {
                pool.returnObject(obj);
            }
        } catch (Exception e) {
            // swallowed
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateObject(final T obj) {
        try {
            pool.invalidateObject(obj);
        } catch (Exception e) {
            // swallowed
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
        pool.addObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumIdle() {
        return pool.getNumIdle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumActive() {
        return pool.getNumActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        pool.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            pool.close();
        } catch (Exception e) {
            // swallowed
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ErodingObjectPool{" + "factor=" + factor + ", pool=" + pool + '}';
    }
}