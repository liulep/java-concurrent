package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NonFairLockTest {

    public Lock lock = new ReentrantLock();
    public void fairLockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName() + " 强占锁成功");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        NonFairLockTest nonFairLockTest = new NonFairLockTest();
        Thread[] threads = new Thread[4];
        for (int i =0; i < 4; i++) {
            threads[i] = new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                nonFairLockTest.fairLockAndUnlock();
            });
        }

        for (int i =0; i < 4; i++) {
            threads[i].start();
        }
    }
}
