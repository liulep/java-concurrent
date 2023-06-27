package completable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableExceptions {
    static CompletableFuture<Breakable> test(String id, int failcount){
        return CompletableFuture.completedFuture(new Breakable(id, failcount))
                .thenApplyAsync(Breakable::work)
                .thenApplyAsync(Breakable::work)
                .thenApplyAsync(Breakable::work)
                .thenApplyAsync(Breakable::work);
    }

    public static void main(String[] args) {
        test("A" ,1);
        test("B", 2);
        test("C", 3);
        test("D", 4);
        test("E", 5);
        //异常不会直接显露出来
        try {
            test("F", 2).join();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        //直到我们尝试获取结果时，异常就会被打印出来
        System.out.println(test("G", 2).isCompletedExceptionally());
        System.out.println(test("H", 2).isDone());
        //强制产生异常
        CompletableFuture<Object> cfi = new CompletableFuture<>();
        System.out.println("done? "+cfi.isDone());
        cfi.completeExceptionally(new RuntimeException("forced"));
        try {
            cfi.get();
        }catch (InterruptedException | ExecutionException e){
            System.out.println(e.getMessage());
        }
    }
}
