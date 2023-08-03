package lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 乐观锁
 * 核心思想：线程每次获取数据时都会认为其他线程不会修改数据，所以不会加锁，但是当前线程在更新数据时会判断当前数据在此期间有没有被其他线程修改过。
 * 场景： 适合读多写少的情况，可以提升系统的性能
 * 乐观锁如何判断是否修改： 两种方式：版本号和CAS自旋机制
 * java中atomic类就是基于乐观锁实现的
 */
public class OptimisticLockTest {

    private AtomicInteger atomicInteger = new AtomicInteger();

    public void incrementCount(){
        atomicInteger.incrementAndGet();
    }

    public Integer getCount(){
        return atomicInteger.get();
    }

    public static void main(String[] args) throws InterruptedException {
        OptimisticLockTest optimisticLockTest = new OptimisticLockTest();
        IntStream.range(0, 10).forEach(i -> {
            new Thread(() -> {
                optimisticLockTest.incrementCount();
            }).start();
        });
        TimeUnit.MILLISECONDS.sleep(500);
        Integer count = optimisticLockTest.getCount();
        System.out.println("最终结果为 ： "+count);
    }

}
