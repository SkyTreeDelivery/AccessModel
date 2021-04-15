package Access2.graph;

import Access2.utils.GeometryUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;

import java.util.*;
import java.util.stream.Collectors;

public class GraphHandler {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static Graph extractMaxGraph(Graph multiPartGraph) {
        if (multiPartGraph == null || multiPartGraph.nodes.size() == 0 || multiPartGraph.edges.size() == 0) {
            throw new IllegalArgumentException();
        }
        ArrayList<Graph> subGraphs = new ArrayList<Graph>();
        Deque<Node> nodeQueue = new LinkedList<Node>();
        // multiGraph所有的节点
        List<Node> nodes = new LinkedList<Node>(multiPartGraph.nodes);
        while (nodes.size() != 0) {

            // 执行一次广度优先搜索，处理一个子图
            // 已处理节点，防止一个节点被多次处理
            HashSet<Node> hasHandledNode = new LinkedHashSet<Node>();
            // 该子图的节点
            HashSet<Node> thisNodes = new HashSet<Node>();
            // 该子图的边
            HashSet<Edge> thisEdge = new HashSet<Edge>();

            // 提取一个种子节点
            Node startNode = nodes.get(0);
            nodes.remove(startNode);
            nodeQueue.offer(startNode);
            hasHandledNode.add(startNode);
            while (nodeQueue.size() != 0) {
                Node poll = nodeQueue.poll();
                // 将子图数据装入集合中
                thisNodes.add(poll);
                thisEdge.addAll(poll.inEdges);
                thisEdge.addAll(poll.outEdges);
                poll.inEdges.forEach(edge -> {
                    // 处理入边
                    if (hasHandledNode.contains(edge.in)) {
                        return;
                    }
                    nodes.remove(edge.in);
                    nodeQueue.offer(edge.in);
                    hasHandledNode.add(edge.in);
                });
                // 处理出边
                poll.outEdges.forEach(edge -> {
                    if (hasHandledNode.contains(edge.out)) {
                        return;
                    }
                    nodes.remove(edge.out);
                    nodeQueue.offer(edge.out);
                    hasHandledNode.add(edge.out);
                });
            }
            // 生成子图
            Graph subGraph = new Graph(thisEdge, thisNodes);
            subGraphs.add(subGraph);
        }

        // 选择最大的子图
        Graph maxGraph = subGraphs.stream().max(Comparator.comparing((Graph graph) -> graph.nodes.size()))
                .orElse(multiPartGraph);
        return maxGraph;
    }

    public static Set<Edge> splitEdge(Set<Edge> edges, double targetLength) {
        return edges.parallelStream()
                .flatMap(edge -> splitEdge(edge, targetLength).stream())
                .collect(Collectors.toSet());
    }

    private static List<Edge> splitEdge(Edge edge, double targetLength) {
        Node in = Node.deepCopy(edge.in);
        Node out =  Node.deepCopy(edge.out);
        double weight = edge.weight;
        LineString lineString = edge.lineString;
        double length = edge.length;

        List<Edge> result = new ArrayList<>();

        // 如果小于阈值，则不作拆分，将整个Edge对象保存为一个subEdge对象
        if (length < targetLength * 1.5) {
            Edge subEdge = new Edge(in, out, weight, length, lineString);
            result.add(subEdge);
            return result;
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
            result.add(subEdge);

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
            Edge subEdge = new Edge(in, out, weight / num, newLineString.getLength(), newLineString);
            result.add(subEdge);
        }
        return result;
    }
}
