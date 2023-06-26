package completable;

import common.Timer;

import java.util.concurrent.CompletableFuture;

public class CompletableApplyAsync {
    public static void main(String[] args) {
        Timer timer = new Timer();
        CompletableFuture<Machina> machinaCompletableFuture = CompletableFuture.completedFuture(new Machina(0))
                .thenApplyAsync(Machina::work)
                .thenApplyAsync(Machina::work)
                .thenApplyAsync(Machina::work)
                .thenApplyAsync(Machina::work);
        System.out.println(timer.duration());
        System.out.println(machinaCompletableFuture.join());
        System.out.println(timer.duration());
    }
}
