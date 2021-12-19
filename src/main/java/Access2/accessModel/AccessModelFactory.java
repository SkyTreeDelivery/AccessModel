package Access2.accessModel;

import Access2.accessModel.databox.DataBox;
import Access2.accessModel.strategy.AggregationFun;
import Access2.accessModel.strategy.DampingFun;
import Access2.accessModel.strategy.FunFactory.DampingFunFactory;
import Access2.accessModel.strategy.Imp.AccessFunImp;
import Access2.accessModel.strategy.Imp.DampingFunImp;
import Access2.accessModel.strategy.Imp.RealAttractiveFunImp;
import Access2.accessModel.strategy.Imp.RegionalCompeteFunImp;
import Access2.graph.Graph;

import java.util.Map;

public class AccessModelFactory {
    /**
     * 机会积累法的基础方法，可达性估值为在给定的半径（accumulateDis）内资源点的资源量总和，考虑了距离阻隔效应
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param accumulateDis  积累距离
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel opportunity_accumulate_basic(Graph graph, DataBox dataBox,
                                                           double accumulateDis, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                DampingFunImp.noDamp,
                RegionalCompeteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompeteFactor,
                AccessFunImp.opportunity_accumulate_basic,
                aggregationFun
        ).fixD0(true).d0(accumulateDis).build();
    }

    /**
     * 机会积累法的扩展方法，引入了距离衰减效应，距离资源点越近，资源获取量越大。考虑了距离阻隔效应。
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param disFactorMap   分级组织的积累距离和各距离下的衰减系数
     * @param maxDis0        积累的最大距离，距离资源的距离大于maxDis0，则认为无法获得该资源点的资源
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel opportunity_accumulate(Graph graph, DataBox dataBox,
                                                     Map<Double, Double> disFactorMap,
                                                     double maxDis0, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                DampingFunFactory.piecewiseConstantDamping(disFactorMap),
                RegionalCompeteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompeteFactor,
                AccessFunImp.opportunity_accumulate,
                aggregationFun
        ).fixD0(true).d0(maxDis0).build();
    }

    /**
     * 引力模型法的基本方法，考虑了距离衰减效应
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param dampingFun     衰减函数
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel gravity_basic(Graph graph, DataBox dataBox, DampingFun dampingFun, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                dampingFun,
                RegionalCompeteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompeteFactor,
                AccessFunImp.gravity,
                aggregationFun
        ).fixD0(true).d0(-1).build();
    }

    /**
     * 引力模型法的扩展方法，引入了区域竞争因素，考虑了人口分布对可达性分布的影响，不考虑距离阻隔。
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param dampingFun     衰减函数
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel gravity(Graph graph, DataBox dataBox, DampingFun dampingFun, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                dampingFun,
                RegionalCompeteFunImp.hasDamping_noDis0,
                RealAttractiveFunImp.hasCompeteFactor,
                AccessFunImp.gravity,
                aggregationFun
        ).fixD0(true).d0(-1).build();
    }

    /**
     * 核密度法的基本方法
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param dampingFun     衰减函数
     * @param bandWith       搜索带宽
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel kernel_basic(Graph graph, DataBox dataBox, DampingFun dampingFun, double bandWith, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                dampingFun,
                RegionalCompeteFunImp.noDamping_noDis0,
                RealAttractiveFunImp.noCompeteFactor,
                AccessFunImp.kernel,
                aggregationFun
        ).fixD0(true).d0(bandWith).build();
    }

    /**
     * 核密度法的扩展方法，考虑了人口分布对可达性分布的影响
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param dampingFun     衰减函数
     * @param bandWith       搜索带宽
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel kernel(Graph graph, DataBox dataBox, DampingFun dampingFun, double bandWith, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                dampingFun,
                RegionalCompeteFunImp.hasDamping_hasDis0,
                RealAttractiveFunImp.hasCompeteFactor,
                AccessFunImp.kernel,
                aggregationFun
        ).fixD0(true).d0(bandWith).build();
    }

    /**
     * 两部移动搜索法的基本方法
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param dis0           搜索距离
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel twoStepMobile(Graph graph, DataBox dataBox, double dis0, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                DampingFunImp.noDamp,
                RegionalCompeteFunImp.noDamping_hasDis0,
                RealAttractiveFunImp.hasCompeteFactor,
                AccessFunImp.twoStepMobile,
                aggregationFun
        ).fixD0(true).d0(dis0).build();
    }

    /**
     * 两步移动搜索法的扩展方法
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param dampingFun     衰减函数
     * @param dis0           搜索距离
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel twoStepMobile_complete(Graph graph, DataBox dataBox, DampingFun dampingFun, double dis0, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                dampingFun,
                RegionalCompeteFunImp.hasDamping_hasDis0,
                RealAttractiveFunImp.hasCompeteFactor,
                AccessFunImp.twoStepMobile_complete,
                aggregationFun
        ).fixD0(true).d0(dis0).build();
    }

    /**
     * 两步移动搜索法的扩展方法2
     * <p>
     * 搜索阈值不指定，而是按照资源点的搜索阈值进行设定
     *
     * @param graph          图数据结构
     * @param dataBox        数据点
     * @param dampingFun     衰减函数
     * @param aggregationFun 聚合函数
     * @return 可达性模型
     */
    public static AccessModel twoStepMobile_complete2(Graph graph, DataBox dataBox, DampingFun dampingFun, AggregationFun aggregationFun) {
        return new AccessModel.Builder(graph, dataBox,
                dampingFun,
                RegionalCompeteFunImp.hasDamping_hasDis0,
                RealAttractiveFunImp.hasCompeteFactor,
                AccessFunImp.twoStepMobile_complete,
                aggregationFun
        ).build();
    }
}
