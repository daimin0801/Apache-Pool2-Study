package bean.inter.pool;

public abstract class BaseObjectPool<T> implements ObjectPool<T> {
    private volatile boolean closed = false;

    @Override
    public abstract T borrowObject() throws Exception;

    @Override
    public abstract void returnObject(T obj) throws Exception;

    @Override
    public abstract void invalidateObject(T obj) throws Exception;

    @Override
    public int getNumIdle() {
        return -1;
    }

    @Override
    public int getNumActive() {
        return -1;
    }

    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addObject() throws Exception, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        closed = true;
    }

    public final boolean isClosed() {
        return closed;
    }

    protected final void assertOpen() throws IllegalStateException {
        if (isClosed()) {
            throw new IllegalStateException("Pool not open");
        }
    }

}