# 并发编程

### 并发与并行概念

并发：如何正确，高效的控制共享资源

​	 同时处理多个任务，不必等待一个任务完成就能开始处理其他任务，并发解决的是阻塞问题（比如I/O）

并行：如何利用更多的资源来产生更快速的响应

​	 同时在多处执行多个任务。并行解决的是计算密集型问题，将任务分成多个小任务，并在多个处理器上执行，从而提升程序运行速度。

> 总而言之，并发是一系列聚焦于如何减少等待并提升性能的技术

并发容易导致常见的竞争条件(race condition)的**共享内存**问题，如工人A将蛋糕放入包装盒时，工人B抢先一步进行放入蛋糕操作，而工人A并未停止放入蛋糕操作，导致两个蛋糕发生了破坏。

> 因此，并发是“理论充满确定性，实际上充满不确定性”

并发能提升运行在**单处理器**上的程序性能，因为单处理的同步执行或者循序执行每次切换任务时，都会有一个上下文切换(context switch,多任务切换)导致额外的开销，而并发则看起来让程序的片段按顺序执行(就像单任务一样)，却减少了上下文切换的开销。

> 一个简单的实现并发的方法：在操作系统层面使用**多进程**，比如通过多开进程进行爬取网络上的数据或图片。进程是在自有地址空间中运行的自包含程序，各个程序相互隔离，互不干扰，可惜的是多进程通常存在数量和开销限制，这影响了多进程在并发领域的适用性。

Java是在其按顺序执行的语言特性上增加了多线程的支持，多线程并不是在多任务操作系统中fork(表示创建线程/进程的动作)出额外的进程，而是在执行程序所维护的单个进程内部创建多任务。

在什么情况下需要使用并发

- 除非任务有阻塞，否则没有理由在单处理器上使用并发
- 除非是您的程序跑的不够快

> 并发唯一的价值在于速度

### 并行流

> 代码库：parallel

Java的流容易并行化，流会使用一种分流器(spliterator)的特殊迭代器，设计要求就是要易于自动分割。

如果代码使用了Stream则可以通过并行化来提升速度。

例如**寻找素数**，我们通过添加计时来看加.parallel()和不加的耗时。

```java
        Timer timer = new Timer();
        List<String> collect = iterate(2, i -> i + 1)
                .parallel().filter(ParallelPrime::isPrime)
                .limit(COUNT)
                .mapToObj(Long::toString)
                .collect(Collectors.toList());
        System.out.println("并行流寻找素数 =>" + timer.duration());
        Files.write(Paths.get("src/parallel/parallelPrimes.txt"),collect, StandardOpenOption.CREATE);
        Timer timer1 = new Timer();
        List<String> collect1 = iterate(2, i -> i + 1)
                .filter(ParallelPrime::isPrime)
                .limit(COUNT)
                .mapToObj(Long::toString)
                .collect(Collectors.toList());
        System.out.println("普通流寻找素数 =>" + timer1.duration());
        Files.write(Paths.get("src/parallel/streamPrimes.txt"),collect1, StandardOpenOption.CREATE);
        //之所以将其保存到文件中，是为了保护文件不受过度优化的影响
        //如果我们对结果什么都不做，编译器可能发现程序毫无意义，然后终止计算（可能发生）
结果：
并行流寻找素数 =>424
普通流寻找素数 =>2260
```

可以看出来并行流寻找素数比普通流寻找素数快了两倍。

虽然并行流看起来十分快速，只需要将程序所要解决的问题转换成流，然后插入parallel()来提升速度，虽然简单高效，却存在则不小的隐患。

为了探寻并行流的不确定性，我们实现一个简单的问题：**对一系列递增的数字求和。**

```Java
        System.out.println(CHECK);
        //Sum Stream
        timeTest("Sum Stream", CHECK, () -> LongStream.rangeClosed(0,SZ).sum());
        //Sum Stream Parallel
        timeTest("Sum Stream Parallel", CHECK, () -> LongStream.rangeClosed(0,SZ)
                .parallel()
                .sum());
        //Sum Iterated
        timeTest("Sum Iterated", CHECK, () -> LongStream.iterate(0, i-> i+1)
                .limit(SZ + 1)
                .sum());
        //Sum Iterated Parallel
        timeTest("Sum Iterated Parallel", CHECK, () -> LongStream.iterate(0, i-> i + 1)
                .limit(SZ+1)
                .parallel()
                .sum());
结果：
CHECKVALUE: 5000000050000000
id => Sum Stream  time: 188ms
id => Sum Stream Parallel  time: 32ms
id => Sum Iterated  time: 232ms
id => Sum Iterated Parallel  time: 2030ms
```

可以看出来Stream流带来的好处，处理10亿的情况下的SZ保证在内存不溢出，使用上parallel()后速度得到显著的提升

而iterate()方法生成序列，速度大打折扣，因为每次生成一个数字都会调用一次lambda表达式，如果尝试并行化，结果会比没用并行时速度更慢。当SZ超出一定的数值时(比如100万后)，还容易导致内存溢出。

