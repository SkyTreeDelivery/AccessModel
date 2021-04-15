package Access1;

import Access1.graph.Edge;
import Access1.graph.Graph;
import Access1.graph.Node;

import java.util.*;

public class GraphHandleUtils {

    public static Graph extractMaxGraph(Graph multiPartGraph){
        if(multiPartGraph == null || multiPartGraph.nodes.size() == 0 || multiPartGraph.edges.size() == 0){
            throw new IllegalArgumentException();
        }
        ArrayList<Graph> subGraphs = new ArrayList<Graph>();
        Deque<Node> nodeQueue = new LinkedList<Node>();
        // multiGraph所有的节点
        List<Node> nodes = new LinkedList<Node>(multiPartGraph.nodes);
        while(nodes.size() != 0){

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
            while(nodeQueue.size()!=0){
                Node poll = nodeQueue.poll();
                // 将子图数据装入集合中
                thisNodes.add(poll);
                thisEdge.addAll(poll.inEdges);
                thisEdge.addAll(poll.outEdges);
                poll.inEdges.forEach(edge -> {
                    // 处理入边
                    if (hasHandledNode.contains(edge.in)){
                        return;
                    }
                    nodes.remove(edge.in);
                    nodeQueue.offer(edge.in);
                    hasHandledNode.add(edge.in);
                });
                // 处理出边
                poll.outEdges.forEach(edge -> {
                    if (hasHandledNode.contains(edge.out)){
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
}
