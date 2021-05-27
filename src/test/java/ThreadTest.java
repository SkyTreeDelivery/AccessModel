import org.junit.Test;

import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ThreadTest {

    public void test() throws ExecutionException, InterruptedException {

        // 创建FutureTask
        FutureTask<Integer> futureTask
                = new FutureTask<>(()-> 1+2);
        // 创建线程池
        ExecutorService es =
                Executors.newCachedThreadPool();
        // 提交FutureTask
        es.submit(futureTask);
        // 获取计算结果
        Integer result = futureTask.get();
    }

    @Test
    public void makeTea() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Callable<String> getTea = ()->{
            System.out.println("洗茶壶");
            TimeUnit.SECONDS.sleep(1);
            System.out.println("拿茶叶");
            TimeUnit.SECONDS.sleep(1);
            return "龙井";
        };
        Callable<String> heatup = ()->{
            System.out.println("烧开水");
            TimeUnit.SECONDS.sleep(10);
            return "";
        };

        System.out.println("洗水壶");
        TimeUnit.SECONDS.sleep(1);
        Future<String> heatUpFeature = executorService.submit(heatup);
        Future<String> getTeaFeature = executorService.submit(getTea);
        String tea = getTeaFeature.get();
        heatUpFeature.get();
        System.out.println("上茶" + tea);
    }

    @Test
    public void test1(){


//任务1：洗水壶->烧开水
        CompletableFuture<Void> f1 =
                CompletableFuture.runAsync(()->{
                    System.out.println("T1:洗水壶...");
                    sleep(1, TimeUnit.SECONDS);

                    System.out.println("T1:烧开水...");
                    sleep(15, TimeUnit.SECONDS);
                });
//任务2：洗茶壶->洗茶杯->拿茶叶
        CompletableFuture<String> f2 =
                CompletableFuture.supplyAsync(()->{
                    System.out.println("T2:洗茶壶...");
                    sleep(1, TimeUnit.SECONDS);

                    System.out.println("T2:洗茶杯...");
                    sleep(2, TimeUnit.SECONDS);

                    System.out.println("T2:拿茶叶...");
                    sleep(1, TimeUnit.SECONDS);
                    return "龙井";
                });
//任务3：任务1和任务2完成后执行：泡茶
        CompletableFuture<String> f3 =
                f1.thenCombine(f2, (__, tf)->{
                    System.out.println("T1:拿到茶叶:" + tf);
                    System.out.println("T1:泡茶...");
                    return "上茶:" + tf;
                });
//等待任务3执行结果
        System.out.println(f3.join());

    }
    void sleep(int t, TimeUnit u) {
        try {
            u.sleep(t);
        }catch(InterruptedException e){}
    }


    @Test
    public void testFI() {
        //创建分治任务线程池
        ForkJoinPool fjp =
                new ForkJoinPool(4);
        //创建分治任务
        Fibonacci fib =
                new Fibonacci(8);
        //启动分治任务
        Integer result =
                fjp.invoke(fib);
        //输出结果
        System.out.println(result);
    }
    //递归任务
    static class Fibonacci extends
            RecursiveTask<Integer>{
        final int n;
        Fibonacci(int n){this.n = n;}
        protected Integer compute(){
            System.out.println(Thread.currentThread().getName());
            if (n <= 1)
                return n;
            Fibonacci f1 =
                    new Fibonacci(n - 1);
            //创建子任务
            f1.fork();
            Fibonacci f2 =
                    new Fibonacci(n - 2);
            //等待子任务结果，并合并结果
            return f2.compute() + f1.join();
        }
    }

    @Test
    public void forkJoinPoolTest() throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool(3);
        ForkJoinTask<?> submit = forkJoinPool.submit(() -> {
            IntStream.range(0, 3).parallel().forEach((number) -> {
                try {
                    System.out.println(Thread.currentThread().getName());
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
            });
            System.out.println("finish");
        });
        submit.get();
        ForkJoinPool forkJoinPool2 = new ForkJoinPool(3);
        ForkJoinTask<?> submit1 = forkJoinPool2.submit(() -> {
            IntStream.range(0, 3).parallel().forEach((number) -> {
                try {
                    System.out.println(Thread.currentThread().getName());
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            });
        });
        submit1.get();

    }

}
