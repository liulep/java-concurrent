package common;

import java.util.concurrent.TimeUnit;

public class TimeSleep {
    public TimeSleep(double t){
        try {
            TimeUnit.MILLISECONDS.sleep((int)(1000 * t));
            //调用TimeUnit.MILLISECONDS.sleep((int)(1000 * t))，会获得当前线程，并让它按参数中传入的时长进行睡眠，这意味着该线程将被挂起。
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public TimeSleep(double t, String msg){
        this(t);
        System.out.println(msg);
    }
}
