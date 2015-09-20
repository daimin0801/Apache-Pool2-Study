package bean.inter.pool;

import java.util.NoSuchElementException;

public interface ObjectPool<T> {
    /**
     * 从池中借出一个对象。要么调用PooledObjectFactory.makeObject方法创建，
     * 要么对一个空闲对象使用PooledObjectFactory.activeObject进行激活，
     * 然后使用PooledObjectFactory.validateObject方法进行验证后再返回
     */
    T borrowObject() throws Exception, NoSuchElementException,
            IllegalStateException;

    /**
     * 将一个对象返还给池。根据约定：对象必须 是使用borrowObject方法从池中借出的
     */
    void returnObject(T obj) throws Exception;

    /**
     * 废弃一个对象。根据约定：对象必须 是使用borrowObject方法从池中借出的。
     * 通常在对象发生了异常或其他问题时使用此方法废弃它
     */
    void invalidateObject(T obj) throws Exception;

    /**
     * 使用工厂创建一个对象，钝化并且将它放入空闲对象池
     */
    void addObject() throws Exception, IllegalStateException,
            UnsupportedOperationException;

    /**
     *返回池中空闲的对象数量。有可能是池中可供借出对象的近似值。
     *如果这个信息无效，返回一个负数
     */
    int getNumIdle();

    /**
     * 返回从借出的对象数量。如果这个信息不可用，返回一个负数
     */
    int getNumActive();

    /**
     *  清除池中的所有空闲对象，释放其关联的资源（可选）。
     *  清除空闲对象必须使用PooledObjectFactory.destroyObject方法
     */
    void clear() throws Exception, UnsupportedOperationException;

    /**
     * 关闭池并释放关联的资源
     */
    void close();
}