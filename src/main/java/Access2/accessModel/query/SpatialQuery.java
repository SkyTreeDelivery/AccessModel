package Access2.accessModel.query;

import Access2.graph.Edge;
import Access2.graph.Node;
import Access2.accessModel.dataPoint.DataPoint;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.List;

public class SpatialQuery {
    
    public static Node searchNearestNode(STRtree stRtree, DataPoint dataPoint){
        Point targetGeom = dataPoint.point;
        Envelope search = targetGeom.getEnvelopeInternal();
        double searchDistance = 1600;
        List<Node> result;
        // 查找出待处理节点。
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
            } else {
                break;
            }
        }
        // 找到最近的节点
        Node clostestPoint = result.get(0);
        double distance = targetGeom.distance(clostestPoint.point);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).point);
            if (distance > d) {
                distance = d;
                clostestPoint = result.get(i);
            }
        }
        return clostestPoint;
    }

    public static Edge searchClosestEdge(STRtree stRtree, DataPoint dataPoint) {
        Point targetGeom = dataPoint.point;
        Envelope search = targetGeom.getEnvelopeInternal();
        // 搜索半径，设置合适的搜索半径可以大幅提高执行速度。因为空间查询是一个比较耗时的操作
        double searchDistance = 1600;
        List<Edge> result;
        // 查找出待处理节点。
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
//                if(searchDistance > 3200){
//                    return null;
//                }
            } else {
                break;
            }
        }
        // 找到最近的节点
        Edge clostestPoint = result.get(0);
        double distance = targetGeom.distance(clostestPoint.lineString);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).lineString);
            if (distance > d) {
                distance = d;
                clostestPoint = result.get(i);
            }
        }
        return clostestPoint;
    }
    
}
