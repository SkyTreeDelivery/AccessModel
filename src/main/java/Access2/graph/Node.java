package Access2.graph;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class Node {

    // Node ID生成器
    private static int idSerial = 1000*1000;
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    // Node id
    public Integer id;

    // node的位置
    public Point point;

    // 入边集合
    public List<Edge> inEdges;

    // 出边集合
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
