package Access2.graph;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GraphFactory {

    GeometryFactory geometryFactory = new GeometryFactory();
    // 预编译正则表达式
    private static Pattern pattern= Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

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
            linkStream.skip(1).parallel().forEach(line->{
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
            }
            in.outEdges.add(edge);

            // 处理出点
            if(pointNodeMap.get(out.point) != null){
                out = pointNodeMap.get(out.point);
                edge.out = out;
            } else {
                pointNodeMap.put(out.point,out);
            }
            out.inEdges.add(edge);
        });
        return new Graph(edges,nodes);
    }
}
