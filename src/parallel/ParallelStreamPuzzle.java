package parallel;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ParallelStreamPuzzle {
    static class IntGenerator implements Supplier<Integer>{
        private int current = 0;
        @Override
        public Integer get() {
            return current++;
        }
    }

    public static void main(String[] args) {
        List<Integer> x = Stream
                .generate(new IntGenerator())
                .limit(10)
                .parallel() //[0]
                .toList();
        System.out.println(x);
    }
}
