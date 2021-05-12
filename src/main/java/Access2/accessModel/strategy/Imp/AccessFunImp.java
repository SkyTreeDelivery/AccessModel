package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.AccessFun;

public class AccessFunImp {
    // 机会积累法的基本方法
    public final static AccessFun opportunity_accumulate_basic = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive : 0;
    };

    // 机会积累法的扩展方法，考虑距离衰减效应
    public final static AccessFun opportunity_accumulate = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive * dampingFun.damping(dis) : 0;
    };

    // 引力模型法
    public final static AccessFun gravity = (dis, realAttractive, dampingFun, dis0) -> {
        return realAttractive * dampingFun.damping(dis);
    };

    // 核密度法
    public final static AccessFun kernel = (dis, realAttractive, dampingFun, dis0)->{
        return dis <= dis0 ? realAttractive * dampingFun.damping(dis / dis0) : 0;
    };

    // 两步移动搜索法的基本模型
    public final static AccessFun twoStepMobile = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive : 0;
    };

    // 两步移动法的扩展方法，考虑距离衰减效应
    public final static AccessFun twoStepMobile_complete = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive * dampingFun.damping(dis) : 0;
    };
}
