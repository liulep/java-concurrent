package thread;

import java.util.stream.IntStream;

public class CountingStream {
    public static void main(String[] args) throws Exception {
        Integer sum = IntStream.range(0, 10)
                .parallel()
                .mapToObj(CountingTask::new)
                .map(tc -> {
                    try {
                        return tc.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .reduce(0, Integer::sum);
        System.out.println("sum = "+sum);
    }
}
