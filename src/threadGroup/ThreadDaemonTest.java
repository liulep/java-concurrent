package threadGroup;

//守护线程是一个特殊的线程，这种线程在系统后台完成相应的任务，例如JVM的垃圾回收，JTI编译线程等
//在程序运行的过程中，只要有一个非守护线程还在运行，守护线程就会一直运行，只有等待非守护线程全都运行结束，守护线程才会退出
public class ThreadDaemonTest {
    public static void main(String[] args) {
        //创建守护线程
        Thread threadDaemon = new Thread(() -> {
            System.out.println("我是守护线程");
        }, "threadDaemon");
        threadDaemon.setDaemon(true);
        threadDaemon.start();
    }
}
