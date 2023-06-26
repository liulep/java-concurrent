package interrupt;

import common.TimeSleep;

import java.util.concurrent.atomic.AtomicBoolean;

public class QuittableTask implements Runnable{
    final int id;
    private final AtomicBoolean flag = new AtomicBoolean(true);
    public QuittableTask(int id){
        this.id = id;
    }
    public void quit(){
        flag.set(false);
    }
    @Override
    public void run() {
        while(flag.get()){ //只要flag还是true,该任务的run()方法机会持续执行
            new TimeSleep(0.1);
        }
        System.out.print(id+ " "); //在任务退出后才会执行本行输出
    }
}
