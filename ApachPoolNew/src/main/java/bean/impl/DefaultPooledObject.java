package bean.impl;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;

import bean.inter.PooledObject;
import bean.inter.assist.TrackedUse;
import enums.PooledObjectState;

public class DefaultPooledObject<T> implements PooledObject<T> {

    private final T object;
    private PooledObjectState state = PooledObjectState.IDLE;

    private final long createTime = System.currentTimeMillis();
    private volatile long lastBorrowTime = createTime;
    private volatile long lastUseTime = createTime;
    private volatile long lastReturnTime = createTime;

    private volatile boolean logAbandoned = false;
    private volatile Exception borrowedBy = null;
    private volatile Exception usedBy = null;
    private volatile long borrowedCount = 0;

    public DefaultPooledObject(T object) {
        this.object = object;
    }

    @Override
    public T getObject() {
        return object;
    }
    
    @Override
    public synchronized PooledObjectState getState() {
        return state;
    }
    
    
    @Override
    public void printStackTrace(PrintWriter writer) {
        Exception borrowedByCopy = this.borrowedBy;
        if (borrowedByCopy != null) {
            borrowedByCopy.printStackTrace(writer);
        }
        Exception usedByCopy = this.usedBy;
        if (usedByCopy != null) {
            usedByCopy.printStackTrace(writer);
        }
    }
    
    @Override
    public synchronized boolean allocate() {
        if (state == PooledObjectState.IDLE) {
            state = PooledObjectState.ALLOCATED;
            lastBorrowTime = System.currentTimeMillis();
            lastUseTime = lastBorrowTime;
            borrowedCount++;
            if (logAbandoned) {
                borrowedBy = new AbandonedObjectCreatedException();
            }
            return true;
        } else if (state == PooledObjectState.EVICTION) {
            // TODO Allocate anyway and ignore eviction test
            state = PooledObjectState.EVICTION_RETURN_TO_HEAD;
            return false;
        }
        return false;
    }
    
    
    
    

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public long getActiveTimeMillis() {
        long rTime = lastReturnTime;
        long bTime = lastBorrowTime;

        if (rTime > bTime) {
            return rTime - bTime;
        } else {
            return System.currentTimeMillis() - bTime;
        }
    }

    @Override
    public long getIdleTimeMillis() {
        final long elapsed = System.currentTimeMillis() - lastReturnTime;
        // elapsed may be negative if:
        // - another thread updates lastReturnTime during the calculation window
        // - System.currentTimeMillis() is not monotonic (e.g. system time is
        // set back)
        return elapsed >= 0 ? elapsed : 0;
    }

    @Override
    public long getLastBorrowTime() {
        return lastBorrowTime;
    }

    @Override
    public long getLastReturnTime() {
        return lastReturnTime;
    }

    public long getBorrowedCount() {
        return borrowedCount;
    }

    @Override
    public long getLastUsedTime() {
        if (object instanceof TrackedUse) {
            return Math.max(((TrackedUse) object).getLastUsed(), lastUseTime);
        } else {
            return lastUseTime;
        }
    }

    @Override
    public int compareTo(PooledObject<T> other) {
        final long lastActiveDiff = this.getLastReturnTime() - other.getLastReturnTime();
        if (lastActiveDiff == 0) {
            // Make sure the natural ordering is broadly consistent with equals
            // although this will break down if distinct objects have the same
            // identity hash code.
            // see java.lang.Comparable Javadocs
            return System.identityHashCode(this) - System.identityHashCode(other);
        }
        // handle int overflow
        return (int) Math.min(Math.max(lastActiveDiff, Integer.MIN_VALUE), Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Object: ");
        result.append(object.toString());
        result.append(", State: ");
        synchronized (this) {
            result.append(state.toString());
        }
        return result.toString();
        // TODO add other attributes
    }

    @Override
    public synchronized boolean startEvictionTest() {
        if (state == PooledObjectState.IDLE) {
            state = PooledObjectState.EVICTION;
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean endEvictionTest(Deque<PooledObject<T>> idleQueue) {
        if (state == PooledObjectState.EVICTION) {
            state = PooledObjectState.IDLE;
            return true;
        } else if (state == PooledObjectState.EVICTION_RETURN_TO_HEAD) {
            state = PooledObjectState.IDLE;
            if (!idleQueue.offerFirst(this)) {
                // TODO - Should never happen
            }
        }

        return false;
    }

   

    /**
     * Deallocates the object and sets it {@link PooledObjectState#IDLE IDLE} if it is currently {@link PooledObjectState#ALLOCATED ALLOCATED}.
     * 
     * @return {@code true} if the state was {@link PooledObjectState#ALLOCATED ALLOCATED}
     */
    @Override
    public synchronized boolean deallocate() {
        if (state == PooledObjectState.ALLOCATED || state == PooledObjectState.RETURNING) {
            state = PooledObjectState.IDLE;
            lastReturnTime = System.currentTimeMillis();
            borrowedBy = null;
            return true;
        }

        return false;
    }

    /**
     * Sets the state to {@link PooledObjectState#INVALID INVALID}
     */
    @Override
    public synchronized void invalidate() {
        state = PooledObjectState.INVALID;
    }

    @Override
    public void use() {
        lastUseTime = System.currentTimeMillis();
        usedBy = new Exception("The last code to use this object was:");
    }

  

   

    @Override
    public synchronized void markAbandoned() {
        state = PooledObjectState.ABANDONED;
    }

    @Override
    public synchronized void markReturning() {
        state = PooledObjectState.RETURNING;
    }

    @Override
    public void setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    /**
     * Used to track how an object was obtained from the pool (the stack trace of the exception will show which code borrowed the object) and when the object
     * was borrowed.
     */
    static class AbandonedObjectCreatedException extends Exception {

        private static final long serialVersionUID = 7398692158058772916L;

        /** Date format */
        // @GuardedBy("format")
        private static final SimpleDateFormat format = new SimpleDateFormat("'Pooled object created' yyyy-MM-dd HH:mm:ss Z "
                + "'by the following code has not been returned to the pool:'");

        private final long _createdTime;

        /**
         * Create a new instance.
         * <p>
         * 
         * @see Exception#Exception()
         */
        public AbandonedObjectCreatedException() {
            super();
            _createdTime = System.currentTimeMillis();
        }

        // Override getMessage to avoid creating objects and formatting
        // dates unless the log message will actually be used.
        @Override
        public String getMessage() {
            String msg;
            synchronized (format) {
                msg = format.format(new Date(_createdTime));
            }
            return msg;
        }
    }
}