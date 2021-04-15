package Access1.graph;

import Access1.graph.kde.Poi;
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
    public List<Access1.graph.Edge> inEdges;
    public List<Access1.graph.Edge> outEdges;

    // TODO 加权
    public double[] costDisArray;
    public Poi[] pois;
    public int nNum;

    public Node(Integer id) {
        this(id,null);
    }

    public Node(Integer id, double x, double y) {
        this(id,geometryFactory.createPoint(new Coordinate(x,y)));
    }

    public Node(Integer id, Point point) {
        this.id = id;
        this.point = point;
        inEdges = new ArrayList<Access1.graph.Edge>();
        outEdges = new ArrayList<Edge>();
        idSerial++;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static int generateId(){
        return idSerial++;
    }
}
