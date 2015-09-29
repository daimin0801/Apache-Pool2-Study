package bean.inter.pool.base.assist;

import java.util.Deque;
import java.util.Iterator;

import bean.inter.PooledObject;

public class EvictionIterator<T> implements Iterator<PooledObject<T>> {

    private final Deque<PooledObject<T>> idleObjects;
    private final Iterator<PooledObject<T>> idleObjectIterator;

    public EvictionIterator(final Deque<PooledObject<T>> idleObjects, boolean lifo) {
        this.idleObjects = idleObjects;

        if (lifo) {
            idleObjectIterator = idleObjects.descendingIterator();
        } else {
            idleObjectIterator = idleObjects.iterator();
        }
    }

    public Deque<PooledObject<T>> getIdleObjects() {
        return idleObjects;
    }

    @Override
    public boolean hasNext() {
        return idleObjectIterator.hasNext();
    }

    @Override
    public PooledObject<T> next() {
        return idleObjectIterator.next();
    }

    @Override
    public void remove() {
        idleObjectIterator.remove();
    }

}
