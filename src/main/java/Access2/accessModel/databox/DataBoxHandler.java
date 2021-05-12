package Access2.accessModel.databox;

import Access2.MathFunc;
import Access2.accessModel.query.IndexFactory;
import Access2.accessModel.query.SpatialQuery;
import Access2.graph.Graph;
import org.locationtech.jts.index.strtree.STRtree;

import java.time.Duration;
import java.time.LocalDateTime;

public class DataBoxHandler {

    /**
     * 搜索距离数据点最近的路网节点
     * @param graph 图数据结构
     * @param dataBox 数据点
     * @param resourceSearchDistance 资源点搜索半径
     * @param demandSearchDistance 需求点搜索半径
     * @param popSearchDistance 人口点搜索半径
     * @param walkSpeed 步行速度
     * @throws Exception
     */
    public static void attachDataPoint(Graph graph, DataBox dataBox, double resourceSearchDistance,
            double demandSearchDistance, double popSearchDistance, double walkSpeed) throws Exception {

        STRtree nodeRtree = IndexFactory.generateNodeRtree(graph.nodes);

        LocalDateTime before = LocalDateTime.now();
        LocalDateTime after = LocalDateTime.now();

        before = LocalDateTime.now();
        dataBox.resourcePoints.parallelStream()
                .forEach(resourcePoint -> {
                    resourcePoint.closestNode = SpatialQuery.searchNearestNode(nodeRtree, resourcePoint,resourceSearchDistance);
                    double distance = resourcePoint.point.distance(resourcePoint.closestNode.point);
                    resourcePoint.connCost = MathFunc.meter2Minute(distance, walkSpeed);
                });
        after = LocalDateTime.now();
        System.out.println("step2.1 | resourcePoints附着耗时：" + Duration.between(before,after).toMillis() + "ms");

        before = LocalDateTime.now();
        dataBox.demandPoints.parallelStream()
                .forEach(demandPoint -> {
                    switch (demandPoint.type){
                        // Node的最近的节点为node自己，连接成本为0
                        case NODE:
                            // Edge的最近的节点为edge的入点，连接成本为道路通行成本的一半
                        case EDGE:
                            break;
                        // 如果是从Pop得到的栅格，则不重新附着，以免耗时
                        case GRID_FROM_POP:
                            // Polygon和GIRD需要计算最近的节点
                            demandPoint.closestNode = SpatialQuery.searchNearestNode(nodeRtree, demandPoint, demandSearchDistance);
                            double distance = demandPoint.point.distance(demandPoint.closestNode.point);
                            demandPoint.connCost =  MathFunc.meter2Minute(distance, walkSpeed);
                            break;
                        case POLYGON:
                        case GRID:
                            demandPoint.closestNode = SpatialQuery.searchNearestNode(nodeRtree, demandPoint, demandSearchDistance);
                            distance = demandPoint.point.distance(demandPoint.closestNode.point);
                            demandPoint.connCost =  MathFunc.meter2Minute(distance, walkSpeed);
                            break;
                    }
                });
        after = LocalDateTime.now();
        System.out.println("step2.2 | demandPoints附着耗时：" + Duration.between(before,after).toMillis() + "ms");

        before = LocalDateTime.now();
        dataBox.popPoints.parallelStream()
                .forEach(popPoint -> {
                    popPoint.closestNode = SpatialQuery.searchNearestNode(nodeRtree, popPoint,popSearchDistance);
                    double distance = popPoint.point.distance(popPoint.closestNode.point);
                    popPoint.connCost = MathFunc.meter2Minute(distance, walkSpeed);
                });
        after = LocalDateTime.now();
        System.out.println("step2.3 | popPoints附着耗时：" + Duration.between(before,after).toMillis() + "ms");
//        System.out.println("空间查询耗时:" + SpatialQuery.queryTime / 1000 + "s");
//        System.out.println("空间查询次数:" + SpatialQuery.queryNum * 1.0 / 10_000 + "w次");
//        System.out.println("寻找最近的点耗时:" + SpatialQuery.traverseTime / 1000 + "s");
//        System.out.println("寻找最近的点遍历次数:" + SpatialQuery.traverseNum * 1.0 / 10_000 + "w次");
        System.out.println("pop附着总时间:" + Duration.between(before,after).toMillis() * 1.0 / 1000 + "s");
        SpatialQuery.initTimeStatistics();
    }
}