流并行化算法初步可以认为

- 流的并行化将输入的数据拆分成多个片段，这样就可以针对独立得数据片段应用各种算法
- 数组的切分轻量、均匀，并且可以完全掌握切分的大小
- 链表则没有这些属性，对链表“切分”意味着将其拆分成“第一元素”和“其余元素”，这没有任何意义。
- 无状态生成器的表现就很想数组，例如rang()
- 迭代式生成器的表现则想链表，例如iterate()

当然速度也取决于内存的限制，现在来实现这样的一个需求：**给一组数组填充值，然后对其求和**

```Java
    public static void main(String[] args) {
        System.out.println("CHECKVALUE => " + CHECK);
        long [] ia = new long[SZ + 1];
        Arrays.parallelSetAll(ia, i -> i); //进行填充数据
        Summing.timeTest("Array Stream Sum", CHECK, () -> Arrays.stream(ia).sum());
        Summing.timeTest("Array Parallel Sum", CHECK, () -> Arrays.stream(ia)
                .parallel()
                .sum());
        Summing.timeTest("Basic Sum", CHECK, () -> basicSum(ia));
        //破坏性求和
        Summing.timeTest("Parallel Prefix", CHECK, () -> {
            Arrays.parallelPrefix(ia, Long::sum);
            return ia[ia.length-1];
        });
    }
结果：
CHECKVALUE => 5000000050000000
id => Array Stream Sum  time: 82ms
id => Array Parallel Sum  time: 47ms
id => Basic Sum  time: 83ms
id => Parallel Prefix  time: 191ms   
```

可以发现Stream并行流填充数组的方式比普通的for更快

现在换成包装的Long

```Java
    public static void main(String[] args) {
        System.out.println("CHECKVALUE => " + CHECK);
        Long [] ia = new Long[SZ + 1];
        Arrays.parallelSetAll(ia, i -> (long)i); //进行填充数据
        Summing.timeTest("Long Array Stream Sum", CHECK, () -> Arrays.stream(ia).reduce(0L,Long::sum));
        Summing.timeTest("Long Array Stream Sum", CHECK, () -> Arrays.stream(ia)
                .parallel()
                .reduce(0L,Long::sum));
        Summing.timeTest("Long Basic Sum", CHECK, () -> basicSum(ia));
        //破坏性求和
        Summing.timeTest("Long Parallel Prefix", CHECK, () -> {
            Arrays.parallelPrefix(ia, Long::sum);
            return ia[ia.length-1];
        });
    }
结果：
CHECKVALUE => 5000000050000000
id => Long Array Stream Sum  time: 1839ms
id => Long Array Stream Sum  time: 1377ms
id => Long Basic Sum  time: 183ms
id => Parallel Prefix  time: 1963ms
```

可用发现当SZ减少并且数组为对象类型时，各处计算所需的时间都呈爆炸式增长，除了基本的for,它只是简单的循环了一下，且其他的方式一直处于边运行边垃圾回收的阶段。

> 处理器的缓存机制是导致耗时增加的主要原因之一，由于使用基本类型long,因此是一段连续的内存，处理器会更快更容易的预测到对这个数组的使用情况，从而将数组数据保存在缓存中以备后续所需，访问缓存远比跳出去访问主存还快，但使用Long包装类作为数组，只是一段连续的Long型对象引用的数组，尽管该数组将会存放在缓存中，但指向的对象却几乎永远在缓存之外。
>
> 盲目使用并行流操作有时候反而让程序跑的更慢。

#### parallel()和limit()的作用

流是围绕无限流的模型设计的，如果要处理有限数量的元素，就需要使用集合，以及专门为有限大小的集合所设计的相关算法，如果要使用无限流则需要使用这些算法专门为流优化后的版本。

Java合并了以上两种情况，举例来说，Collection中是没有map()操作，Collection和Map中唯一的流式批处理操作只有forEach(),如果想执行map()和reduce()的操作，首先将Collection转换成Stream流

```Java
    public static void main(String[] args) {
        List<String> strings =Stream.generate(new Rand.String(5))
                .limit(10)
                .collect(Collectors.toList());
        strings.forEach(System.out::println);
        //转换成Stream
        String result = strings.stream()
                .map(String::toUpperCase)
                .map(s -> s.substring(2))
                .reduce(":",(s1, s2) -> s1 + s2);
        System.out.println(result);
    }
结果：
btpen
pccux
szgvg
meinn
eeloz
tdvew
cippc
ygpoa
lkljl
bynxt
:PENCUXGVGINNLOZVEWPPCPOALJLNXT
```

Collection支持一些批处理操作，比如removeAll(),removeIf()和retainAll()，但是这些都是破坏性操作，

在许多场景中，单纯使用stream()或parallelStream()是没有任何问题的，但是Stream和Collection同时出现则会可能造成意外。

