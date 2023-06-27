package completable;

import java.util.concurrent.CompletableFuture;

import static completable.CompletableUtils.showr;
import static completable.CompletableUtils.voidr;

public class CompletableOperations {
    static CompletableFuture<Integer> cfi(int i){
        return CompletableFuture.completedFuture(Integer.valueOf(i));
    }

    public static void main(String[] args) {
        showr(cfi(1)); //基本测试
        voidr(cfi(2).runAsync(() -> System.out.println("runAsync")));
        //cfi(2)是一个条用runAsync的例子，Runnable不会返回任何值，因此结果是一个CompletableFuture<Void>,所以用到了voidr()
        voidr(cfi(3).thenRunAsync(() -> System.out.println("thenRunAsync")));
        //cfi(3)中的thenRunAsync()和runAsync()完全一样，区别只是在runAsync是一个静态的方法。
        voidr(CompletableFuture.runAsync(() -> System.out.println("runAsync is static")));
        //这与cfi(2)是一样的
        showr(CompletableFuture.supplyAsync(() -> 99));
        //静态方法，不过与runAsync不同，它的返回值为Supplier，并且会生成CompletableFuture<Integer>而不是CompletableFuture<Void>

        //then系列方法针对已有的CompletableFuture<Integer>进行操作，不同于thenRunAsync(),用于cif(4),cfi(5),cfi(6)系列的then方法接受未包装的Integer作为参数
        voidr(cfi(4).thenAcceptAsync(i -> System.out.println("thenAcceptAsync: "+i)));
        //thenAcceptAsync接受Consumer作为参数，所以不会返回结果
        showr(cfi(5).thenApplyAsync(i -> i + 42));
        //thenApplyAsync()接受Function作为参数，因此会返回结果(可以和参数类型不同的类型)
        showr(cfi(6).thenComposeAsync(i -> cfi(i + 99)));
        //thenComposeAsync()接受Function作为参数，返回在CompletableFuture中被包装后的结果。

        CompletableFuture<Integer> cf7 = cfi(7);
        cf7.obtrudeValue(111);
        //强制输入一个值作为结果
        showr(cf7);
        showr(cfi(8).toCompletableFuture());
        //cfi(8)中toCompletableFuture()方法从当前的CompletionStage生成CompletableFuture
        cf7= new CompletableFuture<>();
        cf7.complete(9);
        //complete()方法演示了如何通过传入结果来让一个Future完成执行
        showr(cf7);
        CompletableFuture<Object> c = new CompletableFuture<>();
        c.cancel(true);
        System.out.println("cancelled: "+c.isCancelled());
        //cancel()取消了CompletableFuture,它同样会变成“已完成”(done),并且是特殊情况下的完成。
        System.out.println("completed exceptionally: "+c.isCompletedExceptionally());
        //是否存在异常
        System.out.println("done: "+c.isDone());
        //是否已经完成
        System.out.println(c);
        c = new CompletableFuture<>();
        System.out.println(c.getNow(777));
        //getNow()方法要么返回CompletableFuture的完整值，要么返回getNow()的替代参数(如果该future尚未完成)。
        c = new CompletableFuture<>();
        c.thenApplyAsync(i -> (int)i + 42)
                .thenApplyAsync(i -> i * 12);
        System.out.println("dependents: "+c.getNumberOfDependents());
        c.thenApplyAsync(i -> (int)i / 2);
        System.out.println("dependents: "+c.getNumberOfDependents());
    }
}
