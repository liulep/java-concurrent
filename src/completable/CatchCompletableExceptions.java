package completable;

public class CatchCompletableExceptions {
    static void handleException(int failcount){
        //出现异常时才会调用，Function返回值类型必须和输入相同
        //exceptionally()
        CompletableExceptions
                .test("exceptionally", failcount)
                .exceptionally((ex) -> {
                    //Function
                    if(ex == null){
                        System.out.println("I don't get it yet");
                    }
                    return new Breakable(ex.getMessage(), 0);
                }).thenAccept(str -> System.out.println("result: "+ str));
        //创建新结果(恢复)
        //总是会被调用，必须检查fail是否为true,以此来确定是否有异常发生，handle可以生成任意新类型。
        //handle()
        CompletableExceptions
                .test("handle", failcount)
                .handle((result, fila) -> {
                    if(fila != null){
                        return "Failure recovery object";
                    }else{
                        return result + "is good";
                    }
                }).thenAccept(str -> {
                    System.out.println("result: "+str);
                });
        //做一些逻辑判断，但仍然有向下传递相同的结果
        //和handle类似，必须检查是否有异常发生，但参数是消费者，并不会修改传递中的result对象
        //whenComplete()
        CompletableExceptions
                .test("whenComplete", failcount)
                .whenComplete((result, fail) -> {
                    if(fail != null){
                        System.out.println("It failed");
                    }else{
                        System.out.println(result + " OK");
                    }
                }).thenAccept(str -> System.out.println("result: "+ str));
    }

    public static void main(String[] args) {
        System.out.println("--------------Failure Mode-----------------");
        handleException(2);
        System.out.println("--------------Success Mode-----------------");
        handleException(0);
    }
}
