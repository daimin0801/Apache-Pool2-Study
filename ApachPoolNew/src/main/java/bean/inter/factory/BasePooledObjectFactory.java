package bean.inter.factory;

import bean.inter.PooledObject;

public abstract class BasePooledObjectFactory<T> implements PooledObjectFactory<T> {
    public abstract T create() throws Exception;

    public abstract PooledObject<T> wrap(T obj);

    @Override
    public PooledObject<T> makeObject() throws Exception {
        return wrap(create());
    }

    @Override
    public void destroyObject(PooledObject<T> p) throws Exception {
    }

    @Override
    public boolean validateObject(PooledObject<T> p) {
        return true;
    }

    @Override
    public void activateObject(PooledObject<T> p) throws Exception {
    }

    @Override
    public void passivateObject(PooledObject<T> p) throws Exception {
    }
}