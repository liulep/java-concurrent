package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

//悲观锁
// synchronized也是一种悲观锁
//核心思想： 对所有事务持有悲观的态度，每次都按最坏的情况执行，
//也就是说无论读写，当前线程索取到锁之后，其他线程阻塞等待当前线程释放锁才能继续抢占
//缺点： 在多线程并发的情况下，悲观锁的加锁和释放锁操作会产生大量的CPU线程阻塞挂起，耗费CPU资源，导致CPU调度性能低下
public class PessimismLockTest {

    private Lock lock = new ReentrantLock();

    /**
     * 加锁并释放锁
     */
    public void lockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 抢占锁成功");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        PessimismLockTest pessimismLockTest = new PessimismLockTest();
        IntStream.range(0, 5).forEach((i) -> {
            new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                pessimismLockTest.lockAndUnlock();
            }).start();
        });
    }
}
