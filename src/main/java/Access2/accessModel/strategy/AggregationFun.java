package Access2.accessModel.strategy;

@FunctionalInterface
public interface AggregationFun {
    double aggregation(double[] accessArray);
}
