package bean.inter.jmx;

/**
 * JMX监控
 */
public interface DefaultPooledObjectInfoMBean {
    long getCreateTime();

    String getCreateTimeFormatted();

    long getLastBorrowTime();

    String getLastBorrowTimeFormatted();

    String getLastBorrowTrace();

    long getLastReturnTime();

    String getLastReturnTimeFormatted();

    String getPooledObjectType();

    String getPooledObjectToString();

    long getBorrowedCount();
}