package lock;

public class NonInterruptiblyLockTest {

    public synchronized void lock(){
        try {
            System.out.println(Thread.currentThread().getName()+" 抢占锁成功");
            if(Thread.currentThread().isInterrupted()){
                System.out.println(Thread.currentThread().getName()+" 被中断");
            }
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.println(Thread.currentThread().getName()+" 抢占锁被中断");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NonInterruptiblyLockTest nonInterruptiblyLockTest = new NonInterruptiblyLockTest();
        Thread threadA = new Thread(() -> {
            nonInterruptiblyLockTest.lock();
        },"threadA");

        Thread threadB = new Thread(() -> {
            nonInterruptiblyLockTest.lock();
        },"threadB");

        threadA.start();
        threadB.start();

        Thread.sleep(100);

        threadA.interrupt();
        threadB.interrupt();

        Thread.sleep(2000);
    }
}
