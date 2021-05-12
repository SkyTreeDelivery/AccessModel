package Access2.accessModel.dataPoint;

import Access2.graph.Node;
import org.locationtech.jts.geom.Point;

/**
 *  数据点抽象类，一个数据点可以表达一个资源点（如一家医院、一所学校）、一个人口点（如一个人口栅格，按行政区统计的行政区中心点）
 *  或一个需求点（如一个道路节点、一个建筑物或一个栅格中心点）
 */
public abstract class DataPoint {
    // 该数据点的位置
    public Point point;

    // 路网上距离point属性最近的节点
    public Node closestNode;

    // 数据点的位置（point）到达最近的节点（closestNode）的通行成本，可以使距离，也可以是通行时间。
    public double connCost;
}
