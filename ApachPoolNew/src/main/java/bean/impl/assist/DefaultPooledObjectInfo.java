package bean.impl.assist;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import bean.impl.DefaultPooledObject;
import bean.inter.PooledObject;
import bean.inter.jmx.DefaultPooledObjectInfoMBean;

public class DefaultPooledObjectInfo implements DefaultPooledObjectInfoMBean {
    private final String TIME_FORMAT="yyyy-MM-dd HH:mm:ss Z";

    private final PooledObject<?> pooledObject;

    public DefaultPooledObjectInfo(PooledObject<?> pooledObject) {
        this.pooledObject = pooledObject;
    }

    @Override
    public long getCreateTime() {
        return pooledObject.getCreateTime();
    }

    @Override
    public String getCreateTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        return sdf.format(Long.valueOf(pooledObject.getCreateTime()));
    }

    @Override
    public long getLastBorrowTime() {
        return pooledObject.getLastBorrowTime();
    }

    @Override
    public String getLastBorrowTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        return sdf.format(Long.valueOf(pooledObject.getLastBorrowTime()));
    }

    @Override
    public String getLastBorrowTrace() {
        StringWriter sw = new StringWriter();
        pooledObject.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @Override
    public long getLastReturnTime() {
        return pooledObject.getLastReturnTime();
    }

    @Override
    public String getLastReturnTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        return sdf.format(Long.valueOf(pooledObject.getLastReturnTime()));
    }

    @Override
    public String getPooledObjectType() {
        return pooledObject.getObject().getClass().getName();
    }

    @Override
    public String getPooledObjectToString() {
        return pooledObject.getObject().toString();
    }

    @Override
    public long getBorrowedCount() {
        if (pooledObject instanceof DefaultPooledObject) {
            return ((DefaultPooledObject<?>) pooledObject).getBorrowedCount();
        } else {
            return -1;
        }
    }
}