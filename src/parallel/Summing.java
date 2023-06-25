package parallel;

import common.Timer;

import java.util.function.LongSupplier;
import java.util.stream.LongStream;

//对一系列递增的数字求和。
public class Summing {

    public static final int SZ = 100_000_000;
    //高斯公式
    public static final long CHECK = (long)SZ * ((long)SZ + 1)/2;
    static void timeTest(String id, long checkValue, LongSupplier operation){
        System.out.print("id => "+id);
        Timer timer = new Timer();
        long result = operation.getAsLong();
        if(result == checkValue){
            System.out.format("  time: %dms\n", timer.duration());
        }else{
            System.out.format("  result => %d\ncheckValue => %d\n",result,checkValue);
        }
    }

    public static void main(String[] args) {
        System.out.format("CHECKVALUE: %d\n",CHECK);
        //Sum Stream
        timeTest("Sum Stream", CHECK, () -> LongStream.rangeClosed(0,SZ).sum());
        //Sum Stream Parallel
        timeTest("Sum Stream Parallel", CHECK, () -> LongStream.rangeClosed(0,SZ)
                .parallel()
                .sum());
        //Sum Iterated
        timeTest("Sum Iterated", CHECK, () -> LongStream.iterate(0, i-> i+1)
                .limit(SZ + 1)
                .sum());
        //Sum Iterated Parallel
        timeTest("Sum Iterated Parallel", CHECK, () -> LongStream.iterate(0, i-> i + 1)
                .limit(SZ+1)
                .parallel()
                .sum());
        //CHECK值是数学家高斯在18世纪晚期还在小学时发明的公式
    }
}
