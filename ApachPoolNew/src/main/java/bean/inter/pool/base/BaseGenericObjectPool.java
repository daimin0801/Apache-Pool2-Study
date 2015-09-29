/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bean.inter.pool.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import bean.inter.PooledObject;
import bean.inter.assist.SwallowedExceptionListener;
import bean.inter.pool.base.assist.EvictionIterator;
import bean.inter.pool.base.assist.StatsStore;
import config.evict.EvictionPolicy;
import config.pool.BaseObjectPoolConfig;
import config.pool.impl.GenericKeyedObjectPoolConfig;

/**
 * 共用两个线程池的代码实现
 */
public abstract class BaseGenericObjectPool<T> {

    public static final int MEAN_TIMING_STATS_CACHE_SIZE = 100;

    // Configuration attributes
    private volatile int maxTotal = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;

    private volatile boolean blockWhenExhausted = BaseObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;
    private volatile long maxWaitMillis = BaseObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;
    private volatile boolean lifo = BaseObjectPoolConfig.DEFAULT_LIFO;
    private final boolean fairness;

    private volatile boolean testOnCreate = BaseObjectPoolConfig.DEFAULT_TEST_ON_CREATE;
    private volatile boolean testOnBorrow = BaseObjectPoolConfig.DEFAULT_TEST_ON_BORROW;
    private volatile boolean testOnReturn = BaseObjectPoolConfig.DEFAULT_TEST_ON_RETURN;
    private volatile boolean testWhileIdle = BaseObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE;

    /** 在空闲连接回收器线程运行期间休眠的时间值,以毫秒为单位. 如果设置为非正数,则不运行空闲连接回收器线程 **/
    private volatile long timeBetweenEvictionRunsMillis = BaseObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;

    /** 在每次空闲连接回收器线程(如果有)运行时检查的连接数量 **/
    private volatile int numTestsPerEvictionRun = BaseObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;

    /** 连接在池中保持空闲而不被空闲连接回收器线程(如果有)回收的最小时间值，单位毫秒 **/
    private volatile long minEvictableIdleTimeMillis = BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    private volatile long softMinEvictableIdleTimeMillis = BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private volatile EvictionPolicy<T> evictionPolicy;

    // Internal (primarily state) attributes
    protected final Object closeLock = new Object();
    protected volatile boolean closed = false;

    /*
     * Class loader for evictor thread to use since, in a JavaEE or similar environment, the context class loader for the evictor thread may not have visibility
     * of the correct factory. See POOL-161. Uses a weak reference to avoid potential memory leaks if the Pool is discarded rather than closed.
     */
    private final WeakReference<ClassLoader> factoryClassLoader;

    private final ObjectName oname;
    private final String creationStackTrace;
    private final AtomicLong borrowedCount = new AtomicLong(0);
    private final AtomicLong returnedCount = new AtomicLong(0);
    protected final AtomicLong createdCount = new AtomicLong(0);
    protected final AtomicLong destroyedCount = new AtomicLong(0);
    protected final AtomicLong destroyedByEvictorCount = new AtomicLong(0);
    protected final AtomicLong destroyedByBorrowValidationCount = new AtomicLong(0);

    private final StatsStore activeTimes = new StatsStore(MEAN_TIMING_STATS_CACHE_SIZE);
    private final StatsStore idleTimes = new StatsStore(MEAN_TIMING_STATS_CACHE_SIZE);
    private final StatsStore waitTimes = new StatsStore(MEAN_TIMING_STATS_CACHE_SIZE);
    private final AtomicLong maxBorrowWaitTimeMillis = new AtomicLong(0L);

    private volatile SwallowedExceptionListener swallowedExceptionListener = null;

    public BaseGenericObjectPool(BaseObjectPoolConfig config, String jmxNameBase, String jmxNamePrefix) {
        if (config.getJmxEnabled()) {
            this.oname = jmxRegister(config, jmxNameBase, jmxNamePrefix);
        } else {
            this.oname = null;
        }

        this.creationStackTrace = getStackTrace(new Exception());

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            factoryClassLoader = null;
        } else {
            factoryClassLoader = new WeakReference<ClassLoader>(cl);
        }

