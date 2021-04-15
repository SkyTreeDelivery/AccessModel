package Access2.accessModel;

import Access2.MathFunc;
import Access2.accessModel.query.IndexFactory;
import Access2.accessModel.query.SpatialQuery;
import Access2.graph.Graph;
import org.locationtech.jts.index.strtree.STRtree;

public class DataBoxHandler {

    public static void attachDataPointByWalk(Graph graph, DataBox dataBox, double walkSpeed) throws Exception {

        STRtree nodeRtree = IndexFactory.generateNodeRtree(graph.nodes);

        dataBox.resourcePoints.parallelStream()
                .forEach(resourcePoint -> {
                    resourcePoint.closestNode = SpatialQuery.searchNearestNode(nodeRtree, resourcePoint);
                    double distance = resourcePoint.point.distance(resourcePoint.closestNode.point);
                    resourcePoint.connDis = MathFunc.meter2Minute(distance, walkSpeed);
                });
        dataBox.popPoints.parallelStream()
                .forEach(popPoint -> {
                    popPoint.closestNode = SpatialQuery.searchNearestNode(nodeRtree, popPoint);
                    double distance = popPoint.point.distance(popPoint.closestNode.point);
                    popPoint.connDis = MathFunc.meter2Minute(distance, walkSpeed);
                });
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
                        case POLYGON:
                        case GRID:
                            demandPoint.closestNode = SpatialQuery.searchNearestNode(nodeRtree, demandPoint);
                            double distance = demandPoint.point.distance(demandPoint.closestNode.point);
                            demandPoint.connDis =  MathFunc.meter2Minute(distance, walkSpeed);
                            break;
                    }
                });
    }
}
