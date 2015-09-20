package enums;

public enum PooledObjectState {
	/**
	 * 位于队列中，未使用
	 */
    IDLE,
    
    /**
     * 在使用
     */
    ALLOCATED,
 
    /**
     * 位于队列中，当前正在测试，可能会被回收
     */
    EVICTION,

    /**
     * 不在队列中，当前正在测试，可能会被回收。从池中借出对象时需要从队列出移除并进行测试
     */
    EVICTION_RETURN_TO_HEAD,

    /**
     * 位于队列中，当前正在验证
     */
    VALIDATION,

    /**
     * 不在队列中，当前正在验证。当对象从池中被借出，
     * 在配置了testOnBorrow的情况下，对像从队列移除和进行预分配的时候会进行验证
     */
    VALIDATION_PREALLOCATED,

    /**
     * 不在队列中，正在进行验证。
     * 从池中借出对象时，从队列移除对象时会先进行测试。
     * 返回到队列头部的时候应该做一次完整的验证
     */
    VALIDATION_RETURN_TO_HEAD,

    /**
     * 回收或验证失败，将销毁
     */
    INVALID,

    /**
     * 即将无效
     */
    ABANDONED,

    /**
     * 返还到池中
     */
    RETURNING
}