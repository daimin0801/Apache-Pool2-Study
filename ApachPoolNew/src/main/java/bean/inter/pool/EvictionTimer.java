package bean.inter.pool;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Timer;
import java.util.TimerTask;
/**
 * EvictionTimer，提供一个所有对象池共享的"空闲对象的驱逐定时器"。此类包装标准的定时器(Timer)，并追踪有多少个对象池使用它。
 * 提供一个所有对象池共享的"空闲对象的驱逐定时器"。
 * 
 * 此类包装标准的定时器({@link Timer})，并追踪有多少个对象池使用它。
 * 
 * 如果没有对象池使用这个定时器，它会被取消。这样可以防止线程一直运行着
 * (这会导致内存泄漏)，防止应用程序关闭或重新加载。
 * <p>
 * 此类是包范围的，以防止其被纳入到池框架的公共API中。
 * <p>
 * <font color="red">此类是线程安全的！</font>
 *
 */
class EvictionTimer {

	 /** Timer instance (定时器实例) */
	private static Timer _timer;

	 /** Timer instance (定时器实例) */
	private static int _usageCount;

	/** Prevent instantiation (防止实例化) */
	private EvictionTimer() {
	}

	 /**
     * 添加指定的驱逐任务到这个定时器。
     * 任务，通过调用该方法添加的，必须调用{@link #cancel(TimerTask)}来取消这个任务，
     * 以防止内存或消除泄漏。
     * 
     * @param task      Task to be scheduled (定时调度的任务)
     * @param delay     Delay in milliseconds before task is executed (任务执行前的等待时间)
     * @param period    Time in milliseconds between executions (执行间隔时间)
     */
	static synchronized void schedule(TimerTask task, long delay, long period) {
		if (null == _timer) {
			// Force the new Timer thread to be created with a context class
			// loader set to the class loader that loaded this library
			ClassLoader ccl = AccessController.doPrivileged(new PrivilegedGetTccl());
			try {
				AccessController.doPrivileged(new PrivilegedSetTccl(EvictionTimer.class.getClassLoader()));
				_timer = AccessController.doPrivileged(new PrivilegedNewEvictionTimer());
			} finally {
				AccessController.doPrivileged(new PrivilegedSetTccl(ccl));
			}
		}
		_usageCount++;
		_timer.schedule(task, delay, period);
	}

	static synchronized void cancel(TimerTask task) {
		task.cancel();
		_usageCount--;
		if (_usageCount == 0) {
			_timer.cancel();
			_timer = null;
		}
	}

	/**
	 * {@link PrivilegedAction} used to get the ContextClassLoader
	 */
	private static class PrivilegedGetTccl implements PrivilegedAction<ClassLoader> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ClassLoader run() {
			return Thread.currentThread().getContextClassLoader();
		}
	}

	/**
	 * {@link PrivilegedAction} used to set the ContextClassLoader
	 */
	private static class PrivilegedSetTccl implements PrivilegedAction<Void> {

		/** ClassLoader */
		private final ClassLoader cl;

		/**
		 * Create a new PrivilegedSetTccl using the given classloader
		 * 
		 * @param cl
		 *            ClassLoader to use
		 */
		PrivilegedSetTccl(ClassLoader cl) {
			this.cl = cl;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void run() {
			Thread.currentThread().setContextClassLoader(cl);
			return null;
		}
	}

	/**
	 * {@link PrivilegedAction} used to create a new Timer. Creating the timer
	 * with a privileged action means the associated Thread does not inherit the
	 * current access control context. In a container environment, inheriting
	 * the current access control context is likely to result in retaining a
	 * reference to the thread context class loader which would be a memory
	 * leak.
	 */
	private static class PrivilegedNewEvictionTimer implements PrivilegedAction<Timer> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Timer run() {
			return new Timer("commons-pool-EvictionTimer", true);
		}
	}
}