        fairness = config.getFairness();
    }

    public final int getMaxTotal() {
        return maxTotal;
    }

    public final void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public final boolean getBlockWhenExhausted() {
        return blockWhenExhausted;
    }

    public final void setBlockWhenExhausted(boolean blockWhenExhausted) {
        this.blockWhenExhausted = blockWhenExhausted;
    }

    public final long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public final void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public final boolean getLifo() {
        return lifo;
    }

    public final boolean getFairness() {
        return fairness;
    }

    public final void setLifo(boolean lifo) {
        this.lifo = lifo;
    }

    public final boolean getTestOnCreate() {
        return testOnCreate;
    }

    public final void setTestOnCreate(boolean testOnCreate) {
        this.testOnCreate = testOnCreate;
    }

    public final boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public final void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public final boolean getTestOnReturn() {
        return testOnReturn;
    }

    public final void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public final boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public final void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public final int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public final void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public final void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public final long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public final long getSoftMinEvictableIdleTimeMillis() {
        return softMinEvictableIdleTimeMillis;
    }

    public final void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }

    public final String getEvictionPolicyClassName() {
        return evictionPolicy.getClass().getName();
    }

    public final void setEvictionPolicyClassName(String evictionPolicyClassName) {
        try {
            Class<?> clazz;
            try {
                clazz = Class.forName(evictionPolicyClassName, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                clazz = Class.forName(evictionPolicyClassName);
            }
            Object policy = clazz.newInstance();
            if (policy instanceof EvictionPolicy<?>) {
                @SuppressWarnings("unchecked")
                // safe, because we just checked the class
                EvictionPolicy<T> evicPolicy = (EvictionPolicy<T>) policy;
                this.evictionPolicy = evicPolicy;
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to create EvictionPolicy instance of type " + evictionPolicyClassName, e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Unable to create EvictionPolicy instance of type " + evictionPolicyClassName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to create EvictionPolicy instance of type " + evictionPolicyClassName, e);
        }
    }

    public abstract void close();

    public final boolean isClosed() {
        return closed;
    }

    protected EvictionPolicy<T> getEvictionPolicy() {
        return evictionPolicy;
    }

    protected final void assertOpen() throws IllegalStateException {
        if (isClosed()) {
            throw new IllegalStateException("Pool not open");
        }
    }

    public final ObjectName getJmxName() {
        return oname;
    }

    public final String getCreationStackTrace() {
        return creationStackTrace;
    }

    public final long getBorrowedCount() {
        return borrowedCount.get();
    }

    public final long getReturnedCount() {
        return returnedCount.get();
    }

    public final long getCreatedCount() {
        return createdCount.get();
    }

    public final long getDestroyedCount() {
        return destroyedCount.get();
    }

    public final long getDestroyedByEvictorCount() {
        return destroyedByEvictorCount.get();
    }

    public final long getDestroyedByBorrowValidationCount() {
        return destroyedByBorrowValidationCount.get();
    }

    public final long getMeanActiveTimeMillis() {
        return activeTimes.getMean();
    }

    public final long getMeanIdleTimeMillis() {
        return idleTimes.getMean();
    }

    public final long getMeanBorrowWaitTimeMillis() {
        return waitTimes.getMean();
    }

    public final long getMaxBorrowWaitTimeMillis() {
        return maxBorrowWaitTimeMillis.get();
    }

    public abstract int getNumIdle();
    
    
    

    public final SwallowedExceptionListener getSwallowedExceptionListener() {
        return swallowedExceptionListener;
    }

    public final void setSwallowedExceptionListener(SwallowedExceptionListener swallowedExceptionListener) {
        this.swallowedExceptionListener = swallowedExceptionListener;
    }

    protected final void swallowException(Exception e) {
        SwallowedExceptionListener listener = getSwallowedExceptionListener();

        if (listener == null) {
            return;
        }

        try {
            listener.onSwallowException(e);
        } catch (OutOfMemoryError oome) {
            throw oome;
        } catch (VirtualMachineError vme) {
            throw vme;
        } catch (Throwable t) {
            // Ignore. Enjoy the irony.
        }
    }

    protected final void updateStatsBorrow(PooledObject<T> p, long waitTime) {
        borrowedCount.incrementAndGet();
        idleTimes.add(p.getIdleTimeMillis());
        waitTimes.add(waitTime);

        // lock-free optimistic-locking maximum
        long currentMax;
        do {
            currentMax = maxBorrowWaitTimeMillis.get();
            if (currentMax >= waitTime) {
                break;
            }
        } while (!maxBorrowWaitTimeMillis.compareAndSet(currentMax, waitTime));
    }

    protected final void updateStatsReturn(long activeTime) {
        returnedCount.incrementAndGet();
        activeTimes.add(activeTime);
    }

    private ObjectName jmxRegister(BaseObjectPoolConfig config, String jmxNameBase, String jmxNamePrefix) {
        ObjectName objectName = null;
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        int i = 1;
        boolean registered = false;
        String base = config.getJmxNameBase();
        if (base == null) {
            base = jmxNameBase;
        }
        while (!registered) {
            try {
                ObjectName objName;
                // Skip the numeric suffix for the first pool in case there is
                // only one so the names are cleaner.
                if (i == 1) {
                    objName = new ObjectName(base + jmxNamePrefix);
                } else {
                    objName = new ObjectName(base + jmxNamePrefix + i);
                }
                mbs.registerMBean(this, objName);
                objectName = objName;
                registered = true;
            } catch (MalformedObjectNameException e) {
                if (BaseObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX.equals(jmxNamePrefix) && jmxNameBase.equals(base)) {
                    // Shouldn't happen. Skip registration if it does.
                    registered = true;
                } else {
                    // Must be an invalid name. Use the defaults instead.
                    jmxNamePrefix = BaseObjectPoolConfig.DEFAULT_JMX_NAME_PREFIX;
                    base = jmxNameBase;
                }
            } catch (InstanceAlreadyExistsException e) {
                // Increment the index and try again
                i++;
            } catch (MBeanRegistrationException e) {
                // Shouldn't happen. Skip registration if it does.
                registered = true;
            } catch (NotCompliantMBeanException e) {
                // Shouldn't happen. Skip registration if it does.
                registered = true;
            }
        }
        return objectName;
    }

    protected final void jmxUnregister() {
        if (oname != null) {
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(oname);
            } catch (MBeanRegistrationException e) {
                swallowException(e);
            } catch (InstanceNotFoundException e) {
                swallowException(e);
            }
        }
    }

    private String getStackTrace(Exception e) {
        Writer w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        e.printStackTrace(pw);
        return w.toString();
    }

    public final long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public final void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        startEvictor(timeBetweenEvictionRunsMillis);
    }

    protected final Object evictionLock = new Object();
    private Evictor evictor = null; // @GuardedBy("evictionLock")
    protected EvictionIterator evictionIterator = null; // @GuardedBy("evictionLock")

    protected final void startEvictor(long delay) {
        synchronized (evictionLock) {
            if (null != evictor) {
                EvictionTimer.cancel(evictor);
                evictor = null;
                evictionIterator = null;
            }
            if (delay > 0) {
                evictor = new Evictor();
                EvictionTimer.schedule(evictor, delay, delay);
            }
        }
    }

    class Evictor extends TimerTask {
        @Override
        public void run() {
            ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                if (factoryClassLoader != null) {
                    ClassLoader cl = factoryClassLoader.get();
                    if (cl == null) {
                        // The pool has been dereferenced and the class loader
                        // GC'd. Cancel this timer so the pool can be GC'd as
                        // well.
                        cancel();
                        return;
                    }
                    Thread.currentThread().setContextClassLoader(cl);
                }

                try {
                    evict();
                } catch (Exception e) {
                    swallowException(e);
                } catch (OutOfMemoryError oome) {
                    oome.printStackTrace(System.err);
                }
                try {
                    ensureMinIdle();
                } catch (Exception e) {
                    swallowException(e);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }

    public abstract void evict() throws Exception;

    protected abstract void ensureMinIdle() throws Exception;

}