```java
public class ParallelStreamPuzzle {
    static class IntGenerator implements Supplier<Integer>{
        private int current = 0;
        @Override
        public Integer get() {
            return current++;
        }
    }

    public static void main(String[] args) {
        List<Integer> x = Stream
                .generate(new IntGenerator())
                .limit(10)
                .parallel() //[0]
                .toList();
        System.out.println(x);
    }
}
结果：[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
```

当[0]未被注释时，使用了parallel()后，结果就会产生变化

```java
结果：[2, 0, 1, 5, 8, 6, 9, 7, 8, 3]
```

为什么如此简单的程序如此不稳定？

”并行生成“，意味着一堆线程全都运行在一个生成器上，然后以某种方式选择一组有限的结果，代码看起来很简单，却造成了意料之外的状况。

我们使用ConcurrentLinkedDeque类来记录所有的追踪信息，并保存到并发数据结构中。

```Java
public class ParallelStreamPuzzle2 {
    public static final Deque<String> trace = new ConcurrentLinkedDeque<>();

    static class IntGenerator implements Supplier<Integer> {
        private final AtomicInteger current =new AtomicInteger(); //由线程安全的AtomicInteger类来定义，避免竞态资源的发生
        @Override
        public Integer get() {
            trace.add(current.get() + ": "+Thread.currentThread().getName());
            return current.getAndIncrement();
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<Integer> x = Stream.generate(new IntGenerator())
                .limit(100)
                .parallel()
                .toList();
        System.out.println(x);
        Files.write(Paths.get("src/parallel/PSP2.txt"),trace);
    }
}
```

打开PSP2.txt文件，发现生成器的get()方法被多线程调用了多次

```txt
0: ForkJoinPool.commonPool-worker-11
0: main
0: ForkJoinPool.commonPool-worker-7
0: ForkJoinPool.commonPool-worker-6
1: ForkJoinPool.commonPool-worker-11
0: ForkJoinPool.commonPool-worker-15
4: ForkJoinPool.commonPool-worker-7
4: ForkJoinPool.commonPool-worker-6
4: main
5: ForkJoinPool.commonPool-worker-15
10: ForkJoinPool.commonPool-worker-7
10: ForkJoinPool.commonPool-worker-15
10: main
10: ForkJoinPool.commonPool-worker-6
11: ForkJoinPool.commonPool-worker-7
10: ForkJoinPool.commonPool-worker-11
12: ForkJoinPool.commonPool-worker-15
13: main
15: ForkJoinPool.commonPool-worker-6
16: ForkJoinPool.commonPool-worker-7
18: ForkJoinPool.commonPool-worker-15
18: main
16: ForkJoinPool.commonPool-worker-11
20: ForkJoinPool.commonPool-worker-7
20: ForkJoinPool.commonPool-worker-6
21: ForkJoinPool.commonPool-worker-15
22: main
```

分块大小由内部实现决定的，将paralel()和limit()配合使用，可用告诉程序预先选取一组值，以作为流输出。

可以想象到流里面到底发生了什么：

> 流抽象了一个可按需生产的无限序列，当以并行流生成流时，实际实在让所有线程都尽可能都调用get()方法，加上limit()意味着我们想要到的是”一些“，基本来说，如果同时使用parallel()和limit(),那就是在请求随机的输出。

那对于该问题来说，我们就是想以并行流的方式进行实现,怎么样的实现方式更为合理呢？如果只是简单的生成Int流，可以使用IntStream.range().

```java
public class ParallelStreamPuzzle3 {
    public static void main(String[] args) {
        List<Integer> x = IntStream.range(0,30)
                .peek(e -> System.out.println(e + ": "+Thread.currentThread().getName()))
                .limit(10)
                .parallel()
                .boxed()
                .collect(Collectors.toList());
        System.out.println(x);
    }
}
结果：
7: ForkJoinPool.commonPool-worker-7
5: ForkJoinPool.commonPool-worker-5
1: ForkJoinPool.commonPool-worker-2
6: ForkJoinPool.commonPool-worker-3
3: ForkJoinPool.commonPool-worker-6
8: main
9: ForkJoinPool.commonPool-worker-4
0: ForkJoinPool.commonPool-worker-8
2: ForkJoinPool.commonPool-worker-9
4: ForkJoinPool.commonPool-worker-1
[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
```

现在得到了生成不同值得多个线程，也只会根据要求生成10个值，而不是1024个线程来生成10个值

上下文切换得开销远远超过任何并行化所带来得速度提升

> 并行化只是看起来简单

### 创建和运行任务

> 代码库：thread

如果无法通过并行流来实现并发，那么就需要自行创建和运行任务，java8版本的理想实现方式是CompletableFuture.

#### Task和Executor

在java的早期版本中，我们只能通过以下方式进行使用多线程

1. 直接创建Thread对象
2. 实现Thread的子类
3. 创建自定义的特殊”任务-线程“对象

并且创建完之后，我们需要手动调用构造器，并且自行启动线程。就导致创建所有这些线程的开销非常大

