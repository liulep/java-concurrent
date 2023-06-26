package thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LambdasAndMethodReferences {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.submit(() -> System.out.println("lambda-1"));
        exec.submit(new NotRunnable()::go);
        exec.submit(() -> {
            System.out.println("lambda-2");
            return 1;
        });
        exec.submit(new NotCallable()::get);
        exec.shutdown();
    }
}

class NotRunnable{
    public void go(){
        System.out.println("NotRunnable.");
    }
}

class NotCallable{
    public Integer get(){
        System.out.println("NotCallable.");
        return 1;
    }
}
