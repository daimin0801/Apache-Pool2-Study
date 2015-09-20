package util.Synchronize;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import bean.inter.pool.ObjectPool;

public class SynchronizedObjectPool<T> implements ObjectPool<T> {

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final ObjectPool<T> pool;

    public SynchronizedObjectPool(final ObjectPool<T> pool) throws IllegalArgumentException {
        if (pool == null) {
            throw new IllegalArgumentException("pool must not be null.");
        }
        this.pool = pool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            return pool.borrowObject();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void returnObject(final T obj) {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            pool.returnObject(obj);
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
    public void invalidateObject(final T obj) {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            pool.invalidateObject(obj);
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
    public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            pool.addObject();
        } finally {
            writeLock.unlock();
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
            return pool.getNumIdle();
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
            return pool.getNumActive();
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
            pool.clear();
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
            pool.close();
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
        sb.append("SynchronizedObjectPool");
        sb.append("{pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
