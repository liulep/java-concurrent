package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ReentrantLockTest {

    private Lock lock = new ReentrantLock();

    /**
     * 加锁解锁操作
     */
    public void lockAndUnLock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 第一次加锁成功");
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 第二次加锁成功");
        }finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+" 第一次解锁成功");
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+" 第二次解锁成功");
        }
    }

    public static void main(String[] args) {
        ReentrantLockTest reentrantLockTest = new ReentrantLockTest();
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                reentrantLockTest.lockAndUnLock();
            }).start();
        });
    }
}
