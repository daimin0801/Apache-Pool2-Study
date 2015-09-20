package config.evict.impl;

import config.evict.EvictionConfig;
import config.evict.EvictionPolicy;
import bean.inter.PooledObject;

/**
 * 提供用在对象池的"驱逐回收策略"的默认实现，继承自{@link EvictionPolicy}。
 * <p>
 * 如果满足以下条件，对象将被驱逐：
 * <ul>
 * <li>池对象的空闲时间超过{@link GenericObjectPool#getMinEvictableIdleTimeMillis()}
 * <li>对象池中的空闲对象数超过{@link GenericObjectPool#getMinIdle()}，
 * 且池对象的空闲时间超过{@link GenericObjectPool#getSoftMinEvictableIdleTimeMillis()}
 * </ul>
 * <font color="red">此类是不可变的，且是线程安全的。</font>
 *
 * @param <T> the type of objects in the pool (对象池中对象的类型)
 *
 * 
 * @since 2.0
 */
public class DefaultEvictionPolicy<T> implements EvictionPolicy<T> {

	@Override
	public boolean evict(EvictionConfig config, PooledObject<T> underTest, int idleCount) {

		if ((config.getIdleSoftEvictTime() < underTest.getIdleTimeMillis() && config.getMinIdle() < idleCount) || config.getIdleEvictTime() < underTest.getIdleTimeMillis()) {
			return true;
		}
		return false;
	}
}