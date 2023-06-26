package parallel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParallelStreamPuzzle2 {
    public static final Deque<String> trace = new ConcurrentLinkedDeque<>();

    static class IntGenerator implements Supplier<Integer> {
        private final AtomicInteger current =new AtomicInteger(); //由线程安全的AtomicInteger类来定义，避免竞态资源的发生
        @Override
        public Integer get() {
            trace.add(current.get() + ": "+Thread.currentThread().getName());
            return current.getAndIncrement();
        }
    }

    public static void main(String[] args) throws IOException {
        List<Integer> x = Stream.generate(new IntGenerator())
                .limit(100)
                .parallel()
                .collect(Collectors.toList());
        System.out.println(x);
        Files.write(Paths.get("src/parallel/PSP2.txt"),trace);
    }
}
