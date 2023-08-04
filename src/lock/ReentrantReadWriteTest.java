package lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteTest {

    private Object data;
    private volatile boolean cacheValid;
    //支持公平锁与非公平锁
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    //不支持读锁升级为写锁
    //但是支持写锁降级为读锁
    //具体实现看Java官方的CacheData类
}
