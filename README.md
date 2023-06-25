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

- 流的并行化将输入的数据拆分成多个片段
- 数组的切分轻量、均匀，并且可以完全掌握切分的大小
- 链表则没有这些属性，对链表“切分”意味着将其拆分成“第一元素”和“其余元素”，这没有任何意义。
- 无状态生成器的表现就很想数组，例如rang()
- 迭代式生成器的表现则想链表，例如iterate()

对于内存的限制，现在来实现这样的一个需求：**给一组数组填充值，然后对其求和**

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

”并行生成“，意味着一堆线程全都运行在一个生成器上，然后以某种方式选择一组有限的结果