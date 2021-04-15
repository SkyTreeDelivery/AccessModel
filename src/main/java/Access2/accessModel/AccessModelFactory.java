package Access2.accessModel;

import Access2.accessModel.strategy.AggregationFun;
import Access2.accessModel.strategy.DampingFun;
import Access2.accessModel.strategy.FunFactory.DampingFunFactory;
import Access2.accessModel.strategy.Imp.AccessFunImp;
import Access2.accessModel.strategy.Imp.DampingFunImp;
import Access2.accessModel.strategy.Imp.RealAttractiveFunImp;
import Access2.accessModel.strategy.Imp.RegionalCompleteFunImp;
import Access2.graph.Graph;

import java.util.Map;

public class AccessModelFactory {
    public static AccessModel opportunity_accumulate_basic(Graph graph, DataBox dataBox,
                                                           double dis0, AggregationFun aggregationFun) {
        return new AccessModel(graph, dataBox,
                DampingFunImp.noDamp,
                RegionalCompleteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompleteFactor,
                AccessFunImp.opportunity_accumulate_basic,
                aggregationFun,
                dis0
        );
    }

    public static AccessModel opportunity_accumulate(Graph graph, DataBox dataBox,
                                                     Map<Double, Double> disFactorMap,
                                                     double maxDis0, AggregationFun aggregationFun) {
        return new AccessModel(graph, dataBox,
                DampingFunFactory.piecewiseConstantDamping(disFactorMap),
                RegionalCompleteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompleteFactor,
                AccessFunImp.opportunity_accumulate,
                aggregationFun,
                maxDis0
        );
    }

    public static AccessModel gravity_basic(Graph graph, DataBox dataBox, DampingFun dampingFun, AggregationFun aggregationFun) {
        return new AccessModel(graph, dataBox,
                dampingFun,
                RegionalCompleteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompleteFactor,
                AccessFunImp.gravity,
                aggregationFun,
                -1
        );
    }

    public static AccessModel gravity(Graph graph, DataBox dataBox, DampingFun dampingFun, AggregationFun aggregationFun) {
        return new AccessModel(graph, dataBox,
                dampingFun,
                RegionalCompleteFunImp.hasDamping_noDis0,
                RealAttractiveFunImp.hasCompleteFactor,
                AccessFunImp.gravity,
                aggregationFun,
                -1
        );
    }

    public static AccessModel kernel_basic(Graph graph, DataBox dataBox, DampingFun dampingFun, double bandWith, AggregationFun aggregationFun) {
        return new AccessModel(graph, dataBox,
                dampingFun,
                RegionalCompleteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompleteFactor,
                AccessFunImp.kernel,
                aggregationFun,
                bandWith
        );
    }

    public static AccessModel kernel(Graph graph, DataBox dataBox, DampingFun dampingFun, double bandWith, AggregationFun aggregationFun) {
        return new AccessModel(graph, dataBox,
                dampingFun,
                RegionalCompleteFunImp.hasDamping_hasDis0,
                RealAttractiveFunImp.hasCompleteFactor,
                AccessFunImp.kernel,
                aggregationFun,
                bandWith
        );
    }

    public static AccessModel twoStepMobile(Graph graph, DataBox dataBox, double dis0, AggregationFun aggregationFun ) {
        return new AccessModel(graph, dataBox,
                DampingFunImp.noDamp,
                RegionalCompleteFunImp.noDamping_hasDis0,
                RealAttractiveFunImp.hasCompleteFactor,
                AccessFunImp.twoStepMobile,
                aggregationFun,
                dis0
        );
    }

    public static AccessModel twoStepMobile_complete(Graph graph, DataBox dataBox, DampingFun dampingFun, double dis0, AggregationFun aggregationFun) {
        return new AccessModel(graph, dataBox,
                dampingFun,
                RegionalCompleteFunImp.noDamping_hasDis0,
                RealAttractiveFunImp.hasCompleteFactor,
                AccessFunImp.twoStepMobile_complete,
                aggregationFun,
                dis0
        );
    }
}
