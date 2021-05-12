package Access2.graph;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 *  表示有向图数据结构中的边
 */
public class Edge {

    // Edge ID生成器
    private static int idSerial = 1000 * 1000;
    private final static GeometryFactory geometryFactory = new GeometryFactory();

    // edge的id
    public Integer id;
    // 边的入点
    public Node in;
    // 边的出点
    public Node out;
    // edge对象的权重，在项目中主要指路段的通行时间，也可以指定为其他有效的通行成本
    public double weight;
    // Edge保存的lineString的长度
    public double length;
    // Edge对象对应的SimpleFeature对象，依赖JTS包
    public LineString lineString;

    public Edge(Node source, Node target, double weight, double length, LineString lineString) {
        this(generateId(),source,target,weight,length,lineString);
    }

    public Edge(Integer id, Node source, Node target, double weight, double length, LineString lineString) {
        this.id = id;
        this.in = source;
        this.out = target;
        this.weight = weight;
        this.length = length;
        this.lineString = lineString;
    }

    @Override
    public String toString() {
        return id.toString();
    }


    private static synchronized int generateId(){
        return idSerial++;
    }

    private Coordinate getBreakPoint(Coordinate start, Coordinate end, double scale){
        if(scale < 0 || scale > 1){
            throw new IllegalArgumentException();
        }
        return new Coordinate(start.x + scale * ( end.x - start.x), start.y + scale * ( end.y - start.y));
    }

    /**
     * 深拷贝
     * @param edge
     * @return
     */
    public static Edge deepCopy(Edge edge){
        Node in = Node.deepCopy(edge.in);
        Node out = Node.deepCopy(edge.out);
        Edge newEdge = new Edge(in, out, edge.weight, edge.length,
                geometryFactory.createLineString(edge.lineString.getCoordinates()));
        newEdge.in.outEdges.add(edge);
        newEdge.out.inEdges.add(edge);
        return newEdge;
    }
}
