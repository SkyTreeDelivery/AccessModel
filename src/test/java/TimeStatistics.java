import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimeStatistics {
    public static void timeStatistics(Runnable testObject, int testSum){
        if(testObject == null){
            return;
        }
        double executedTime = IntStream.range(0, testSum).map(i -> {
            LocalDateTime start = LocalDateTime.now();
            testObject.run();
            LocalDateTime after = LocalDateTime.now();
            return (int) Duration.between(start, after).toMillis();
        }).average().orElse(-1);
        System.out.println("执行[" + testSum +"]次的平均执行时间:" + executedTime + "ms");
    }

    /**
     * 1. IntStream的range函数不包含end，而rangeClosed函数包含end
     * 2. boxed函数将基本数据类型装箱。装箱后会被转换为普通流。
     * 3. 如果不装箱，那么在处理的过程中需要一直保持数据类型为int，装箱后无此限制
     */
    @Test
    public void intStreamTest(){
        List<Integer> collect = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        int[] ints = IntStream.rangeClosed(0, 10).toArray();
        System.out.println(collect);
        System.out.println(Arrays.toString(ints));
    }
}