在Java5中专门新增了一些类来处理线程池，无需为每个不同的任务类型都创建一个新的Thread子类，只需要将任务创建为一个单独的类型，然后传递给某一个ExecutorService来运行该任务，ExecutorService会为你管理多线程，并且在线程完成后不会丢失，而是回收它们。

现在我们创建一个简单的任务。

```java
public class TimeTask implements Runnable {
    final int id;
    public TimeTask(int id){
        this.id = id;
    }
    @Override
    public void run() {
        new TimeSleep(0.1);
        System.out.println(this + ": "+ Thread.currentThread().getName());
    }

    @Override
    public String toString() {
        return "TimeTack["+id+"]";
    }
}
```

这是一个简单的Runnable;一个包含run()方法的类，这段代码只是通过TimeSleep进行睡眠1秒

> TimeUnit.*MILLISECONDS*.sleep((int)(1000 * t));
> 调用TimeUnit.MILLISECONDS.sleep((int)(1000 * t))，会获得当前线程，并让它按参数中传入的时长进行睡眠，这意味着该线程将被挂起。
>
> 但并不是意味着底层的处理器停止了，操作系统会切换至其他任务，操作系统的任务管理器会定期检查sleep()方法是否到时间，时间一到，线程将会被唤醒，并继续分配给处理器处理。

也可以看到sleep()方法会抛出InterruptedException异常，这是Java早期设计的产物，通过立即跳出任务来终止它们，但是也会极其容易产生不稳定的状态，后续便不鼓励如此终止任务了。

进行执行简单的任务,即SingkeThreadExecutor：

```java
public class SingleThreadExecutor {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        IntStream.range(0,10)
                .mapToObj(TimeTask::new)
                .forEach(executorService::execute);
        System.out.println("aAll tasks submitted");
        executorService.shutdown();
        while(!executorService.isTerminated()){
            System.out.println(Thread.currentThread().getName()+" awaiting termination");
            new TimeSleep(0.1);
        }
    }
}
结果：
All tasks submitted
main awaiting termination
main awaiting termination
TimeTack[0]: pool-1-thread-1
main awaiting termination
TimeTack[1]: pool-1-thread-1
main awaiting termination
TimeTack[2]: pool-1-thread-1
main awaiting termination
TimeTack[3]: pool-1-thread-1
main awaiting termination
TimeTack[4]: pool-1-thread-1
main awaiting termination
TimeTack[5]: pool-1-thread-1
main awaiting termination
TimeTack[6]: pool-1-thread-1
main awaiting termination
TimeTack[7]: pool-1-thread-1
main awaiting termination
TimeTack[8]: pool-1-thread-1
main awaiting termination
TimeTack[9]: pool-1-thread-1
```

注意，并不存在SingleThreadExecutor类，newSingleThreadExecutor()是Executors中的工厂方法，用于创建特定的类型的ExecutorService.

我们创建了十个TimeTask并将它们提交到了ExecutorService中，意味着它们会自动启动，同时，main线程会继续处理其他的事情，当调用executorService.shutdown();时，会告诉ExecutorService完成所有已提交的任务，不再接受任何新任务，在退出main()之前，必须等待这些任务完成，这是通过executorService.isTerminated()的结果实现的，所有任务完成后，该方法返回true

从交错的输出来看，这两个线程的确实在并发的运行着。

如果仅仅调用shutdown(),程序将会在所有任务完成后立即结束

```java
public class SingleThreadExecutor2 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        IntStream.range(0,10)
                .mapToObj(TimeTask::new)
                .forEach(executorService::execute);
        System.out.println("All tasks submitted");
        executorService.shutdown();
    }
}
结果：
All tasks submitted
TimeTack[0]: pool-1-thread-1
TimeTack[1]: pool-1-thread-1
TimeTack[2]: pool-1-thread-1
TimeTack[3]: pool-1-thread-1
TimeTack[4]: pool-1-thread-1
TimeTack[5]: pool-1-thread-1
TimeTack[6]: pool-1-thread-1
TimeTack[7]: pool-1-thread-1
TimeTack[8]: pool-1-thread-1
TimeTack[9]: pool-1-thread-1
```

一旦调用了shutdown()方法后，此后再想提交新的任务，就会抛出RejectedExecutionExcetion异常

```java
public class MoreTasksAfterShutdown {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new TimeTask(1));
        executorService.shutdown();
        try {
            executorService.execute(new TimeTask(1));
        }catch (RejectedExecutionException e){
            System.out.println(e);
        }
    }
}
结果：
java.util.concurrent.RejectedExecutionException: Task TimeTack[1] rejected from java.util.concurrent.ThreadPoolExecutor@34c45dca[Shutting down, pool size = 1, active threads = 1, queued tasks = 0, completed tasks = 0]
TimeTack[1]: pool-1-thread-1
```

executorService.shutdown()的兄弟方法是shutdownNow(),作用是不在接受新任务，同时通过中断来停止所有正在运行的任务。

> tips:中断线程容易引发混乱和错误，不鼓励这么做

