package deadlock;

//通过Synchronized关键字构造同步构造器，构造器实际上是一个静态方法
class SyncConstructor implements HasID{
    private final int id;
    private static Object constructorLock = new Object();
    SyncConstructor(SharedArg sa){
        synchronized (constructorLock){
            id = sa.get();
        }
    }
    @Override
    public int getId() {
        return id;
    }
}
public class SynchronizedConstructor {
    public static void main(String[] args) {
        Unsafe unsafe = new Unsafe();
        IDChecker.test(() -> new SyncConstructor(unsafe));
    }

}
