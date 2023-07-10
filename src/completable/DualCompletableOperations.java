package completable;

import java.util.concurrent.CompletableFuture;
import static completable.CompletableUtils.*;

public class DualCompletableOperations {
    static CompletableFuture<Workable> cfa , cfb;
    static void init(){
        cfa = Workable.make("A", 0.15);
        cfb = Workable.make("B", 0.10); //总是最先执行
    }

    static void join(){
        cfa.join(); //除非显示调用join(),否则程序将会在第一时间退出，而不会等待任务结束
        cfb.join();
        System.out.println("...............................");
    }

    public static void main(String[] args) {
        init();
        voidr(cfa.runAfterEitherAsync(cfb, () -> System.out.println("runAfterEither")));
        //在任一之后运行
        join();

        init();
        voidr(cfa.runAfterBothAsync(cfb, () -> System.out.println("runAfterBoth")));
        //在两者之后运行
        join();

        init();
        showr(cfa.applyToEitherAsync(cfb, w -> {
            System.out.println("applyToEither: "+w);
            return w;
        }));
        //适用于任一
        join();

        init();
        voidr(cfa.acceptEitherAsync(cfb, w -> {
            System.out.println("acceptEither: "+w);
        }));
        //接受任一
        join();

        init();
        voidr(cfa.thenAcceptBothAsync(cfb, (w1, w2) -> {
            System.out.println("thenAcceptBoth: " + w1 + ", " + w2);
        }));
        //同时接受两者
        join();

        init();
        showr(cfa.thenCombineAsync(cfb, (w1, w2) -> {
            System.out.println("thenCombine: "+ w1 +", " + w2);
            return w1;
        }));
        //合并
        join();

        init();
        CompletableFuture<Workable>
                cfc = Workable.make("C", 0.08),
                cfd = Workable.make("D", 0.09);
        CompletableFuture.anyOf(cfa, cfb, cfc, cfd)
                .thenRunAsync(() -> System.out.println("anyOf"));
        join();

        init();
        cfc = Workable.make("C", 0.08);
        cfd = Workable.make("D", 0.09);
        CompletableFuture.allOf(cfa, cfb, cfc, cfd)
                .thenRunAsync(() -> System.out.println("allOf"));
        join();
    }
}