#### 使用更多的线程

使用更多线程的主要目的是想让任务完成得更快一些，所以我们为何要将自己限制再singleThreadExecutor中呢？

查看javadoc，发现我们可以有更多得选择，比如CachedThreadPool;

```java
public class CachedThreadPool {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        IntStream.range(0, 10)
                .mapToObj(TimeTask::new)
                .forEach(exec::execute);
        exec.shutdown();
    }
}
结果：
TimeTack[6]: pool-1-thread-7
TimeTack[7]: pool-1-thread-8
TimeTack[4]: pool-1-thread-5
TimeTack[5]: pool-1-thread-6
TimeTack[3]: pool-1-thread-4
TimeTack[0]: pool-1-thread-1
TimeTack[8]: pool-1-thread-9
TimeTack[1]: pool-1-thread-2
TimeTack[2]: pool-1-thread-3
TimeTack[9]: pool-1-thread-10
```

当运行程序时，可以发现它完成得更快。

这很合理，因为不再使用同一个线程来按顺序运行所有任务，而是每一个任务都能获得属于自己的线程，所以它们都是并行的了，这是不是看起来似乎没有任何缺点？

我们实现一个更复杂的任务，来检验是否没有任何问题

```java
public class InterferingTask implements Runnable{
    final int id;
    private static Integer val = 0;
    public InterferingTask(int id) {this.id = id;}
    @Override
    public void run() {
        for(int i = 0; i < 100; i++){
            val ++;
        }
        System.out.println(Thread.currentThread().getName()+" "+val);
    }
}
```

这个任务都会使val自增100次，我们使用CachedThreadPool来试试

```java
public class CachedThreadPool2 {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        IntStream.range(0, 10)
                .mapToObj(InterferingTask::new)
                .forEach(exec::execute);
        exec.shutdown();
    }
}
结果：
1 pool-1-thread-2 190
7 pool-1-thread-8 790
0 pool-1-thread-1 175
4 pool-1-thread-5 490
6 pool-1-thread-7 690
3 pool-1-thread-4 390
8 pool-1-thread-9 890
9 pool-1-thread-10 990
5 pool-1-thread-6 590
2 pool-1-thread-3 290
```

可以发现输出结果并不是我们预料的一样，问题在于所有的任务都在试图对单例的val进行写操作，它们在竞争打架，我们认为这样的类是**非线程安全(not-thread-safe)**的,我们再来看看SingleThreadExecutor会是怎么样的情形

```java
public class SingleThreadExecutor3 {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        IntStream.range(0, 10)
                .mapToObj(InterferingTask::new)
                .forEach(exec::execute);
        exec.shutdown();
    }
}
结果：
0 pool-1-thread-1 100
1 pool-1-thread-1 200
2 pool-1-thread-1 300
3 pool-1-thread-1 400
4 pool-1-thread-1 500
5 pool-1-thread-1 600
6 pool-1-thread-1 700
7 pool-1-thread-1 800
8 pool-1-thread-1 900
9 pool-1-thread-1 1000
```

可以发现结果和我们猜想的是一致的，尽管InterferingTask缺乏线程安全性，由于SingleThreadExecutor的特性（同时只执行一项任务，这些任务永远都不会互相影响，因此保证了线程的安全性），这样的现象称为**线程封闭（Thread confinement）**,因为将多个任务运行在单线程上可以限制它们之间的影响，线程封闭限制了提速，但也节省了很多困难任务的调试和重写工作。

#### 生成结果

由于InterferingTask是一个Runnable的实现，它并没有返回值，因此只能通过副作用(interferingTask中的val)生成结果，即通过控制环境来生成(而不是直接返回)结果，副作用是并发编程的主要问题之一，interferingTask中的val称为**可变共享状态(mutable shared state)**,正是它带来的问题：**多个任务同时修改同一个变量会导致所谓的竞态问题，结果由哪个任务抢先得到终点并修改了变量(以及其他各种可能性)而决定。**

> 避免竞态条件的最好方法是避免使用可变共享状态，我们可以称之为自私儿童原则(selfish child principle):什么都不共享。

对于InterferingTask,消除副作用，我们只需要返回任务结果就好了，要达到这个目的，我们只需要创建一个Callable,而不是Runnable;

```java
public class CountingTask implements Callable<Integer> {
    final int id;
    
    public CountingTask(int id){
        this.id = id;
    }
    
    @Override
    public Integer call() throws Exception {
        Integer val = 0;
        for(int i = 0; i < 100; i++){
            val ++;
        }
        System.out.println(id + " "+Thread.currentThread().getName()+" : "+val);
        return val;
    }
}
```

call()完全独立的生成结果，独立于任何其他CountingTask,这意味着并不存在可变共享状态。

ExecutorService允许在集合中通过invokeAll()来启动所有的Callable;

