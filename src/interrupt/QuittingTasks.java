package interrupt;

import common.TimeSleep;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QuittingTasks {
    public static final int COUNT = 150;

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        List<QuittableTask> tasks = IntStream.range(1,COUNT)
                .mapToObj(QuittableTask::new)
                .peek(qt -> exec.execute(qt)) //再将任务收录到List之前，通过peek()将QuittingTask传递给ExecutorService
                .collect(Collectors.toList());
        new TimeSleep(0.1);
        tasks.forEach(QuittableTask::quit);
        exec.shutdown();
    }
}
