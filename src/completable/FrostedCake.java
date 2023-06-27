package completable;

import common.TimeSleep;

import java.sql.Time;
import java.util.concurrent.CompletableFuture;

final class Frosting{
    private Frosting(){}
    static CompletableFuture<Frosting> make(){
        new TimeSleep(0.1);
        return CompletableFuture.completedFuture(new Frosting());
    }
}
public class FrostedCake {
    public FrostedCake(Baked baked, Frosting frosting){
        new TimeSleep(0.1);
    }

    public static void main(String[] args) {
        Baked.batch().forEach(backed -> backed.thenCombineAsync(Frosting.make(), FrostedCake::new)
                .thenAcceptAsync(System.out::println)
                .join());
    }

    @Override
    public String toString(){
        return "FrostedCake";
    }
}