```java
public class CachedThreadPool3 {
    public static Integer extractResult(Future<Integer> f){
        try {
            return f.get();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        List<CountingTask> tasks = IntStream.range(0,10)
                .mapToObj(CountingTask::new)
                .collect(Collectors.toList());
        List<Future<Integer>> futures = exec.invokeAll(tasks);
        Integer sum = futures.stream()
                .map(CachedThreadPool3::extractResult)
                .reduce(0, Integer::sum);
        System.out.println("sum = "+sum);
        exec.shutdown();
    }
}
结果：
9 pool-1-thread-10 : 100
0 pool-1-thread-1 : 100
1 pool-1-thread-2 : 100
6 pool-1-thread-7 : 100
3 pool-1-thread-4 : 100
8 pool-1-thread-9 : 100
2 pool-1-thread-3 : 100
5 pool-1-thread-6 : 100
7 pool-1-thread-8 : 100
4 pool-1-thread-5 : 100
sum = 1000
```

当所有任务都完成时，invokeAll()才会返回由Future组成的List,每一个Future对应一个任务。

Future是Java5引入的机制，它允许你提交一个任务，并不需要等待它完成。

```java
public class Futures {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Integer> submit = exec.submit(new CountingTask(99));
        System.out.println(submit.get());
        exec.shutdown();
    }
}
结果：
99 pool-1-thread-1 : 100
100
```

当我们对尚未完成的任务进行get()方法调用时，调用会持续阻塞(等待)，直到结果可用。

但这意味着，在CachedThreadPool3.java中，该Future似乎有点多余，因为在所有任务完成之前，invokeAll()不会有任何返回。

由于在调用get()时Future会阻塞，因此它只是将等待任务完成得问题推迟了，最终，Future被认为是一个无效的解决办法，现在并不鼓励它。而是更推荐Java8的CompletableFuture

我们也可以使用更简单也更优雅的方式-----并行流

```java
public class CountingStream {
    public static void main(String[] args) throws Exception {
        Integer sum = IntStream.range(0, 10)
                .parallel()
                .mapToObj(CountingTask::new)
                .map(tc -> {
                    try {
                        return tc.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .reduce(0, Integer::sum);
        System.out.println("sum = "+sum);
    }
}
结果：
9 ForkJoinPool.commonPool-worker-7 : 100
3 ForkJoinPool.commonPool-worker-6 : 100
4 ForkJoinPool.commonPool-worker-3 : 100
7 ForkJoinPool.commonPool-worker-5 : 100
8 ForkJoinPool.commonPool-worker-4 : 100
5 ForkJoinPool.commonPool-worker-9 : 100
1 ForkJoinPool.commonPool-worker-2 : 100
2 ForkJoinPool.commonPool-worker-1 : 100
6 main : 100
0 ForkJoinPool.commonPool-worker-8 : 100
sum = 1000
```

这样更容易理解，我们所做的只需要将parallel()插入一个顺序操作中，然后一切就突然就以并发的形式运行了。

#### 作为任务的lambda与方法引用

Java8通过匹配签名的方式支持lambda和方法引用(也就是支持**结构一致性(structural conformance)**),我们可以将非Runnable或Callable类型的参数传递给ExecutorService;

```java
public class LambdasAndMethodReferences {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.submit(() -> System.out.println("lambda-1"));
        exec.submit(new NotRunnable()::go);
        exec.submit(() -> {
            System.out.println("lambda-2");
            return 1;
        });
        exec.submit(new NotCallable()::get);
        exec.shutdown();
    }
}

class NotRunnable{
    public void go(){
        System.out.println("NotRunnable.");
    }
}

class NotCallable{
    public Integer get(){
        System.out.println("NotCallable.");
        return 1;
    }
}
结果：
lambda-1
NotRunnable.
lambda-2
NotCallable.
```

submit()方法可以更改为execute()，两者最大的区别是是否返回Future。

### 终止长时间运行的任务

> 代码库：interrupt

并发程序通常会运行耗时较长的任务。

Callable的任务在完成时会返回值，虽然这给了它有限的生命周期，但仍然还是会运行很久。

Runnable任务有时被设置为永久运行的后台进程。

我们时常需要某种方式来在Runnable/Callable任务正常结束前提前终止它们，比如要关闭某个程序的时候。

Java最初的设计提供了某种机制来中断(interrupt)正在运行的任务，中断任务在阻塞方面存在一些问题。可能导致数据丢失，所以中断被认为是一种反模式。

终止任务的最好办法是设置一个任务会定期检查的标识，任务可以通过自己的关闭流程来优雅的终止，可以设置任务在合适的时候自行终止，而不是在某一时刻突然拔掉任务的插头，这样的结果永远都比中断好，而且代码也更清晰、更好的理解。

这样的终止任务听起来很简单：设置一个任务可见的boolean标识，修改任务，让其定期检查该标识，并优雅的终止，但还是有麻烦的地方，最要注意的地方-共享可变状态，如果该状态可以被其他任务操作，那么有可能就会造成冲突。

Java5中引入了Atomic类，它提供了一组类型，可以让你无需担心并发问题，并放心使用，下面我们引入AtomicBoolean标识来告诉任务自行清理并退出：

