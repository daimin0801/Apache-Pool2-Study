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
package config.evict;

/**
 * 此类用于将对象池的配置信息传递给"驱逐回收策略({@link EvictionPolicy})"实例。
 *  <font color="red">此类是不可变的，且是线程安全的。</font>
 *
 */
public class EvictionConfig {

	/**
	 * 池对象的最大空闲驱逐时间（当池对象的空闲时间超过该值时，立马被强制驱逐掉
	 */
	private final long idleEvictTime;
	

	/**
	 * 池对象的最小空闲驱逐时间（当池对象的空闲时间超过该值时，被纳入驱逐对象列表里）
	 */
	private final long idleSoftEvictTime;
	
	/**
	 *  对象池的最小空闲池对象数量
	 */
	private final int minIdle;

	public EvictionConfig(long poolIdleEvictTime, long poolIdleSoftEvictTime, int minIdle) {
		if (poolIdleEvictTime > 0) {
			idleEvictTime = poolIdleEvictTime;
		} else {
			idleEvictTime = Long.MAX_VALUE;
		}
		if (poolIdleSoftEvictTime > 0) {
			idleSoftEvictTime = poolIdleSoftEvictTime;
		} else {
			idleSoftEvictTime = Long.MAX_VALUE;
		}
		this.minIdle = minIdle;
	}

	public long getIdleEvictTime() {
		return idleEvictTime;
	}

	public long getIdleSoftEvictTime() {
		return idleSoftEvictTime;
	}

	public int getMinIdle() {
		return minIdle;
	}
}