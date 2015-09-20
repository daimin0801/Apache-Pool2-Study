package bean.inter.factory;

import bean.inter.PooledObject;

public interface PooledObjectFactory<T> {
  /**
   *用于生成一个新的PooledObject实例
   */
  PooledObject<T> makeObject() throws Exception;

  /**
   *当ObjectPool实例从池中被清理出去丢弃的时候调用
   *（是否根据validateObject的测试结果由具体的实现在而定）
   */
  void destroyObject(PooledObject<T> p) throws Exception;

  /**
   * 可能用于从池中借出对象时，对处于激活（activated）状态的ObjectPool实例进行测试确保它是有效的。
   * 也有可能在ObjectPool实例返还池中进行钝化前调用进行测试是否有效。它只对处于激活状态的实例调用
   */
  boolean validateObject(PooledObject<T> p);

  /**
   * Reinitialize an instance to be returned by the pool.
   *
   * @param p a {@code PooledObject} wrapping the instance to be activated
   *
   * @throws Exception if there is a problem activating <code>obj</code>,
   *    this exception may be swallowed by the pool.
   *
   * @see #destroyObject
   */
  void activateObject(PooledObject<T> p) throws Exception;

  /**
   * Uninitialize an instance to be returned to the idle object pool.
   *
   * @param p a {@code PooledObject} wrapping the instance to be passivated
   *
   * @throws Exception if there is a problem passivating <code>obj</code>,
   *    this exception may be swallowed by the pool.
   *
   * @see #destroyObject
   */
  void passivateObject(PooledObject<T> p) throws Exception;
}