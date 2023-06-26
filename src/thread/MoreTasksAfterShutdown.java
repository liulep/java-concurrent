package thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MoreTasksAfterShutdown {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new TimeTask(1));
        executorService.shutdown();
        try {
            executorService.execute(new TimeTask(1));
        }catch (RejectedExecutionException e){
            System.out.println(e);
        }
    }
}
