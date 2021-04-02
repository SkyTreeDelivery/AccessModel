import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamTest {
    @Test
    public void toMapTest(){
        TimeStatistics.timeStatistics(()->{
            Stream.iterate(1, i -> i + 1).limit(1000 * 1000)
                    .collect(Collectors.toMap(num -> num, num -> num))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        },10);
    }


    // 一个大的ArrayList，内部是随机的整形数据
    volatile List<Integer> integers = Stream.iterate(0,i -> i += 1).limit(1000 * 1000).collect(Collectors.toList());

    // 基准测试1
    @Test
    public void forEachLoopMaxInteger() {
        TimeStatistics.timeStatistics(()->{
            int max = Integer.MIN_VALUE;
            for (int n : integers) {
                max = Integer.max(max, n);
            }
        },10);
    }

    // 基准测试2
    @Test
    public void lambdaMaxInteger() {
        TimeStatistics.timeStatistics(() ->{
                integers.stream().reduce(Integer.MIN_VALUE, (a, b) -> Integer.max(a, b));
        },1);

        TimeStatistics.timeStatistics(() ->{
            integers.stream().collect(Collectors.summingInt(e->e));
        },1);
    }

    @Test
    public void autoBoxingTest(){
        TimeStatistics.timeStatistics(() ->{
            Random random = new Random(1000);
            random.doubles().limit(1000 * 1000 * 100).boxed().collect(Collectors.summingDouble(num -> num));
        },1);
        Random random = new Random(1000);
        double[] doubles = random.doubles().limit(1000 * 1000).toArray();
        // 包装类List
        List<Double> doubleList = random.doubles().limit(1000 * 1000).boxed().collect(Collectors.toList());

        // 方法1：速度最快的求和方法，使用数组
        TimeStatistics.timeStatistics(() ->{
            double sum = 0;
            for (int i = 0; i < doubles.length; i++) {
                sum += doubles[i];
            }
        },1);

        // 方法2：速度第二块的方法，时间复杂度O(n)，对于每个元素执行1次拆箱
        TimeStatistics.timeStatistics(() ->{
            double sum = 0;
            for (int i = 0; i < doubleList.size(); i++) {
                sum += doubleList.get(i);
            }
        },1);

        // 方法3：速度最慢的方法，时间复杂度O(3n)，对于每个元素执行3次拆箱
        TimeStatistics.timeStatistics(() ->{
            Double sum = 0.0;
            for (int i = 0; i < doubleList.size(); i++) {
                sum += doubleList.get(i);
            }
        },1);

        // 方法4：速度和方法2一直，可以推测内部使用了方法二进行求和计算
        TimeStatistics.timeStatistics(() ->{
            doubleList.stream().collect(Collectors.summingDouble(sum -> sum));
        },1);

    }

    @Test
    public void toCollectionTest(){
        LinkedList<Integer> collect = Stream.iterate(0, i -> i += 1).limit(1000 * 1000)
                .collect(Collectors.toCollection(LinkedList::new));
        System.out.println(collect.getClass());
    }

    @Test
    public void toMapTest1(){
        Map<Integer, Integer> collect = Stream.iterate(0, i -> i += 1).limit(1000 * 1000)
                .collect(Collectors.toMap(num -> num, num -> num, (a, b) -> a > b ? a : b));
        System.out.println(collect.getClass());
    }

    /**
     * 在调用toMap搜集器时，Collectors包会使用默认的mergeFunction，而在此mergeFunction中，会抛出IllegalStateException异常
     * 也就是说，如果提供了重复的key，必须提供一个自定义的mergeFunction，处理重复的key产生的冲突
     */
    @Test
    public void toMapTest2(){
        Map<Integer, Integer> collect = Stream.iterate(0, i -> i += 1).limit(100)
                .collect(Collectors.toMap(num -> num / 2, num -> num, (a, b) -> a < b ? a : b));
        System.out.println(collect);
    }

    /**
     * 如果在流处理的过程中，流中出现了null，则后续使用null元素对方法的调用会抛出NLP异常。
     */
    @Test
    public void voidTest(){
        IntStream.range(0,100).boxed().map(i -> i < 50 ? i : null).map(i -> i.toString()).collect(Collectors.toList());
    }

    @Test
    public void filterTest(){
        List<Integer> collect = IntStream.range(0, 1000 * 1000).boxed().collect(Collectors.toList());
        Integer target = collect.get(669977);
        Set<Integer> set = collect.stream().collect(Collectors.toSet());
        TimeStatistics.timeStatistics(()->{
            set.stream().filter(e-> e!= 669977)
                    .collect(Collectors.toMap(e->e,e->e));
        },1);
        TimeStatistics.timeStatistics(()->{
            set.remove(target);
            set.stream()
                    .collect(Collectors.toMap(e->e,e->e));
        },1);

    }

    @Test
    public void arrayTest(){
        int[] array = new int[9];
        for (int i = 0; i < 10; i++) {
            array[i] = i;
        }
        System.out.println(Arrays.toString(array));
    }

    @Test
    /**
     * java 中
     */
    public void arrayInitTest(){
        double[] array = new double[10];
        System.out.println(Arrays.toString(array));
    }
}
