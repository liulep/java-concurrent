package deadlock;

import common.Timer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//使用CompletableFuture实现并行流制作披萨
public class CompletablePizza {
    static final int QUANTITY = 5;
    public static CompletableFuture<Pizza> makeCF(Pizza pizza){
        return CompletableFuture.completedFuture(pizza)
                .thenApplyAsync(Pizza::roll)
                .thenApplyAsync(Pizza::sauce)
                .thenApplyAsync(Pizza::cheese)
                .thenApplyAsync(Pizza::topping)
                .thenApplyAsync(Pizza::bake)
                .thenApplyAsync(Pizza::slice)
                .thenApplyAsync(Pizza::box);
    }

    public static void show(CompletableFuture<Pizza> cf){
        try {
            System.out.println(cf.join());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        List<CompletableFuture<Pizza>> pizzas = IntStream
                .range(0, QUANTITY)
                .mapToObj(Pizza::new)
                .map(CompletablePizza::makeCF)
                .collect(Collectors.toList());
        System.out.println(timer.duration());
        pizzas.forEach(CompletablePizza::show);
        System.out.println(timer.duration());
    }

}
