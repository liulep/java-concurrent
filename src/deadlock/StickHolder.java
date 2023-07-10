package deadlock;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//筷子持有者
public class StickHolder {
    private static class ChopStick{} //筷子
    private ChopStick stick = new ChopStick();
    private BlockingQueue<ChopStick> holder = new ArrayBlockingQueue<ChopStick>(1); //线程安全集合，用于并发程序
    public StickHolder(){
        this.pickDown();
    }

    //拿起筷子
    public void pickUp(){
        try {
            holder.take(); //如果调用take()且队列为空，它就会阻塞(等待)，一旦新的元素被放入队列，阻塞就会被解除
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    //放下筷子
    public void pickDown(){
        try {
            holder.put(stick);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}
