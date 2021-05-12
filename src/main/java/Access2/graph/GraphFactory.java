package Access2.graph;

import Access2.utils.GeometryUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphFactory {

    private static final GeometryFactory geometryFactory = new GeometryFactory();
    // 预编译正则表达式
    private static Pattern pattern= Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

    /**
     *  从CSV数据结构中读取数据，生成有向图
     * @param linkCSVPath linkCSV文件路径
     * @param nodeCSVPath nodeCSV文件路径
     * @return
     */
    public static Graph generateGraphFromCSV(String linkCSVPath, String nodeCSVPath){
        WKTReader wktReader = new WKTReader();

        HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>();
        try {
            HashSet<Edge> edges = new HashSet<Edge>();
            HashSet<Node> nodes = new HashSet<Node>();
            // 生成node对象。
            Stream<String> nodeStream = Files.lines(Paths.get(nodeCSVPath));
            final int[] i = {0};
            nodeStream.skip(1).forEach(line->{
                String[] data = pattern.split(line);
                Integer linkId = Integer.parseInt(data[0]);
                Point point = null;
                try {
                    point = (Point) wktReader.read(data[3]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Node node = new Node(linkId, point);
                nodeMap.put(linkId,node);
                nodes.add(node);
            });

            // 生成link对象。
            Stream<String> linkStream = Files.lines(Paths.get(linkCSVPath));
            linkStream.skip(1).forEach(line->{
                String[] data = pattern.split(line);
                Integer linkId = Integer.parseInt(data[0]);
                String name = data[1];
                Node from = nodeMap.get(Integer.parseInt(data[2]));
                Node to = nodeMap.get(Integer.parseInt(data[3]));
                LineString lineString = null;
                try {
                    lineString = (LineString) wktReader.read(data[11].substring(1,data[11].length()-1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                double length = lineString.getLength();
                double freeSpeed = Integer.parseInt(data[6]);
                double transitTime = (length / 1000) / freeSpeed * 60; // 通行时间单位：分钟
                Edge edge = new Edge(linkId, from, to, transitTime, length,lineString);
                from.outEdges.add(edge);
                to.inEdges.add(edge);
                edges.add(edge);
            });

            return new Graph(edges, nodes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  将原始图分割为由更小的边构成的图，当原始边长度小于目标分割长度*1.5时不分割，否则分割为等长的子边
     * @param graph 图数据结构
     * @param targetLength 目标分割长度
     * @return 分割后的图
     */
    public static Graph splitGraph(Graph graph,double targetLength){
        Map<Node, Node> nodeSubNodeMap = graph.nodes.stream()
                .collect(Collectors.toMap(node -> node, Node::deepCopy));

        Set<Edge> edges = graph.edges.stream().map(edge -> {
            Node in = nodeSubNodeMap.get(edge.in);
            Node out = nodeSubNodeMap.get(edge.out);
            double weight = edge.weight;
            LineString lineString = edge.lineString;
            double length = edge.length;

            Set<Edge> subEdges = new HashSet<>();

            // 如果小于阈值，则不作拆分，将整个Edge对象保存为一个subEdge对象
            if (length < targetLength * 1.5) {
                Edge subEdge = new Edge(in, out, weight, length, lineString);
                in.outEdges.add(subEdge);
                out.inEdges.add(subEdge);
                subEdges.add(subEdge);
                return subEdges;
            }

            //计算拆分后subEdge的长度
            long num = Math.round(length / targetLength);
            double newWeight = weight / num;
            double subEdgeLength = length / num;

            // 从Edge对象中拆分出SubEdge对象数组
            // 初始化参数
            int serial = 1;
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
                while (surplus > 0) {
                    coos.add(endP);
                    serial++;
                    startP = endP;
                    endP = lineString.getCoordinateN(serial);
                    lineSegment = new LineSegment(startP, endP);
                    segLength = lineSegment.getLength();
                    surplus -= segLength;
                }
                // 计算断点
                Coordinate breakP = GeometryUtils.getBreakPoint(startP, endP, (surplus + segLength) / segLength);
                out = new Node(geometryFactory.createPoint(breakP));
                coos.add(breakP);

                // 生成新的lineString对象和Edge对象
                LineString newLineString = geometryFactory.createLineString(coos.toArray(new Coordinate[0]));
                Edge subEdge = new Edge(in, out, newWeight, newLineString.getLength(), newLineString);
                in.outEdges.add(subEdge);
                out.inEdges.add(subEdge);
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
            }

            // 生成新的lineString对象和Edge对象
            out = nodeSubNodeMap.get(edge.out);
            LineString newLineString = geometryFactory.createLineString(coos.toArray(new Coordinate[0]));
            Edge subEdge = new Edge(in, out, weight / num, newLineString.getLength(), newLineString);
            in.outEdges.add(subEdge);
            out.inEdges.add(subEdge);
            subEdges.add(subEdge);
            return subEdges;
        }).flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<Node> nodes = edges.stream().flatMap(edge -> Stream.of(edge.in, edge.out)).collect(Collectors.toSet());

        return new Graph(edges, nodes);
    }

    /**
     * 从边数据中生成图
     * @param edges
     * @return
     */
    public static Graph generateGraphFromEdges(Set<Edge> edges){
        // 清理拓扑
        edges.forEach(edge -> {
            edge.in.inEdges.clear();
            edge.in.outEdges.clear();
            edge.out.inEdges.clear();
            edge.out.outEdges.clear();
        });

        Map<Point,Node> pointNodeMap = new HashMap<>(edges.size() * 2);
        Set<Node> nodes = new HashSet<>(edges.size() * 2);
        edges.forEach(edge -> {
            Node in = edge.in;
            Node out = edge.out;
            // 处理入点
            if(pointNodeMap.get(in.point) != null){
                in = pointNodeMap.get(in.point);
                edge.in = in;
            } else {
                pointNodeMap.put(in.point,in);
                nodes.add(in);
            }
            in.outEdges.add(edge);

            // 处理出点
            if(pointNodeMap.get(out.point) != null){
                out = pointNodeMap.get(out.point);
                edge.out = out;
            } else {
                pointNodeMap.put(out.point,out);
                nodes.add(out);
            }
            out.inEdges.add(edge);
        });
        return new Graph(edges,nodes);
    }
}
