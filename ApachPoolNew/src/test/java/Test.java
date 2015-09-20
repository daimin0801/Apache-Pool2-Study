import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Test {
    public static void main(String[] args) {
    }

    public static void test1() {
        AtomicLong maxBorrowWaitTimeMillis = new AtomicLong(0L);
        long currenMax;
        long waitTime = 1000l;
        do {
            currenMax = maxBorrowWaitTimeMillis.get();
            System.out.println(currenMax);
            if (currenMax >= waitTime) {
                System.out.println("currenMax=" + currenMax);
                break;
            }
        } while (!maxBorrowWaitTimeMillis.compareAndSet(currenMax, waitTime));
        System.out.println("end");
    }

    public static void test2() {
        int a = 1;
        int b = 2;
        int c = 3;
        if (a > 0 && a > b || c > 0) {
            System.out.println("&& is high");
        }
    }
    
    public static void test3(){
        int borrowMaxWaitMillis=-1;
        TimeUnit unit=TimeUnit.MILLISECONDS;
        long nanos = unit.toNanos(borrowMaxWaitMillis);
        System.out.println(nanos);
    }
}
