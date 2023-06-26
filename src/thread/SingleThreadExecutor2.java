package thread;

import common.TimeSleep;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class SingleThreadExecutor2 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        IntStream.range(0,10)
                .mapToObj(TimeTask::new)
                .forEach(executorService::execute);
        System.out.println("All tasks submitted");
        executorService.shutdown();
    }
}
