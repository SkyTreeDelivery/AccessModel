package Access2.accessModel.strategy;

@FunctionalInterface
public interface AggregationFun {
    /**
     * 聚合函数，将可达性计算结果聚合为最终的估计值，可以求和、求均值、求最大值、求最小值等。
     * @param accessArray 待聚合的可达性估值集合
     * @return 最终可达性估值
     */
    double aggregation(double[] accessArray);
}
