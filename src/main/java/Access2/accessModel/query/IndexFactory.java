package Access2.accessModel.query;

import Access2.graph.Edge;
import Access2.graph.Node;
import Access2.accessModel.dataPoint.DataPoint;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.Set;

/**
 * 索引工厂
 */
public class IndexFactory {
    /**
     * 为数据点集合建立索引
     * @param dataPoints
     * @param <T>
     * @return
     */
    public static <T extends DataPoint> STRtree generateDataPointRtree(Set<T> dataPoints){
        STRtree stRtree = new STRtree(dataPoints.size());
        dataPoints.forEach(dataPoint ->{
            stRtree.insert(dataPoint.point.getEnvelopeInternal(), dataPoint);
        } );
        stRtree.build();
        return stRtree;
    }

    /**
     * 为节点集合建立索引
     * @param nodes
     * @return
     */
    public static STRtree generateNodeRtree(Set<Node> nodes){
        STRtree stRtree = new STRtree(nodes.size());
        nodes.forEach(node -> stRtree.insert(node.point.getEnvelopeInternal(),node));
        stRtree.build();
        return stRtree;
    }

    /**
     * 为边集合建立索引
     * @param edges
     * @return
     */
    public static STRtree generateEdgeRtree(Set<Edge> edges){
        STRtree stRtree = new STRtree(edges.size());
        edges.forEach(edge -> stRtree.insert(edge.lineString.getEnvelopeInternal(),edge));
        stRtree.build();
        return stRtree;
    }
}
