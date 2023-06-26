package thread;

import common.TimeSleep;

public class TimeTask implements Runnable {
    final int id;
    public TimeTask(int id){
        this.id = id;
    }
    @Override
    public void run() {
        new TimeSleep(0.1);
        System.out.println(this + ": "+ Thread.currentThread().getName());
    }

    @Override
    public String toString() {
        return "TimeTack["+id+"]";
    }
}
