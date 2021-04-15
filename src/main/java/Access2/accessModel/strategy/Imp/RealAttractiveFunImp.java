package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.RealAttractiveFun;

public class RealAttractiveFunImp {
    final public static RealAttractiveFun noCompleteFactor = (resource, completeFactor) -> {
        return resource;
    };

    final public static RealAttractiveFun hasCompleteFactor = (resource, completeFactor) -> {
        return resource / completeFactor;
    };
}
