package parallel;
import common.Timer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.stream.LongStream.*;

//寻找素数
public class ParallelPrime {
    private static final int COUNT = 100_000;

    public static boolean isPrime(long n){
        return LongStream.rangeClosed(2, (long)Math.sqrt(n))
                .noneMatch(i -> n % i ==0);
    }
    public static void main(String[] args) throws IOException {
        Timer timer = new Timer();
        List<String> collect = iterate(2, i -> i + 1)
                .parallel().filter(ParallelPrime::isPrime)
                .limit(COUNT)
                .mapToObj(Long::toString)
                .collect(Collectors.toList());
        System.out.println("并行流寻找素数 =>" + timer.duration());
        Files.write(Paths.get("src/parallel/parallelPrimes.txt"),collect, StandardOpenOption.CREATE);
        Timer timer1 = new Timer();
        List<String> collect1 = iterate(2, i -> i + 1)
                .filter(ParallelPrime::isPrime)
                .limit(COUNT)
                .mapToObj(Long::toString)
                .collect(Collectors.toList());
        System.out.println("普通流寻找素数 =>" + timer1.duration());
        Files.write(Paths.get("src/parallel/streamPrimes.txt"),collect1, StandardOpenOption.CREATE);
        //之所以将其保存到文件中，是为了保护文件不受过度优化的影响
        //如果我们对结果什么都不做，狡猾的编译器可能发现程序毫无意义，然后终止计算（可能发生）
    }
}