```java
public class QuittableTask implements Runnable{
    final int id;
    public QuittableTask(int id){
        this.id = id;
    }
    private AtomicBoolean flag = new AtomicBoolean(true);
    public void quit(){
        flag.set(false);
    }
    @Override
    public void run() {
        while(flag.get()){ //只要flag还是true,该任务的run()方法机会持续执行
            new TimeSleep(0.1);
        }
        System.out.println("id = "+id); //在任务退出后才会执行本行输出
    }
}
```

虽然多个任务可以成功的在同一个实例中调用quit(),但AtomicBoolean会阻止多个任务同时修改flag,由此保证quit()方法是线程安全的。

现在我们来测试一下

```java
public class QuittingTasks {
    public static final int COUNT = 150;

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        List<QuittableTask> tasks = IntStream.range(1,COUNT)
                .mapToObj(QuittableTask::new)
                .peek(qt -> exec.execute(qt)) //再将任务收录到List之前，通过peek()将QuittingTask传递给ExecutorService
                .collect(Collectors.toList());
        new TimeSleep(0.1);
        tasks.forEach(QuittableTask::quit);
        exec.shutdown();
    }
}
结果：
60 126 104 107 125 112 148 87 53 100 106 61 123 35 38 109 36 101 113 70 68 130 122 99 146 114 135 95 29 108 98 119 43 120 37 20 110 103 78 22 63 2 30 64 25 143 44 121 67 23 46 139 51 105 136 66 147 18 142 144 45 115 34 59 74 133 47 117 116 141 81 149 24 77 86 137 7 138 85 145 71 134 118 93 56 124 90 140 76 12 32 42 40 21 31 57 127 27 128 19 83 33 9 84 41 54 26 89 14 62 65 52 69 80 28 111 79 13 17 82 58 132 91 72 55 39 129 96 73 10 131 88 4 102 75 3 16 94 8 1 11 97 48 50 6 15 49 92 5 
```

main()保证还有任务在运行，程序就会退出，任务关闭的顺序和创建的顺序并不一致，即使每一个任务是按顺序条用quit方法的，这些独立的任务对信号的响应是不可控的。

### CompletableFuture

为了有一个初步认识的印象，下面将QuittingTasks改造成CompletableFuture来实现。

```java
public class QuittingCompletable {
    public static void main(String[] args) {
        List<QuittableTask> tasks = IntStream.range(1, QuittingTasks.COUNT)
                .mapToObj(QuittableTask::new)
                .collect(Collectors.toList());
        List<CompletableFuture<Void>> cFutures = tasks .stream()
                .map(CompletableFuture::runAsync)
                .collect(Collectors.toList());
        new TimeSleep(0.1);
        tasks.forEach(QuittableTask::quit);
        cFutures.forEach(CompletableFuture::join);
    }
}
结果：
14 2 15 3 12 13 7 4 5 9 11 8 16 1 17 18 10 19 20 21 6 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 81 67 68 69 70 71 72 73 74 75 76 77 78 79 80 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100 114 101 102 103 104 120 105 122 123 124 125 106 107 108 109 129 110 111 112 113 115 116 117 118 119 121 126 142 143 144 127 128 130 131 132 133 134 135 136 137 138 139 140 141 145 146 147 148 149 
```

和在QuittingTasks中一样,tasks是List<QuittableTask>类型，但是在本例中，并没有使用peek()来将QuittableTask传递给ExecutorService,而是在cFutures的创建过程中将任务传递给了CompletableFuture::runAsync,这样就会执行QuittableTask.run(),并返回了CompletableFuture<Void>,由于run()并不会返回任务结果，因此我们只只用了CompletableFuture来调用join(),以等待完成。

需要注意的重点：并不要求用ExecutorService来运行任务，这是由CompletableFuture管理的(虽然可以选择自定义的ExecutorService)。也无须调用shutdown(),事实上除非显式调用join(),否则程序会在第一时间退出，而不会等待任务的完成。

#### 基本用法

下面这个类通过静态的work()方法对该对象执行了某些操作。

```java
public class Machina {
    public enum State{
        STATE,ONE,TWO,THREE,END;
        State step(){
            if(equals(END))
                return END;
            return values()[ordinal()-1];
        }
    }
    private State state = State.STATE;
    private final int id;
    public Machina(int id){
        this.id = id;
    }
    public static Machina work(Machina m){
        if(!m.state.equals(State.END)){
            new TimeSleep(0.1);
            m.state = m.state.step();
        }
        System.out.println(m);
        return m;
    }

    @Override
    public String toString() {
        return "Machina" + id + ": "+(state.equals(State.END)? "complete": state);
    }
}
```

work()方法使状态机从一个状态转移到下一个状态，并请求了100毫秒来执行work;

利用CompletableFuture来包装这个对象；

```java
public class CompletableMachina {
    public static void main(String[] args) {
        CompletableFuture<Machina> cf = CompletableFuture.completedFuture(new Machina(0));
        try {
            Machina m = cf.get(); //不会阻塞
        }catch (InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
    }
}
```

