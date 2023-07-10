package deadlock;

import common.TimeSleep;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class DiningPhilosophers {
    private StickHolder[] sticks;//筷子
    private Philosopher[] philosophers;//哲学家
    public DiningPhilosophers(int n){
        this.sticks = new StickHolder[n];
        Arrays.setAll(this.sticks, i -> new StickHolder());
        this.philosophers = new Philosopher[n];
        Arrays.setAll(this.philosophers, i -> new Philosopher(i, this.sticks[i], this.sticks[(i + 1) % n]));
        philosophers[0] = new Philosopher(0, sticks[0], sticks[1]);
        Arrays.stream(this.philosophers)
                .forEach(CompletableFuture::runAsync);
    }

    public static void main(String[] args) {
        new DiningPhilosophers(5);
        //保持main线程不退出
        new TimeSleep(10, "shutdown");
    }
}
