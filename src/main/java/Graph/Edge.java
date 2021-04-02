package Graph;

import Graph.kde.Poi;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class Edge {

    // Edge ID生成器
    private static int idSerial = 1000 * 1000;

    public Integer id;
    public Node in;
    public Node out;
    // edge对象的权重，在项目中主要指路段的通行时间，也可以指定为其他有效的通行成本
    public double weight;
    // Edge保存的lineString的长度
    public double length;
    // Edge对象对应的SimpleFeature对象，依赖JTS包
    public LineString lineString;
    public List<Edge> subEdges;
    public double[] subEdgeCostDisArray;
    public Poi[] pois;
    public int eNum;
    public double pop;

    // 子边序号，从0开始计数
    public Integer subEdgeOrder;

    // 父边引用
    public Edge fatherEdge;

    public boolean isTop;

    // subEdge的中心计算系数
    public double childCenterCoefficient;

    public Edge(Integer id, Node source, Node target, double weight,double length, LineString lineString) {
        this.id = id;
        this.in = source;
        this.out = target;
        this.weight = weight;
        this.length = length;
        subEdges = new ArrayList<>();
        this.lineString = lineString;
    }

    public Edge(Integer id, Node in, Node out, double weight, double length, LineString lineString,
                Integer subEdgeOrder, Edge fatherEdge, double childCenterCoefficient) {
        this(id,in,out,weight,length,lineString);
        this.subEdgeOrder = subEdgeOrder;
        this.fatherEdge = fatherEdge;
        this.childCenterCoefficient = childCenterCoefficient;
    }

    public void splitEdge(double targetLength){
        // 如果小于阈值，则不作拆分，将整个Edge对象保存为一个subEdge对象
        if(length < targetLength * 1.5){
            subEdges.add(new Edge(generateId(), in, out, weight, length,lineString,subEdges.size(), this,0.5));
            return;
        }

        //计算拆分后subEdge的长度
        long num = Math.round(length / targetLength);
        double newWeight = weight / num;
        double subEdgeLength = length / num;
        GeometryFactory geometryFactory = new GeometryFactory();

        // 从Edge对象中拆分出SubEdge对象数组
        // 初始化参数
        int serial = 1;
        Node in = this.in;
        Coordinate startP = lineString.getCoordinateN(0);
        ArrayList<Coordinate> coos = new ArrayList<>();
        coos.add(startP);

        // 生成num个subEdge对象
        for (int i = 1; i < num; i++) {
            Coordinate endP = lineString.getCoordinateN(serial);
            LineSegment lineSegment = new LineSegment(startP, endP);
            double segLength = lineSegment.getLength();
            double surplus = subEdgeLength - segLength;
            // 如果已经找到断点所在的segment，则生成SubEdge对象
            while(surplus > 0){
                coos.add(endP);
                serial++;
                startP = endP;
                endP = lineString.getCoordinateN(serial);
                lineSegment = new LineSegment(startP, endP);
                segLength = lineSegment.getLength();
                surplus -= segLength;
            }
            // 计算断点
            Coordinate breakP = getBreakPoint(startP, endP, (surplus + segLength) / segLength);
            Node out = new Node(Node.generateId(),geometryFactory.createPoint(breakP));
            coos.add(breakP);

            // 生成新的lineString对象和Edge对象
            LineString newLineString = geometryFactory.createLineString(coos.toArray(new Coordinate[0]));
            double childCenterCo = (2 * i - 1) * 1.0 / (2 * num);
            Edge subEdge = new Edge(generateId(), in, out, newWeight, newLineString.getLength(),newLineString ,subEdges.size(), this,childCenterCo);
            subEdges.add(subEdge);

            // 初始化部分参数
            coos.clear();
            coos.add(breakP);
            // 更新下一轮使用的参数
            startP = breakP;
            in = out;
        }

        // 处理最后一个节点
        for (int i = serial; i < lineString.getNumPoints(); i++) {
            coos.add(lineString.getCoordinateN(i));
            // 生成新的lineString对象和Edge对象
            LineString newLineString = geometryFactory.createLineString(coos.toArray(new Coordinate[0]));
            double childCenterCo = (2 * num - 1) * 1.0 / (2 * num);
            Edge subEdge = new Edge(generateId(), in, out, weight / num, newLineString.getLength(),newLineString ,subEdges.size(), this,childCenterCo);
            subEdges.add(subEdge);
        }

    }

    @Override
    public String toString() {
        return id.toString();
    }


    public synchronized int generateId(){
        return idSerial++;
    }

    private Coordinate getBreakPoint(Coordinate start, Coordinate end, double scale){
        if(scale < 0 || scale > 1){
            throw new IllegalArgumentException();
        }
        return new Coordinate(start.x + scale * ( end.x - start.x), start.y + scale * ( end.y - start.y));
    }
}
