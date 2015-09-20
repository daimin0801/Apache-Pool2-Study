package config.evict;

import bean.inter.PooledObject;

public interface EvictionPolicy<T> {
	
	/**
	 *  一个对象池中的空闲对象是否应该被驱逐，调用此方法来测试。
	 * @param config 与驱逐相关的对象池配置
	 * @param underTest 正在被驱逐测试的池对象
	 * @param idleCount 当前对象池中的空闲对象数，包括测试中的对象
	 * @return
	 */
    boolean evict(EvictionConfig config, PooledObject<T> underTest,
            int idleCount);
}