package completable;

import java.util.stream.Stream;

public class ThrowsChecked {
    class Checked extends Exception{}
    static ThrowsChecked nochecked(ThrowsChecked tc){
        return tc;
    }
    static ThrowsChecked withChecked(ThrowsChecked tc) throws Checked{
        return tc;
    }

    static void StreamTest(){
        Stream.of(new ThrowsChecked())
                .map(ThrowsChecked::nochecked)
                //.map(ThrowsChecked::withChecked) 如果想使用方法引用，必须写出lambda表达式（或者写一个不会抛出异常的包装方法）
                .map(tc -> {
                    try {
                        return withChecked(tc);
                    }catch (Checked e){
                        throw new RuntimeException(e);
                    }
                });
    }
}
