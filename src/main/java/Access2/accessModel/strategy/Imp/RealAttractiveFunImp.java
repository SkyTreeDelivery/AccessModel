package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.RealAttractiveFun;

public class RealAttractiveFunImp {
    // 不考虑区域竞争
    final public static RealAttractiveFun noCompleteFactor = (resource, completeFactor) -> {
        return resource;
    };

    // 考虑区域竞争
    final public static RealAttractiveFun hasCompleteFactor = (resource, completeFactor) -> {
        return resource / completeFactor;
    };
}
