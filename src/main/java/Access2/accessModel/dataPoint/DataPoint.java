package Access2.accessModel.dataPoint;

import Access2.graph.Node;
import org.locationtech.jts.geom.Point;

public abstract class DataPoint {

    public Point point;
    public Node closestNode;
    public double connDis;
}
