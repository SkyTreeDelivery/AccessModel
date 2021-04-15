package Access2.accessModel.strategy.FunFactory;

import Access2.accessModel.strategy.DampingFun;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DampingFunFactory {

    public static DampingFun expDampingFun(int factor){
        return (x)->{
            return Math.exp(-x * factor);
        };
    }

    public static DampingFun piecewiseConstantDamping(Map<Double, Double> disFactorMap){
        List<Double> disList = disFactorMap.keySet().stream()
                .sorted(Double::compareTo)
                .collect(Collectors.toList());
        return (x)->{
            int index = 0;
            for (int i = 0; i < disList.size(); i++) {
                if(disList.get(index) >= x){
                    return disFactorMap.get(index) * x;
                }
            }
            return 0;
        };
    }
}