completedFuture()创建了一个”已完成“的CompletableFuture,这种Future做的事get()内部对象，所有乍一看这样做好像并没有用处。

注意看CompletableFuture的类型为它包含的对象，这很重要。

一般来说，get()会阻塞正在等待结果的被调用线程，该阻塞可以通过InterruptedExection或者ExecutionExection来退出，在本场景下，由于CompletableFuture已经完成，因此永远不会发生阻塞，当场就能得到结果。

我们还可以通过CompletableFuture来增加操作来控制其包含的对象：

```java
public class CompletableApply {
    public static void main(String[] args) {
        CompletableFuture<Machina> cf = CompletableFuture.completedFuture(new Machina(0));
        CompletableFuture<Machina> cf2 = cf.thenApply(Machina::work);
        CompletableFuture<Machina> cf3 = cf2.thenApply(Machina::work);
        CompletableFuture<Machina> cf4 = cf3.thenApply(Machina::work);
        CompletableFuture<Machina> cf5 = cf4.thenApply(Machina::work);
    }
}
结果：
Machina[0]: ONE
Machina[0]: TWO
Machina[0]: THREE
Machina[0]: complete
```

thenApply()用到了接受输入并生成输出的Funcation,在本例中Function返回和输入相同的类型，由此每个得到的CompletableFuture都仍然是Machina类型，但是Function也可以返回不同的类型，这可以从返回类型上看出来。

可以从中看出来CompletableFuture的一些本质：当执行某一个操作时，它们会自动对其所携带的对象拆开包装，再重新包装，这样就不会陷入混乱的细节，从而可以大幅简化代码的编写和理解工作。

当然我们可以进行消除中间变量，将多个操作串联起来，就像我们使用Stream时那样。

```java
public class CompletableApplyChained {
    public static void main(String[] args) {
        Timer timer = new Timer();
        CompletableFuture<Machina> machinaCompletableFuture = CompletableFuture.completedFuture(new Machina(0))
                .thenApply(Machina::work)// 100ms
                .thenApply(Machina::work)// 100ms
                .thenApply(Machina::work)// 100ms
                .thenApply(Machina::work);// 100ms
        System.out.println(timer.duration()+"ms");
    }
}
结果：
Machina[0]: ONE
Machina[0]: TWO
Machina[0]: THREE
Machina[0]: complete
450ms
```

通过计时发现每一步都增加了100ms，并且还有一些额外的开销。

> 使用CompletableFuture有一个重要的好处：会促使我们使用自私儿童原则(什么都不共享)。
>
> 默认情况下，通过thenApply()来应用函数并不会产生任何通信，它只是接受参数并返回结果，这是函数式编程的基础之一，也是它为何如此适合并发的原因之一。
>
> 并行流和CompletableFuture便是基于这些原则而设计的。只要你决定怎么样都不分享任何数据(分享很容易发生，甚至会有意外发生)，就可以写出相当安全的并发程序。

操作是通过调用thenApply()开始的，本例中，CompletableFuture的创建过程会等到所有的任务完成之后才会开始完成，这虽然很有用，但更多的价值还是在于可以开启所有的任务，然后可以在任务运行时继续做其他的事情，我们通过在操作最后增加Async来实现该效果。

```java
public class CompletableApplyAsync {
    public static void main(String[] args) {
        Timer timer = new Timer();
        CompletableFuture<Machina> machinaCompletableFuture = CompletableFuture.completedFuture(new Machina(0))
                .thenApplyAsync(Machina::work)
                .thenApplyAsync(Machina::work)
                .thenApplyAsync(Machina::work)
                .thenApplyAsync(Machina::work);
        System.out.println(timer.duration());
        System.out.println(machinaCompletableFuture.join());
        System.out.println(timer.duration());
    }
}
结果：
10
Machina[0]: ONE
Machina[0]: TWO
Machina[0]: THREE
Machina[0]: complete
Machina[0]: complete
463
```

同步调用意味着“完成工作后返回”,而异步调用则意味着“立即返回，同时在后台继续工作”。

正如以上所见，现在CompletableFuture的创建过程变快了很多，对thenApplyAsync()的每次调用都会立即返回，这样就可以立即执行下一个调用，整个链式调用序列就会比之前快很多。

执行速度确实就是这么快，在没有调用join()的情况下，程序在任务完成前就退出了,对join()的调用就会一直阻塞main()线程的执行，直到操作完成。

这种可以“立即返回”的异步能力依赖于CompletableFuture库的某些背后操作，通常来说，该库需要将你请求的操作链保存为一组**回调(callback)**,当第一个后台操作完成后，第二个后台操作必须接受相应的Machina并开始工作，然后当该操作完成后，下一个操作继续，以此类推。但是由于并非是程序调用栈控制的普通函数调用序列，其调用顺序会丢失，因此改用回调来存储，即一个记录了函数地址的表格。
