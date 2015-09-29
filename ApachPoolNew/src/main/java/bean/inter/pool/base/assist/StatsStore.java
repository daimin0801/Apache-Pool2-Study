package bean.inter.pool.base.assist;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @Author daimin
 * @Description 
 */
public class StatsStore {
    private final AtomicLong values[];
    private final int size;
    private int index;

    public StatsStore(int size) {
        this.size = size;
        values=new AtomicLong[size];
        for(int i=0;i<size;i++){
            values[i]=new AtomicLong(-1);
        }
    }
    
    public synchronized void add(long value){
        values[index].set(value);
        index++;
        if(index==size){
            index=0;
        }
    }
    
    public long getMean() {
        double result = 0;
        int counter = 0;
        for (int i = 0; i < size; i++) {
            long value = values[i].get();
            if (value != -1) {
                counter++;
                result = result * ((counter - 1) / (double) counter) + value / (double) counter;
            }
        }
        return (long) result;
    }

}
