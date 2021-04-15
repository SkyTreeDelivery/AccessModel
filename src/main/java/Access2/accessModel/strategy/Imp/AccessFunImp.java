package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.AccessFun;

public class AccessFunImp {

    public final static AccessFun opportunity_accumulate_basic = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive : 0;
    };

    public final static AccessFun opportunity_accumulate = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive * dampingFun.damping(dis) : 0;
    };

    public final static AccessFun gravity = (dis, realAttractive, dampingFun, dis0) -> {
        return realAttractive * dampingFun.damping(dis);
    };

    public final static AccessFun kernel = (dis, realAttractive, dampingFun, dis0)->{
        return dis <= dis0 ? realAttractive * dampingFun.damping(dis / dis0) : 0;
    };

    public final static AccessFun twoStepMobile = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive : 0;
    };

    public final static AccessFun twoStepMobile_complete = (dis, realAttractive, dampingFun, dis0) -> {
        return dis <= dis0 ? realAttractive * dampingFun.damping(dis) : 0;
    };
}
