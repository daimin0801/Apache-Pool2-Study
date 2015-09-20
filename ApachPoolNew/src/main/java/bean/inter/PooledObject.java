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

    /**
     * Attempt to place the pooled object in the {@link PooledObjectState#EVICTION} state.
     * 
     * @return <code>true</code> if the object was placed in the {@link PooledObjectState#EVICTION} state otherwise <code>false</code>
     */
    boolean startEvictionTest();

    /**
     * Called to inform the object that the eviction test has ended.
     * 
     * @param idleQueue
     *            The queue of idle objects to which the object should be returned
     * 
     * @return Currently not used
     */
    boolean endEvictionTest(Deque<PooledObject<T>> idleQueue);


}