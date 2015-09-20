package util.Synchronize;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import bean.inter.PooledObject;
import bean.inter.factory.KeyedPooledObjectFactory;

public class SynchronizedKeyedPooledObjectFactory<K, V> implements KeyedPooledObjectFactory<K, V> {
    private final WriteLock writeLock = new ReentrantReadWriteLock().writeLock();

    private final KeyedPooledObjectFactory<K, V> keyedFactory;

    public SynchronizedKeyedPooledObjectFactory(final KeyedPooledObjectFactory<K, V> keyedFactory) throws IllegalArgumentException {
        if (keyedFactory == null) {
            throw new IllegalArgumentException("keyedFactory must not be null.");
        }
        this.keyedFactory = keyedFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PooledObject<V> makeObject(final K key) throws Exception {
        writeLock.lock();
        try {
            return keyedFactory.makeObject(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroyObject(final K key, final PooledObject<V> p) throws Exception {
        writeLock.lock();
        try {
            keyedFactory.destroyObject(key, p);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateObject(final K key, final PooledObject<V> p) {
        writeLock.lock();
        try {
            return keyedFactory.validateObject(key, p);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateObject(final K key, final PooledObject<V> p) throws Exception {
        writeLock.lock();
        try {
            keyedFactory.activateObject(key, p);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void passivateObject(final K key, final PooledObject<V> p) throws Exception {
        writeLock.lock();
        try {
            keyedFactory.passivateObject(key, p);
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
        sb.append("SynchronizedKeyedPoolableObjectFactory");
        sb.append("{keyedFactory=").append(keyedFactory);
        sb.append('}');
        return sb.toString();
    }
}
