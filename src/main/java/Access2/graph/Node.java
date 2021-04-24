package Access2.graph;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private static int idSerial = 1000*1000;
    public Integer id;
    public Point point;
    public List<Edge> inEdges;
    public List<Edge> outEdges;

    public Node() {
        this(generateId(),null);
    }

    public Node(Integer id) {
        this(id,null);
    }

    public Node(Integer id, double x, double y) {
        this(id,geometryFactory.createPoint(new Coordinate(x,y)));
    }

    public Node(Point point) {
        this(generateId(),point);
    }

    public Node(Integer id, Point point) {
        this.id = id;
        this.point = point;
        inEdges = new ArrayList<Edge>();
        outEdges = new ArrayList<Edge>();
        idSerial++;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static synchronized int generateId(){
        return idSerial++;
    }

    public static Node deepCopy(Node node){
        return new Node(geometryFactory.createPoint(node.point.getCoordinate()));
    }
}
