package cas;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * 通过CAS实现count++
 */
public class CasCountIncrement {

    //获取Unsafe对象
    private static final Unsafe unsafe = getUnsafe();
    //线程数量
    private static final int THREAD_COUNT = 20;
    //每个线程运行的次数
    private static final int EXECUTE_COUNT_THREAD = 500;
    //自增的count值
    private volatile int count = 0;
    //count的偏移量
    private static long countOffset;

    static {
        try {
            countOffset = unsafe.objectFieldOffset(CasCountIncrement.class.getDeclaredField("count"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自增操作
     */
    public void incrementCountByCas(){
        int oldCount = 0;
        do{
            oldCount = count;
        }while (!unsafe.compareAndSwapInt(this, countOffset, oldCount, oldCount+1));
    }

    private static Unsafe getUnsafe(){
        Unsafe unsafe = null;
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return unsafe;
    }

    public static void main(String[] args) throws InterruptedException {
        CasCountIncrement casCountIncrement = new CasCountIncrement();
        //定义一个计数器
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        IntStream.range(0, THREAD_COUNT).forEach(i -> {
            new Thread(() ->{
                IntStream.range(0, EXECUTE_COUNT_THREAD).forEach(j -> {
                    casCountIncrement.incrementCountByCas();
                });
                //减一
                countDownLatch.countDown();
            }).start();
        });
        //阻塞main线程，直到计数器为0
        countDownLatch.await();
        System.out.println("count的最终结果为："+ casCountIncrement.count);
    }
}
