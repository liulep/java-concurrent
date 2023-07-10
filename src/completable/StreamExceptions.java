package completable;

import java.beans.beancontext.BeanContext;
import java.util.stream.Stream;

public class StreamExceptions {
    static Stream<Breakable> test(String id, int fail){
        return Stream.of(new Breakable(id, fail))
                .map(Breakable::work)
                .map(Breakable:: work)
                .map(Breakable::work)
                .map(Breakable::work);
    }

    public static void main(String[] args) {
        test("A", 1);
        test("B", 2);
        Stream<Breakable> c = test("C", 3);
        test("D", 4);
        //并不会抛出异常
        System.out.println("Entering try");
        //直到终结操作时
        try {
            c.forEach(System.out::println);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        //Stream流与CompletableFuture不同，Stream流在没有遇到终结操作时是不会做任何事情的。
        //CompletableFuture不同，会直接执行任务，在遇到异常时会将异常进行保存下来，以备后续的结果取回。
    }
}
