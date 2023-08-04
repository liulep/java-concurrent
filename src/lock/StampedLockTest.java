package lock;

import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

public class StampedLockTest {

    private final StampedLock lock = new StampedLock();

    /**
     * 写锁
     */
    public void writeLockAndUnLock(){
        //加锁时返回long的票据
        long stamp = lock.writeLock();
        try {
            System.out.println(Thread.currentThread().getName()+"抢占写锁成功");
        } finally {
            lock.unlock(stamp);
            System.out.println(Thread.currentThread().getName()+"释放写锁成功");
            System.out.println("-----");
        }
    }

    /**
     * 读锁
     */
    public void readLockAndUnLock(){
        //加锁时会返回stamp的票据
        long stamp = lock.readLock();
        try {
            System.out.println(Thread.currentThread().getName()+"抢占读锁成功");
        }finally {
            lock.unlock(stamp);
            System.out.println(Thread.currentThread().getName()+"释放读锁成功");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        StampedLockTest stampedLockTest = new StampedLockTest();
        //写锁
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+"开始抢占资源");
                stampedLockTest.writeLockAndUnLock();
            }).start();
        });

        Thread.sleep(1000);
        System.out.println("=========================================================");
        //写锁
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+"开始抢占资源");
                stampedLockTest.readLockAndUnLock();
            }).start();
        });
    }
}
