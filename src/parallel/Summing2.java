package parallel;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

//给一组数组填充值，然后对其求和
public class Summing2 {
    static long basicSum(long [] ia){
        long sum = 0;
        int size = ia.length;
        for(int i=0; i<size; i++){
            sum+=ia[i];
        }
        return sum;
    }

    public static final int SZ = 100_000_000;
    public static final long CHECK = (long)SZ * ((long)SZ + 1)/2;

    public static void main(String[] args) {
        System.out.println("CHECKVALUE => " + CHECK);
        long [] ia = new long[SZ + 1];
        Arrays.parallelSetAll(ia, i -> i); //进行填充数据
        Summing.timeTest("Array Stream Sum", CHECK, () -> Arrays.stream(ia).sum());
        Summing.timeTest("Array Parallel Sum", CHECK, () -> Arrays.stream(ia)
                .parallel()
                .sum());
        Summing.timeTest("Basic Sum", CHECK, () -> basicSum(ia));
        //破坏性求和
        Summing.timeTest("Parallel Prefix", CHECK, () -> {
            Arrays.parallelPrefix(ia, Long::sum);
            return ia[ia.length-1];
        });
    }
}
