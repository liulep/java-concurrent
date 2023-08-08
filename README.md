# 并发编程
### 公平锁

> 规规矩矩的排队，一个一个来

```java
//公平锁实战
public class FairLockTest {
    //公平锁大概介绍
    /**
     * 当多个线程争抢锁时，会先判断锁对应的等待队列是否为空，如果队列为空，或者当前线程是队列头部的元素，则当前线程会获取到锁的资源，
     * 否则将会将当前线程放入队列的尾部等待获取锁
     */

    // 创建公平锁实例
    private Lock lock = new ReentrantLock(true);

    //公平模式下的加锁和释放锁
    public void fairLockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName() + " 强占锁成功");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        FairLockTest fairLockTest = new FairLockTest();
        Thread[] threads = new Thread[4];
        for (int i =0; i < 4; i++) {
            threads[i] = new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                fairLockTest.fairLockAndUnlock();
            });
        }

        for (int i =0; i < 4; i++) {
            threads[i].start();
        }
    }
}
```

### 非公平锁

> 第一次尝试插队，插队成功排第一个，插队失败排最后一个

```java
public class NonFairLockTest {

    public Lock lock = new ReentrantLock();
    public void fairLockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName() + " 强占锁成功");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        NonFairLockTest nonFairLockTest = new NonFairLockTest();
        Thread[] threads = new Thread[4];
        for (int i =0; i < 4; i++) {
            threads[i] = new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                nonFairLockTest.fairLockAndUnlock();
            });
        }

        for (int i =0; i < 4; i++) {
            threads[i].start();
        }
    }
}

```

### 悲观锁

> 对所有事务持有悲观的态度，每次都按最坏的情况执行，

```java
// synchronized也是一种悲观锁
//核心思想： 对所有事务持有悲观的态度，每次都按最坏的情况执行，
//也就是说无论读写，当前线程索取到锁之后，其他线程阻塞等待当前线程释放锁才能继续抢占
//缺点： 在多线程并发的情况下，悲观锁的加锁和释放锁操作会产生大量的CPU线程阻塞挂起，耗费CPU资源，导致CPU调度性能低下
public class PessimismLockTest {

    private Lock lock = new ReentrantLock();

    /**
     * 加锁并释放锁
     */
    public void lockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 抢占锁成功");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        PessimismLockTest pessimismLockTest = new PessimismLockTest();
        IntStream.range(0, 5).forEach((i) -> {
            new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                pessimismLockTest.lockAndUnlock();
            }).start();
        });
    }
}

```

### 乐观锁

> 线程每次获取数据时都会认为其他线程不会修改数据，所以不会加锁，但是当前线程在更新数据时会判断当前数据在此期间有没有被其他线程修改过。

```java
/**
 * 乐观锁
 * 核心思想：线程每次获取数据时都会认为其他线程不会修改数据，所以不会加锁，但是当前线程在更新数据时会判断当前数据在此期间有没有被其他线程修改过。
 * 场景： 适合读多写少的情况，可以提升系统的性能
 * 乐观锁如何判断是否修改： 两种方式：版本号和CAS自旋机制
 * java中atomic类就是基于乐观锁实现的
 */
public class OptimisticLockTest {

    private AtomicInteger atomicInteger = new AtomicInteger();

    public void incrementCount(){
        atomicInteger.incrementAndGet();
    }

    public Integer getCount(){
        return atomicInteger.get();
    }

    public static void main(String[] args) throws InterruptedException {
        OptimisticLockTest optimisticLockTest = new OptimisticLockTest();
        IntStream.range(0, 10).forEach(i -> {
            new Thread(() -> {
                optimisticLockTest.incrementCount();
            }).start();
        });
        TimeUnit.MILLISECONDS.sleep(500);
        Integer count = optimisticLockTest.getCount();
        System.out.println("最终结果为 ： "+count);
    }
}
```

### 可中断锁

> 指的是在多个线程抢占过程中可被中断的锁
>
> ReentrantLock就是可中断锁，它提供了两个方法
>
> - lockInterruptibly()
>
> - tryLock()
>
>   无论是那种方式，都是通过处理Thread类中的interrup()方法发出中断信号来处理中断的

