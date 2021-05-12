package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.RegionalCompleteFun;

public class RegionalCompleteFunImp {
    // 不考虑距离衰减和距离阻隔
    final public static RegionalCompleteFun noDamping_noDis0 = (pop, distance, dampingFun, dis0) -> {
        return pop;
    };

    // 不考虑距离衰减，考虑距离阻隔
    final public static RegionalCompleteFun noDamping_hasDis0 = (pop, distance, dampingFun, dis0) -> {
        return distance <= dis0 ? pop : 0;
    };

    // 考虑距离衰减，不考虑距离阻隔
    final public static RegionalCompleteFun hasDamping_noDis0 = (pop, distance, dampingFun, dis0) -> {
        return pop * dampingFun.damping(distance);
    };

    // 考虑距离衰减和距离阻隔
    final public static RegionalCompleteFun hasDamping_hasDis0 = (pop, distance, dampingFun, dis0) -> {
        return distance <= dis0 ? pop * dampingFun.damping(distance / dis0) : 0;
    };
}
