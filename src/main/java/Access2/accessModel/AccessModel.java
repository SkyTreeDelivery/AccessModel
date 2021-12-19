package Access2.accessModel;

import Access2.accessModel.ShortestPath.Dijkstra.DijkstraPathFinder;
import Access2.accessModel.ShortestPath.Dijkstra.DijkstraResult;
import Access2.accessModel.dataPoint.DataPoint;
import Access2.accessModel.dataPoint.ResourcePoint;
import Access2.accessModel.databox.DataBox;
import Access2.accessModel.strategy.*;
import Access2.graph.Graph;

import java.util.List;

public class AccessModel {
    // 默认步行速度， 单位： km/h
    public final double walkSpeed;
    public final Graph graph;
    public final DataBox dataBox;
    private final boolean fixD0;
    private final double d0;

    // 该可达性模型选择的策略
    private final DampingFun dampingFun;
    private final RegionalCompeteFun regionalCompeteFun;
    private final RealAttractiveFun realAttractiveFun;
    private final AccessFun accessFun;
    private final AggregationFun aggregationFun;

    public static class Builder {
        private final Graph graph; // 计算最短路径的图
        private final DataBox dataBox; // 数据封装
        private final DampingFun dampingFun; // 距离衰减函数
        private final RegionalCompeteFun regionalCompeteFun; // 区域竞争
        private final RealAttractiveFun realAttractiveFun; // 资源实际吸引力
        private final AccessFun accessFun; // 可达性计算
        private final AggregationFun aggregationFun; // 可达性结果聚合

        private boolean fixD0 = false; // 是否使用固定搜索阈值
        private double d0 = -1; // 若选择固定搜索阈值，则d0会被设置未搜索阈值
        private double walkSpeed = 5; // 行走速度

        public Builder(Graph graph, DataBox dataBox, DampingFun dampingFun, RegionalCompeteFun regionalCompeteFun, RealAttractiveFun realAttractiveFun,AccessFun accessFun, AggregationFun aggregationFun) {
            this.graph = graph;
            this.dataBox = dataBox;
            this.dampingFun = dampingFun;
            this.regionalCompeteFun = regionalCompeteFun;
            this.realAttractiveFun = realAttractiveFun;
            this.accessFun = accessFun;
            this.aggregationFun = aggregationFun;
        }

        public Builder fixD0(boolean fixD0) {
            this.fixD0 = fixD0;
            return this;
        }

        public Builder d0(double d0) {
            this.d0 = d0;
            return this;
        }

        public Builder walkSpeed(double walkSpeed) {
            this.walkSpeed = walkSpeed;
            return this;
        }

        public AccessModel build(){
            return new AccessModel(this);
        }
    }

    private AccessModel(Builder builder) {
        this.graph = builder.graph;
        this.dataBox = builder.dataBox;
        this.dampingFun = builder.dampingFun;
        this.regionalCompeteFun = builder.regionalCompeteFun;
        this.realAttractiveFun = builder.realAttractiveFun;
        this.accessFun = builder.accessFun;
        this.aggregationFun = builder.aggregationFun;
        this.d0 = builder.d0;
        this.fixD0 = builder.fixD0;
        this.walkSpeed = builder.walkSpeed;
    }

    public void calculate() {
        // 1. 计算各个资源点的实际吸引力
        dataBox.resourcePoints.parallelStream().forEach(resourcePoint -> {
            // 以资源点为中心，若采取固定阈值（如核密度分析），则 d0 为搜索阈值，否则资源点的属性 d0 即为搜索阈值，求得处于搜索阈值内部的路网节点图
            DijkstraPathFinder dijkstraPathFinder = new DijkstraPathFinder(graph, resourcePoint, fixD0?d0:resourcePoint.d0);
            DijkstraResult dijkstraResult = dijkstraPathFinder.calculate();

            // 将相关数据分配给需求点
            dataBox.demandPoints.parallelStream()
                    // 判断该需求点最近路网节点是否处于搜索阈值内
                    .filter(demandPoint -> dijkstraResult.graph.nodes.contains(demandPoint.closestNode))
                    // 对于每个处于搜索阈值内的需求点，计算其与资源点的距离
                    .forEach(demandPoint -> {
                        double realDis = realDis(resourcePoint, demandPoint, dijkstraResult);
                        demandPoint.demandPackages.add(new DemandPackage(realDis, resourcePoint));
                    });

            // 计算区域竞争指数
            resourcePoint.competeFactor = dataBox.popPoints.stream()
                    // 同样的，对于人口点，计算人口点最近路网节点是否处于搜索阈值内
                    .filter(popPoint -> dijkstraResult.graph.nodes.contains(popPoint.closestNode))
                    // 对于处于阈值内的人口点，计算竞争因子
                    .mapToDouble(popPoint -> {
                        double realDis = realDis(resourcePoint, popPoint, dijkstraResult);
                        return regionalCompeteFun.completeFactor(popPoint.popNum, realDis, dampingFun, fixD0?d0:resourcePoint.d0);
                    }).sum();

            // 计算实际吸引力
            resourcePoint.realAttractive = realAttractiveFun.realAttractive(resourcePoint.resourceWeight, resourcePoint.competeFactor);
        });

        // 2. 计算各个需求点的可达性
        dataBox.demandPoints.parallelStream().forEach(demandPoint -> {
            // 获取各个需求点可达的所有资源点
            List<DemandPackage> demandPackages = demandPoint.demandPackages;
            // 计算该需求点到每一个资源点的可达性
            double[] accessArray = demandPackages.stream()
                    .mapToDouble(demandPackage ->
                            accessFun.access(demandPackage.dis, demandPackage.resourcePoint.realAttractive, dampingFun, fixD0?d0:demandPackage.resourcePoint.d0))
                    .toArray();
            demandPoint.access = aggregationFun.aggregation(accessArray);
        });
    }

    private double realDis(ResourcePoint resourcePoint, DataPoint demandPoint, DijkstraResult dijkstraResult) {
        double roadDis = dijkstraResult.nodeMap.get(demandPoint.closestNode).cost;
        return roadDis + resourcePoint.connCost + demandPoint.connCost;
    }

    public static class DemandPackage {
        public final double dis;
        public final ResourcePoint resourcePoint;

        private DemandPackage(double dis, ResourcePoint resourcePoint) {
            this.dis = dis;
            this.resourcePoint = resourcePoint;
        }
    }
}
