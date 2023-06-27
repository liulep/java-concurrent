package completable;

import common.TimeSleep;

import java.util.concurrent.CompletableFuture;

public class Batter {
    static class Eggs{} //蛋
    static class Milk{} //牛奶
    static class Sugar{} //糖
    static class Flour{} //面粉
    static <T> T prepare(T ingredient){
        new TimeSleep(0.1);
        return ingredient;
    }

    static <T>CompletableFuture<T> prep(T ingredient){
        return CompletableFuture.completedFuture(ingredient)
                .thenApplyAsync(Batter::prepare);
}

    public static CompletableFuture<Batter> mix(){
        CompletableFuture<Eggs> eggs = prep(new Eggs());
        CompletableFuture<Milk> milk = prep(new Milk());
        CompletableFuture<Sugar> sugar = prep(new Sugar());
        CompletableFuture<Flour> flour = prep(new Flour());
        CompletableFuture.allOf(eggs, milk, sugar, flour)
                .join();
        new TimeSleep(0.1); //混合时间
        return CompletableFuture.completedFuture(new Batter());
    }
}