```java
package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InterruptiblyLockTest {

    private Lock lock = new ReentrantLock();

    /**
     * 加锁并释放锁
     */
    public void lockAndUnLock(){
        try {
            lock.lockInterruptibly();
            System.out.println(Thread.currentThread().getName()+" 抢占到锁");
            if(Thread.currentThread().isInterrupted()){
                System.out.println(Thread.currentThread().getName()+" 被中断");
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName()+" 抢占锁被中断");
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        InterruptiblyLockTest interruptiblyLockTest = new InterruptiblyLockTest();
        Thread threadA = new Thread(() ->{
            interruptiblyLockTest.lockAndUnLock();
        },"threadA");

        Thread threadB = new Thread(() ->{
            interruptiblyLockTest.lockAndUnLock();
        },"threadB");

        threadA.start();
        threadB.start();

        Thread.sleep(100);

        threadB.interrupt();
        threadA.interrupt();

        Thread.sleep(2000);
    }
}
```

### 不可中断锁

> 指的是在抢占锁的过程中不能被中断，如果抢占成功则继续执行业务逻辑，也可被中断，如果抢占失败，则阻塞挂起且不能被中断。

```java
package lock;

public class NonInterruptiblyLockTest {

    public synchronized void lock(){
        try {
            System.out.println(Thread.currentThread().getName()+" 抢占锁成功");
            if(Thread.currentThread().isInterrupted()){
                System.out.println(Thread.currentThread().getName()+" 被中断");
            }
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.println(Thread.currentThread().getName()+" 抢占锁被中断");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NonInterruptiblyLockTest nonInterruptiblyLockTest = new NonInterruptiblyLockTest();
        Thread threadA = new Thread(() -> {
            nonInterruptiblyLockTest.lock();
        },"threadA");

        Thread threadB = new Thread(() -> {
            nonInterruptiblyLockTest.lock();
        },"threadB");

        threadA.start();
        threadB.start();

        Thread.sleep(100);

        threadA.interrupt();
        threadB.interrupt();

        Thread.sleep(2000);
    }
}

```

### 排他锁

> 也称独占锁或互斥锁，指的是在同一时刻只能被一条线程获取到，其他线程阻塞挂起。

```java
package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

public class MutexLockTest {

    private ReadWriteLock readWriteLock =new ReentrantReadWriteLock();
    private Lock lock = readWriteLock.writeLock();

    /**
     * 加锁且释放锁
     */

    public void lockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 抢占锁成功");
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.println(Thread.currentThread().getName()+" 被中断");
        }finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+" 释放锁成功");
        }
    }

    public static void main(String[] args) {
        MutexLockTest mutexLockTest = new MutexLockTest();
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                mutexLockTest.lockAndUnlock();
            }).start();
        });
    }
}

```

### 共享锁

> 在同一时刻能够被多个线程获取到，需要注意的是，多个线程同时获取到共享锁之后，只能对临界区的资源进行读操作，而不能进行修改操作，也就是说，共享锁是针对于读操作的锁。

```java
package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

public class SharedLockTest {


    private ReadWriteLock readWriteLock =new ReentrantReadWriteLock();
    private Lock lock = readWriteLock.readLock();

    /**
     * 加锁且释放锁
     */

    public void lockAndUnlock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 抢占锁成功");
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.println(Thread.currentThread().getName()+" 被中断");
        }finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+" 释放锁成功");
        }
    }

    public static void main(String[] args) {
        MutexLockTest mutexLockTest = new MutexLockTest();
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                mutexLockTest.lockAndUnlock();
            }).start();
        });
    }
}

```

### 可重入锁

> 一个线程可以反复对相同的资源重复加锁

使用ReentrantLock

