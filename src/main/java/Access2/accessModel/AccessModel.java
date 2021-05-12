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
    public final static double DEFAULT_WALK_SPEED = 5;

    public final Graph graph;
    public final DataBox dataBox;
    private final double dis0;

    // 该可达性模型选择的策略
    private final DampingFun dampingFun;
    private final RegionalCompleteFun regionalCompleteFun;
    private final RealAttractiveFun realAttractiveFun;
    private final AccessFun accessFun;
    private final AggregationFun aggregationFun;

    public AccessModel(Graph graph, DataBox dataBox, DampingFun dampingFun, RegionalCompleteFun regionalCompleteFun, RealAttractiveFun realAttractiveFun, AccessFun accessFun, AggregationFun aggregationFun, double dis0) {
        this.graph = graph;
        this.dataBox = dataBox;
        this.dampingFun = dampingFun;
        this.regionalCompleteFun = regionalCompleteFun;
        this.realAttractiveFun = realAttractiveFun;
        this.accessFun = accessFun;
        this.aggregationFun = aggregationFun;
        this.dis0 = dis0;
    }

    public void calculate(){

        // 计算最短路径
        dataBox.resourcePoints.parallelStream().forEach(resourcePoint -> {
            // 求得最短路径
            DijkstraPathFinder dijkstraPathFinder = new DijkstraPathFinder(graph, resourcePoint, dis0);
            DijkstraResult dijkstraResult = dijkstraPathFinder.calculate();

            // 将相关数据分配给需求点
            dataBox.demandPoints.parallelStream()
                    .filter(demandPoint -> dijkstraResult.graph.nodes.contains(demandPoint.closestNode))
                    .forEach(demandPoint -> {
                        double realDis = realDis(resourcePoint, demandPoint, dijkstraResult);
                        demandPoint.demandPackages.add(new DemandPackage(realDis, resourcePoint));
                    });

            // 计算区域竞争指数
            resourcePoint.competeFactor = dataBox.popPoints.stream()
                    .filter(popPoint -> dijkstraResult.graph.nodes.contains(popPoint.closestNode))
                    .mapToDouble(popPoint -> {
                        double realDis = realDis(resourcePoint, popPoint, dijkstraResult);
                        return regionalCompleteFun.completeFactor(popPoint.popNum, realDis, dampingFun, dis0);
                    }).sum();

            // 计算实际吸引力
            resourcePoint.realAttractive = realAttractiveFun.realAttractive(resourcePoint.resourceWeight,resourcePoint.competeFactor);
        });

        dataBox.demandPoints.parallelStream().forEach(demandPoint -> {
            List<DemandPackage> demandPackages = demandPoint.demandPackages;
            double[] accessArray = demandPackages.stream()
                    .mapToDouble(demandPackage ->
                            accessFun.access(demandPackage.dis, demandPackage.resourcePoint.realAttractive, dampingFun, dis0))
                    .toArray();
            demandPoint.access = aggregationFun.aggregation(accessArray);
        });
    }

    private double realDis(ResourcePoint resourcePoint, DataPoint demandPoint, DijkstraResult dijkstraResult){
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
