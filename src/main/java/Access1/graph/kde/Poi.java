package Access1.graph.kde;

import Access1.graph.Node;
import org.locationtech.jts.geom.Point;

public class Poi {
    public Point point;
    public double weight;
    public double popCover;
    public Node closestNode;

    public Poi(Point point) {
        this(point,null,0.0);
    }

    public Poi(Point point, Node closestNode, double weight) {
        this.point = point;
        this.weight = weight;
        this.closestNode = closestNode;
    }
}
