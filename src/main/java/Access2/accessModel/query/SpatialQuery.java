package Access2.accessModel.query;

import Access2.graph.Edge;
import Access2.graph.Node;
import Access2.accessModel.dataPoint.DataPoint;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行空间查询
 */
public class SpatialQuery {

    // 统计变量
    public static double queryTime = 0;
    public static double traverseTime = 0;
    public static int queryNum = 0;
    public static int traverseNum = 0;

    /**
     * 搜索最近的节点，并统计各步骤的执行时间和循环次数
     * @param stRtree 保存节点的空间索引
     * @param dataPoint 目标数据点
     * @param searchDistance 初始搜索半径
     * @return
     */
    public static Node searchNearestNodeTest(STRtree stRtree, DataPoint dataPoint, double searchDistance){
        Point targetGeom = dataPoint.point;
        if(targetGeom.isEmpty()){
            throw new IllegalArgumentException("geom不能为空");
        }
        Envelope search = targetGeom.getEnvelopeInternal();
        List<Node> result;
        // 查找出待处理节点。
        LocalDateTime start = LocalDateTime.now();
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            queryNum++;
            if (result.size() == 0) {
                searchDistance *= 2;
            } else {
                break;
            }
        }
        LocalDateTime end = LocalDateTime.now();
        queryTime += Duration.between(start,end).toMillis();
        // 找到最近的节点
        start = LocalDateTime.now();
        Node closestPoint = result.get(0);
        double distance = targetGeom.distance(closestPoint.point);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).point);
            if (distance > d) {
                distance = d;
                closestPoint = result.get(i);
            }
            traverseNum++;
        }
        end = LocalDateTime.now();
        traverseTime += Duration.between(start,end).toMillis();
        return closestPoint;
    }

    /**
     * 搜索最近的节点
     * @param stRtree 保存节点的空间索引
     * @param dataPoint 目标数据点
     * @param searchDistance 初始搜索半径
     * @return
     */
    public static Node searchNearestNode(STRtree stRtree, DataPoint dataPoint, double searchDistance){
        Point targetGeom = dataPoint.point;
        if(targetGeom.isEmpty()){
            throw new IllegalArgumentException("geom不能为空");
        }
        Envelope search = targetGeom.getEnvelopeInternal();
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
        Node closestPoint = result.get(0);
        double distance = targetGeom.distance(closestPoint.point);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).point);
            if (distance > d) {
                distance = d;
                closestPoint = result.get(i);
            }
        }
        return closestPoint;
    }

    /**
     * 搜索最近的边
     * @param stRtree 保存边的空间索引
     * @param dataPoint 目标数据点
     * @param searchDistance 初始搜索半径
     * @return
     */
    public static Edge searchClosestEdge(STRtree stRtree, DataPoint dataPoint, double searchDistance) {
        Point targetGeom = dataPoint.point;
        Envelope search = targetGeom.getEnvelopeInternal();
        // 搜索半径，设置合适的搜索半径可以大幅提高执行速度。因为空间查询是一个比较耗时的操作
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
        Edge closestPoint = result.get(0);
        double distance = targetGeom.distance(closestPoint.lineString);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).lineString);
            if (distance > d) {
                distance = d;
                closestPoint = result.get(i);
            }
        }
        return closestPoint;
    }

    /**
     * 初始化空间查询的统计变量
     */
    public static void initTimeStatistics (){
        queryTime = 0;
        queryNum = 0;
        traverseTime = 0;
        traverseNum = 0;
    }
    
}