```java
package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ReentrantLockTest {

    private Lock lock = new ReentrantLock();

    /**
     * 加锁解锁操作
     */
    public void lockAndUnLock(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 第一次加锁成功");
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" 第二次加锁成功");
        }finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+" 第一次解锁成功");
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+" 第二次解锁成功");
        }
    }

    public static void main(String[] args) {
        ReentrantLockTest reentrantLockTest = new ReentrantLockTest();
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                reentrantLockTest.lockAndUnLock();
            }).start();
        });
    }
}

```

使用synchronized

```java
package lock;

import java.util.stream.IntStream;

public class ReentrantLockSyncTest {

    public synchronized  void lockAndUnlock(){
        System.out.println(Thread.currentThread().getName()+" 第一次抢占锁成功");
        synchronized (this){
            System.out.println(Thread.currentThread().getName()+" 第二次抢占锁成功");
        }
        System.out.println(Thread.currentThread().getName()+" 第一次释放锁成功");
    }

    public static void main(String[] args) {
        ReentrantLockSyncTest reentrantLockSyncTest = new ReentrantLockSyncTest();
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName()+" 开始抢占锁");
                reentrantLockSyncTest.lockAndUnlock();
                System.out.println(Thread.currentThread().getName()+" 第二次释放锁成功");
            }).start();
        });
    }
}

```

### 读/写锁

> 包含一个写锁和读锁
>
> 写锁是排他锁，在同一时刻只能有一个线程获取到锁
>
> 读锁是共享锁，允许在同一个时刻同时获取到锁

```java
package lock;

import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

/**
* 使用StampedLock实现
**/
public class StampedLockTest {

    private final StampedLock lock = new StampedLock();

    /**
     * 写锁
     */
    public void writeLockAndUnLock(){
        //加锁时返回long的票据
        long stamp = lock.writeLock();
        try {
            System.out.println(Thread.currentThread().getName()+"抢占写锁成功");
        } finally {
            lock.unlock(stamp);
            System.out.println(Thread.currentThread().getName()+"释放写锁成功");
            System.out.println("-----");
        }
    }

    /**
     * 读锁
     */
    public void readLockAndUnLock(){
        //加锁时会返回stamp的票据
        long stamp = lock.readLock();
        try {
            System.out.println(Thread.currentThread().getName()+"抢占读锁成功");
        }finally {
            lock.unlock(stamp);
            System.out.println(Thread.currentThread().getName()+"释放读锁成功");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        StampedLockTest stampedLockTest = new StampedLockTest();
        //写锁
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+"开始抢占资源");
                stampedLockTest.writeLockAndUnLock();
            }).start();
        });

        Thread.sleep(1000);
        System.out.println("=========================================================");
        //写锁
        IntStream.range(0, 5).forEach(i -> {
            new Thread(() ->{
                System.out.println(Thread.currentThread().getName()+"开始抢占资源");
                stampedLockTest.readLockAndUnLock();
            }).start();
        });
    }
}

```

### LockSupport

> Java提供的创建锁和其他多线程工具类的基础类库，最主要的作用时阻塞和唤醒线程。

```java
public class LockSupportTest {

    /**
     * 阻塞线程
     */
    public void parkThread(){
        System.out.println(Thread.currentThread().getName()+" 开始阻塞");
        LockSupport.park();
        System.out.println(Thread.currentThread().getName()+" 结束阻塞");
    }

    public static void main(String[] args) throws InterruptedException {
        LockSupportTest lockSupportTest = new LockSupportTest();
        Thread thread = new Thread(() -> {
            lockSupportTest.parkThread();
        });
        thread.start();
        Thread.sleep(200);
        System.out.println(Thread.currentThread().getName()+" 开始唤醒线程");
        LockSupport.unpark(thread);
        System.out.println(Thread.currentThread().getName()+" 结束唤醒线程");
    }
}
```

### Unsafe

> 实现CAS（Compare And Swap 比较且交换）操作的底层核心类，提供硬件级别的原子性操作，在Unsafe类中，提供了大量的native方法，通过调用JTI的方式调用JVM底层C和C++实现的函数。

