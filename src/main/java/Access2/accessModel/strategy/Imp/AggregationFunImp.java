package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.AggregationFun;

import java.util.Arrays;

public class AggregationFunImp {

    // 求和
    final public static AggregationFun sum = (array)-> Arrays.stream(array).sum();

    // 求最大值
    final public static AggregationFun max = (array)-> Arrays.stream(array).max().orElse(0);

    // 求最小值
    final public static AggregationFun min = (array)-> Arrays.stream(array).min().orElse(0);

    // 求平均值
    final public static AggregationFun average = (array)-> Arrays.stream(array).average().orElse(0);
}
