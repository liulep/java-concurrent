package deadlock;

import common.Timer;

//如果是一个人制作一份披萨
public class OnePizza {
    public static void main(String[] args) {
        Pizza pizza = new Pizza(0);
        System.out.println(Timer.duration(() -> {
            while(!pizza.complete()){
                pizza.next();
            }
        }));
    }
}
