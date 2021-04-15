package Access2.accessModel;

import Access2.accessModel.ShortestPath.Dijkstra.DijkstraPathFinder;
import Access2.accessModel.ShortestPath.Dijkstra.DijkstraResult;
import Access2.accessModel.dataPoint.DataPoint;
import Access2.accessModel.dataPoint.ResourcePoint;
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
                        double roadDis = dijkstraResult.nodeMap.get(demandPoint.closestNode).cost;
                        double realDis = realDis(roadDis, resourcePoint, demandPoint);
                        DemandPackage demandPackage = new DemandPackage(realDis, resourcePoint);
                        demandPoint.demandPackages.add(demandPackage);
                    });

            // 计算区域竞争指数
            resourcePoint.competeFactor = dataBox.popPoints.stream()
                    .filter(popPoint -> dijkstraResult.graph.nodes.contains(popPoint.closestNode))
                    .mapToDouble(popPoint -> {
                        double popNum = popPoint.popNum;
                        double roadDis = dijkstraResult.nodeMap.get(popPoint.closestNode).cost;
                        double realDis = realDis(roadDis, resourcePoint, popPoint);
                        return regionalCompleteFun.completeFactor(popNum, realDis, dampingFun, dis0);
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

    private double realDis(double roadDis, ResourcePoint resourcePoint, DataPoint dataPoint){
        return roadDis + resourcePoint.connDis + dataPoint.connDis;
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
