package completable;

import common.TimeSleep;
import interrupt.QuittableTask;
import interrupt.QuittingTasks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QuittingCompletable {
    public static void main(String[] args) {
        List<QuittableTask> tasks = IntStream.range(1, QuittingTasks.COUNT)
                .mapToObj(QuittableTask::new)
                .collect(Collectors.toList());
        List<CompletableFuture<Void>> cFutures = tasks .stream()
                .map(CompletableFuture::runAsync)
                .collect(Collectors.toList());
        new TimeSleep(0.1);
        tasks.forEach(QuittableTask::quit);
        cFutures.forEach(CompletableFuture::join);
    }
}
