package util.Synchronize;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import bean.inter.PooledObject;
import bean.inter.factory.PooledObjectFactory;

public class SynchronizedPooledObjectFactory<T> implements PooledObjectFactory<T> {
    private final WriteLock writeLock = new ReentrantReadWriteLock().writeLock();

    private final PooledObjectFactory<T> factory;

    public SynchronizedPooledObjectFactory(final PooledObjectFactory<T> factory) throws IllegalArgumentException {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null.");
        }
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PooledObject<T> makeObject() throws Exception {
        writeLock.lock();
        try {
            return factory.makeObject();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroyObject(final PooledObject<T> p) throws Exception {
        writeLock.lock();
        try {
            factory.destroyObject(p);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateObject(final PooledObject<T> p) {
        writeLock.lock();
        try {
            return factory.validateObject(p);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateObject(final PooledObject<T> p) throws Exception {
        writeLock.lock();
        try {
            factory.activateObject(p);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void passivateObject(final PooledObject<T> p) throws Exception {
        writeLock.lock();
        try {
            factory.passivateObject(p);
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
        sb.append("SynchronizedPoolableObjectFactory");
        sb.append("{factory=").append(factory);
        sb.append('}');
        return sb.toString();
    }
}