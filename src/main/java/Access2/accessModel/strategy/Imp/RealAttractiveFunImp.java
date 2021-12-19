package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.RealAttractiveFun;

public class RealAttractiveFunImp {
    // 不考虑区域竞争
    final public static RealAttractiveFun noCompeteFactor = (resource, completeFactor) -> resource;

    // 考虑区域竞争
    final public static RealAttractiveFun hasCompeteFactor = (resource, completeFactor) -> resource / completeFactor;
}
