package util.Synchronize;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import bean.inter.pool.KeyedObjectPool;

public class SynchronizedKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final KeyedObjectPool<K, V> keyedPool;

    public SynchronizedKeyedObjectPool(final KeyedObjectPool<K, V> keyedPool) throws IllegalArgumentException {
        if (keyedPool == null) {
            throw new IllegalArgumentException("keyedPool must not be null.");
        }
        this.keyedPool = keyedPool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V borrowObject(final K key) throws Exception, NoSuchElementException, IllegalStateException {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            return keyedPool.borrowObject(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void returnObject(final K key, final V obj) {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            keyedPool.returnObject(key, obj);
        } catch (Exception e) {
            // swallowed
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateObject(final K key, final V obj) {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            keyedPool.invalidateObject(key, obj);
        } catch (Exception e) {
            // swallowed as of Pool 2
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObject(final K key) throws Exception, IllegalStateException, UnsupportedOperationException {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            keyedPool.addObject(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumIdle(final K key) {
        ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return keyedPool.getNumIdle(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumActive(final K key) {
        ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return keyedPool.getNumActive(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumIdle() {
        ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return keyedPool.getNumIdle();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumActive() {
        ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return keyedPool.getNumActive();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            keyedPool.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final K key) throws Exception, UnsupportedOperationException {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            keyedPool.clear(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            keyedPool.close();
        } catch (Exception e) {
            // swallowed as of Pool 2
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SynchronizedKeyedObjectPool");
        sb.append("{keyedPool=").append(keyedPool);
        sb.append('}');
        return sb.toString();
    }
}
