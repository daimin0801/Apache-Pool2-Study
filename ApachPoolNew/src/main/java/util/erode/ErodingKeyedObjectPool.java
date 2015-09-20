package util.erode;

import java.util.NoSuchElementException;

import bean.inter.pool.KeyedObjectPool;

public class ErodingKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
    private final KeyedObjectPool<K, V> keyedPool;

    private final ErodingFactor erodingFactor;

    public ErodingKeyedObjectPool(final KeyedObjectPool<K, V> keyedPool, final float factor) {
        this(keyedPool, new ErodingFactor(factor));
    }
    protected ErodingKeyedObjectPool(final KeyedObjectPool<K, V> keyedPool, final ErodingFactor erodingFactor) {
        if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
        }
        this.keyedPool = keyedPool;
        this.erodingFactor = erodingFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V borrowObject(final K key) throws Exception, NoSuchElementException, IllegalStateException {
        return keyedPool.borrowObject(key);
    }

    @Override
    public void returnObject(final K key, final V obj) throws Exception {
        boolean discard = false;
        final long now = System.currentTimeMillis();
        final ErodingFactor factor = getErodingFactor(key);
        synchronized (keyedPool) {
            if (factor.getNextShrink() < now) {
                final int numIdle = getNumIdle(key);
                if (numIdle > 0) {
                    discard = true;
                }

                factor.update(now, numIdle);
            }
        }
        try {
            if (discard) {
                keyedPool.invalidateObject(key, obj);
            } else {
                keyedPool.returnObject(key, obj);
            }
        } catch (Exception e) {
            // swallowed
        }
    }

    /**
     * Returns the eroding factor for the given key
     * 
     * @param key
     *            key
     * @return eroding factor for the given keyed pool
     */
    protected ErodingFactor getErodingFactor(final K key) {
        return erodingFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateObject(final K key, final V obj) {
        try {
            keyedPool.invalidateObject(key, obj);
        } catch (Exception e) {
            // swallowed
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObject(final K key) throws Exception, IllegalStateException, UnsupportedOperationException {
        keyedPool.addObject(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumIdle() {
        return keyedPool.getNumIdle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumIdle(final K key) {
        return keyedPool.getNumIdle(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumActive() {
        return keyedPool.getNumActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumActive(final K key) {
        return keyedPool.getNumActive(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        keyedPool.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final K key) throws Exception, UnsupportedOperationException {
        keyedPool.clear(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            keyedPool.close();
        } catch (Exception e) {
            // swallowed
        }
    }

    /**
     * Returns the underlying pool
     * 
     * @return the keyed pool that this ErodingKeyedObjectPool wraps
     */
    protected KeyedObjectPool<K, V> getKeyedPool() {
        return keyedPool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ErodingKeyedObjectPool{" + "factor=" + erodingFactor + ", keyedPool=" + keyedPool + '}';
    }
}