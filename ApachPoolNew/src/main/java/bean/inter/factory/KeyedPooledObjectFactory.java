package bean.inter.factory;

import bean.inter.PooledObject;

public interface KeyedPooledObjectFactory<K, V> {
    PooledObject<V> makeObject(K key) throws Exception;

    void destroyObject(K key, PooledObject<V> p) throws Exception;

    boolean validateObject(K key, PooledObject<V> p);

    void activateObject(K key, PooledObject<V> p) throws Exception;

    void passivateObject(K key, PooledObject<V> p) throws Exception;
}
