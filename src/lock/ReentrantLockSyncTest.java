package lock;

import java.util.stream.IntStream;

public class ReentrantLockSyncTest {

    public synchronized  void lockAndUnlock(){
        System.out.println(Thread.currentThread().getName()+" 第一次抢占锁成功");
        synchronized (this){
            System.out.println(Thread.currentThread().getName()+" 第二次抢占锁成功");
        }
        System.out.println(Thread.currentThread().getName()+" 第一次释放锁成功");
    }

    public static void main(String[] args) {
        ReentrantLockSyncTest reentrantLockSyncTest = new ReentrantLockSyncTest();
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                reentrantLockSyncTest.lockAndUnlock();
                System.out.println(Thread.currentThread().getName()+" 第二次释放锁成功");
            }).start();
        });
    }
}
