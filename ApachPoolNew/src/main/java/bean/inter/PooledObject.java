package bean.inter;

import java.io.PrintWriter;
import java.util.Deque;

import enums.PooledObjectState;

public interface PooledObject<T> extends Comparable<PooledObject<T>> {
    T getObject();

    PooledObjectState getState();

    void printStackTrace(PrintWriter writer);

    boolean allocate();

    boolean deallocate();

    void use();

    void markAbandoned();

    void markReturning();

    void invalidate();

    long getCreateTime();

    long getActiveTimeMillis();

    long getIdleTimeMillis();

    long getLastBorrowTime();

    long getLastReturnTime();

    void setLogAbandoned(boolean logAbandoned);

    long getLastUsedTime();

    @Override
    int compareTo(PooledObject<T> other);

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    @Override
    String toString();

    boolean startEvictionTest();

    boolean endEvictionTest(Deque<PooledObject<T>> idleQueue);

}