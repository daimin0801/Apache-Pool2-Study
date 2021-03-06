
package config.abandon;

import java.io.PrintWriter;

public class AbandonedConfig {

    /**
     * 是否在获取对象的时候检查对象，开启的话则检查【主要是检查废弃】
     */
    private boolean removeAbandonedOnBorrow = false;

    /**
     * 是否在保持对象的时候检查对象，开启的话则检查【主要是检查废弃】
     */
    private boolean removeAbandonedOnMaintenance = false;


    /**
     * 此时如果该连接超过removeAbandonedTimeout设置的n秒,认为该连接已被泄漏,连接池进行清理,
     * 注意并没有放入池中,而是直接清理掉
     */
    private int removeAbandonedTimeout = 300;


    /**
     * 是否开启应用代码连接泄露的日志堆栈踪迹
     */
    private boolean logAbandoned = false;


    /**
     * 用来记录泄露的对象信息，默认是用系统输出
     */
    private PrintWriter logWriter = new PrintWriter(System.out);


    /**
     * 
     * 如果一个池实现 了UsageTracking接口，它是否应记录每一次池中对象堆栈踪迹，
     * 并且保持 最近的堆栈踪迹来帮助调试废弃的对象
     */
    private boolean useUsageTracking = false;


	public boolean getRemoveAbandonedOnBorrow() {
		return removeAbandonedOnBorrow;
	}


	public void setRemoveAbandonedOnBorrow(boolean removeAbandonedOnBorrow) {
		this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
	}


	public boolean getRemoveAbandonedOnMaintenance() {
		return removeAbandonedOnMaintenance;
	}


	public void setRemoveAbandonedOnMaintenance(boolean removeAbandonedOnMaintenance) {
		this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
	}


	public int getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}


	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		this.removeAbandonedTimeout = removeAbandonedTimeout;
	}


	public boolean getLogAbandoned() {
		return logAbandoned;
	}


	public void setLogAbandoned(boolean logAbandoned) {
		this.logAbandoned = logAbandoned;
	}


	public PrintWriter getLogWriter() {
		return logWriter;
	}


	public void setLogWriter(PrintWriter logWriter) {
		this.logWriter = logWriter;
	}


	public boolean getUseUsageTracking() {
		return useUsageTracking;
	}


	public void setUseUsageTracking(boolean useUsageTracking) {
		this.useUsageTracking = useUsageTracking;
	}
    
    

}