package lock;

import java.util.concurrent.locks.LockSupport;

public class LockSupportTest {

    /**
     * 阻塞线程
     */
    public void parkThread(){
        System.out.println(Thread.currentThread().getName()+" 开始阻塞");
        LockSupport.park();
        System.out.println(Thread.currentThread().getName()+" 结束阻塞");
    }

    public static void main(String[] args) throws InterruptedException {
        LockSupportTest lockSupportTest = new LockSupportTest();
        Thread thread = new Thread(() -> {
            lockSupportTest.parkThread();
        });
        thread.start();
        Thread.sleep(200);
        System.out.println(Thread.currentThread().getName()+" 开始唤醒线程");
        LockSupport.unpark(thread);
        System.out.println(Thread.currentThread().getName()+" 结束唤醒线程");
    }
}