```java
package cas;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe案例
 */
public class UnsafeTest {
    private static final Unsafe unsafe = getUnsafe();
    private static long staticNameOffset = 0;
    private static long memberVariableOffset = 0;
    private static String staticName = "liulep";
    private static String memberVariable = "liulep";

    static {
        try {
            staticNameOffset = unsafe.staticFieldOffset(UnsafeTest.class.getDeclaredField("staticName"));
            memberVariableOffset = unsafe.staticFieldOffset(UnsafeTest.class.getDeclaredField("memberVariable"));
        }catch (NoSuchFieldException e){
            e.printStackTrace();
        }
    }

    private static Unsafe getUnsafe(){
       Unsafe unsafe = null;
       try {
           Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
           theUnsafe.setAccessible(true);
           unsafe = (Unsafe) theUnsafe.get(null);
       }catch (Exception e){
           e.printStackTrace();
       }
       return unsafe;
    }

    public static void main(String[] args) {
        UnsafeTest unsafeTest = new UnsafeTest();
        System.out.println("修改前的数值如下：");
        System.out.println("staticName = "+staticName+",memberVariable = "+unsafeTest.memberVariable);

        unsafe.putObject(UnsafeTest.class, staticNameOffset, "liulep_static");
        unsafe.compareAndSwapObject(unsafeTest, memberVariableOffset, "liulep", "liulep_member");
        System.out.println("修改后的数值如下");
        System.out.println("staticName = "+staticName+",memberVariable = "+unsafeTest.memberVariable);
    }
}
```

### 死锁

```java
/**
 * 线程不安全的转账操作
 */
public class UnsafeTransferAccount {

    private long balance = 500;

    /**
     *
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(UnsafeTransferAccount targetAccount, long transferMoney){
        synchronized (this){ //此处的锁只能锁住当前this对象，无法所锁住别人账户的对象锁，所以还是存在线程安全
            if(this.balance >= transferMoney){
                this.balance -= transferMoney;
                targetAccount.balance += transferMoney;
            }
        }
    }
}
```

```java
/**
 * 安全的账户转账
 */
public class SafeTransferAccount {

    private long balance = 500;

    /**
     *
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(SafeTransferAccount targetAccount, long transferMoney){
        synchronized (SafeTransferAccount.class){ //通过锁住class对象来达到串行执行
            if(this.balance >= transferMoney){
                this.balance -= transferMoney;
                targetAccount.balance += transferMoney;
            }
        }
    }
    /**
     * 缺点：会导致排队现象，转账的时候必须等待前一个人转账成功，对于银行转账操作是不可取的
     * 之前都是对当前转账人账户进行加锁或者是对全局的账户对象进行加锁导致了线程安全问题和排队现象问题，并没有对收款人账户进行加锁
     * 那如果我们只对这两个账户进行加锁，是不是并行执行呢
     * 往下看
     */
}

```

```java
public class DeadLockTransferAccount {

    private long balance = 500;

    /**
     *
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(DeadLockTransferAccount targetAccount, long transferMoney){
        synchronized (this){ 
            synchronized (targetAccount){
                if(this.balance >= transferMoney){
                    this.balance -= transferMoney;
                    targetAccount.balance += transferMoney;
                }
            }
        }
    }

    /**
     * 结果就是会导致死锁的产生
     
     */
}

```

```java
/**
 * 破坏不可剥夺条件来预防死锁
 */
public class LockTransferAccount {

    //账户余额
    private long balance;
    //转出账户的锁
    private Lock thisLock = new ReentrantLock();
    //转入账户的锁
    private Lock targetAccountLock = new ReentrantLock();

    /**
     * @param targetAccount 别人的账户
     * @param transferMoney 所需转账的金额
     */
    public void transferMoney(LockTransferAccount targetAccount, long transferMoney) {
        try {
            if (thisLock.tryLock()) {
                try {
                    if (targetAccountLock.tryLock()) {
                        if (this.balance >= transferMoney) {
                            this.balance -= transferMoney;
                            targetAccount.balance -= transferMoney;
                        }
                    }
                } finally {
                    targetAccountLock.unlock();
                }
            }
        }finally {
            thisLock.unlock();
        }
    }
}
;
```

