package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InterruptiblyLockTest {

    private Lock lock = new ReentrantLock();

    /**
     * 加锁并释放锁
     */
    public void lockAndUnLock(){
        try {
            lock.lockInterruptibly();
            System.out.println(Thread.currentThread().getName()+" 抢占到锁");
            if(Thread.currentThread().isInterrupted()){
                System.out.println(Thread.currentThread().getName()+" 被中断");
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName()+" 抢占锁被中断");
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        InterruptiblyLockTest interruptiblyLockTest = new InterruptiblyLockTest();
        Thread threadA = new Thread(() ->{
            interruptiblyLockTest.lockAndUnLock();
        },"threadA");

        Thread threadB = new Thread(() ->{
            interruptiblyLockTest.lockAndUnLock();
        },"threadB");

        threadA.start();
        threadB.start();

        Thread.sleep(100);

        threadB.interrupt();
        threadA.interrupt();

        Thread.sleep(2000);
    }
}
