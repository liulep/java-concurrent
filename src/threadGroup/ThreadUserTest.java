package threadGroup;

public class ThreadUserTest {
    //程序的main()方法就是一个用户线程
    public static void main(String[] args) {
        Thread threadUser = new Thread(() -> {
            System.out.println("我是用户线程");
        }, "threadUser");
        //启动线程
        threadUser.start();
    }
}
