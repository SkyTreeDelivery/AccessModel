package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.RegionalCompleteFun;

public class RegionalCompleteFunImp {
    final public static RegionalCompleteFun noDamping_noDis0 = (pop, distance, dampingFun, dis0) -> {
        return pop;
    };

    final public static RegionalCompleteFun noDamping_hasDis0 = (pop, distance, dampingFun, dis0) -> {
        return distance <= dis0 ? pop : 0;
    };

    final public static RegionalCompleteFun hasDamping_noDis0 = (pop, distance, dampingFun, dis0) -> {
        return pop * dampingFun.damping(distance);
    };

    final public static RegionalCompleteFun hasDamping_hasDis0 = (pop, distance, dampingFun, dis0) -> {
        return distance <= dis0 ? pop * dampingFun.damping(distance / dis0) : 0;
    };
}
