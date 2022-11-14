package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.RegionalCompeteFun;

public class RegionalCompeteFunImp {
    // 不考虑距离衰减和距离阻隔
    final public static RegionalCompeteFun noDamping_noDis0 = (pop, distance, dampingFun, dis0) -> pop;

    // 不考虑距离衰减，考虑距离阻隔
    final public static RegionalCompeteFun noDamping_hasDis0 = (pop, distance, dampingFun, dis0) -> distance <= dis0 ? pop : 0;

    // 考虑距离衰减，不考虑距离阻隔
    final public static RegionalCompeteFun hasDamping_noDis0 = (pop, distance, dampingFun, dis0) -> pop * dampingFun.damping(distance, 1.0);

    // 考虑距离衰减和距离阻隔
    final public static RegionalCompeteFun hasDamping_hasDis0 = (pop, distance, dampingFun, dis0) -> distance <= dis0 ? pop * dampingFun.damping(distance , dis0) : 0;
}
