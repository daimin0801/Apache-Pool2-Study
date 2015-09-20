package bean.inter.factory;

import bean.inter.PooledObject;

public abstract class BaseKeyedPooledObjectFactory<K, V> implements KeyedPooledObjectFactory<K, V> {

    public abstract V create(K key) throws Exception;

    public abstract PooledObject<V> wrap(V value);

    @Override
    public PooledObject<V> makeObject(K key) throws Exception {
        return wrap(create(key));
    }

    @Override
    public void destroyObject(K key, PooledObject<V> p) throws Exception {
    }

    @Override
    public boolean validateObject(K key, PooledObject<V> p) {
        return true;
    }

    @Override
    public void activateObject(K key, PooledObject<V> p) throws Exception {
    }

    @Override
    public void passivateObject(K key, PooledObject<V> p) throws Exception {
    }
}