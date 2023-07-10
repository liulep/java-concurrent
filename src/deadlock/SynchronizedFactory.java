package deadlock;

final class SyncFactory implements HasID{
    private final int id;
    private SyncFactory(SharedArg sa){
        id = sa.get();
    }
    @Override
    public int getId() {
        return id;
    }
    public static synchronized SyncFactory factory(SharedArg arg){
        return new SyncFactory(arg);
    }
}
public class SynchronizedFactory {
    public static void main(String[] args) {
        Unsafe unsafe = new Unsafe();
        IDChecker.test(() -> new SyncConstructor(unsafe));
    }
}
