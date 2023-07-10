package deadlock;

import common.Timer;

import java.util.stream.IntStream;

//如果是多人制作多份披萨，如果像线性制作一样，耗时将会呈倍增长，这个时候我们使用并行流
public class PizzaStream {
    static final int QUANTITY = 5;

    public static void main(String[] args) {
        Timer timer = new Timer();
        IntStream.range(0, QUANTITY)
                .mapToObj(Pizza::new)
                .parallel()
                .forEach(za -> {
                    while(!za.complete()){
                        za.next();
                    }
                });
        System.out.println(timer.duration());
    }
}
