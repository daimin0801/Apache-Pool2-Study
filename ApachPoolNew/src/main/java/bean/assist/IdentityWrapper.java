package bean.assist;

/**
 * 
 * @Author daimin
 * @Description 在线程池里用于包装缓存类,只有内存地址相同的对象才是同一引用
 */
public class IdentityWrapper<T> {
	private final T instance;

	public IdentityWrapper(T instance) {
		this.instance = instance;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(instance);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (!(other instanceof IdentityWrapper)) {
			return false;
		} else {
			return ((IdentityWrapper<?>) other).instance == instance;
		}
	}

	public T getObject() {
		return instance;
	}

}
