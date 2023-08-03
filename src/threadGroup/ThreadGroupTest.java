package threadGroup;

//线程组可以同时管理多个线程
public class ThreadGroupTest {
    public static void main(String[] args) {
        //创建线程组
        ThreadGroup threadGroup = new ThreadGroup("threadGroupTest");
        //创建Thread实例
        Thread thread1 = new Thread(threadGroup, () -> {
            String groupName = Thread.currentThread().getThreadGroup().getName();
            String threadName = Thread.currentThread().getName();
            System.out.println(groupName+"-"+threadName);
        }, "thread1");
        Thread thread2 = new Thread(threadGroup, () -> {
            String groupName = Thread.currentThread().getThreadGroup().getName();
            String threadName = Thread.currentThread().getName();
            System.out.println(groupName+"-"+threadName);
        }, "thread2");

        //启动线程1
        thread1.start();
        //启动线程2
        thread2.start();
        //在实际的任务中，可以根据线程的不同功能划分到不同的线程组中
    }
}
