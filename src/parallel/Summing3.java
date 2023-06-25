package parallel;

import java.util.Arrays;

public class Summing3 {
    static long basicSum(Long [] ia){
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
        Long [] ia = new Long[SZ + 1];
        Arrays.parallelSetAll(ia, i -> (long)i); //进行填充数据
        Summing.timeTest("Long Array Stream Sum", CHECK, () -> Arrays.stream(ia).reduce(0L,Long::sum));
        Summing.timeTest("Long Array Stream Sum", CHECK, () -> Arrays.stream(ia)
                .parallel()
                .reduce(0L,Long::sum));
        Summing.timeTest("Long Basic Sum", CHECK, () -> basicSum(ia));
        //破坏性求和
        Summing.timeTest("Long Parallel Prefix", CHECK, () -> {
            Arrays.parallelPrefix(ia, Long::sum);
            return ia[ia.length-1];
        });
    }
}
