package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//公平锁实战
public class FairLockTest {
    //公平锁大概介绍
    /**
     * 当多个线程争抢锁时，会先判断锁对应的等待队列是否为空，如果队列为空，或者当前线程是队列头部的元素，则当前线程会获取到锁的资源，
     * 否则将会将当前线程放入队列的尾部等待获取锁
     */

    // 创建公平锁实例
    private Lock lock = new ReentrantLock(true);

    //公平模式下的加锁和释放锁
    public void fairLockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName() + " 强占锁成功");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        FairLockTest fairLockTest = new FairLockTest();
        Thread[] threads = new Thread[4];
        for (int i =0; i < 4; i++) {
            threads[i] = new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                fairLockTest.fairLockAndUnlock();
            });
        }

        for (int i =0; i < 4; i++) {
            threads[i].start();
        }
    }
}
