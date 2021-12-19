package Access2.accessModel.strategy.FunFactory;

import Access2.accessModel.strategy.DampingFun;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DampingFunFactory {

    public static DampingFun expDampingFun(int factor){
        return (d,d0)->{
            double x = d/d0;
            return Math.exp(-x * factor);
        };
    }

    public static DampingFun piecewiseConstantDamping(Map<Double, Double> disFactorMap){
        List<Double> disList = disFactorMap.keySet().stream()
                .sorted(Double::compareTo)
                .collect(Collectors.toList());
        return (d,d0)->{
            for (double dis : disList) {
                if (dis >= d) {
                    return disFactorMap.get(dis);
                }
            }
            return 0;
        };
    }
}
