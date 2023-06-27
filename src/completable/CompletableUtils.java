package completable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableUtils {
    //获取并展示CF中存储的值
    public static void showr(CompletableFuture<?> cf){
        try {
            System.out.println(cf.get());
        }catch (InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
    }

    //针对无值的CF操作
    public static void voidr(CompletableFuture<Void> cf){
        try {
            cf.get();
        }catch (InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
    }
}
