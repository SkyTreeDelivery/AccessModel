package Access2.accessModel.ShortestPath.Dijkstra;

import Access2.accessModel.ShortestPath.PathFinder;
import Access2.accessModel.dataPoint.ResourcePoint;
import Access2.graph.Edge;
import Access2.graph.Graph;
import Access2.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理一次Dijkstra算法的执行
 */
public class DijkstraPathFinder extends PathFinder {

    // 默认的成本阈值为-1，即进行剪枝，单位：min
    public final static double defaultCostThreshold = -1;

    public ResourcePoint startPoi;

    Map<Node, DijkstraNode> nodeMap;

    private final Graph graph;

    // 成本阈值
    double costThreshold;

    public DijkstraPathFinder(Graph graph, Node startNode) {
        this(graph,startNode,defaultCostThreshold);
    }

    public DijkstraPathFinder(Graph graph, Node startNode, double costThreshold) {
        super(graph, startNode);
        nodeMap = graph.nodes.parallelStream()
                .collect(Collectors.toMap(e -> e, e -> new DijkstraNode(startNode, e, Double.MAX_VALUE)));
        this.costThreshold = costThreshold;
        this.graph = graph;
    }

    public DijkstraPathFinder(Graph graph, ResourcePoint poi, double costThreshold) {
        super(graph, poi.closestNode);
        nodeMap = graph.nodes.parallelStream()
                .collect(Collectors.toMap(e -> e, e -> new DijkstraNode(startNode, e, Double.MAX_VALUE)));
        this.costThreshold = costThreshold;
        this.startPoi = poi;
        this.graph = graph;
    }

    /**
     * 调用最短路径函数之前，需要初始化（预处理）node对象的cost属性
     *
     */
    public DijkstraResult calculate() {
        Set<Node> nodes = new HashSet<>(graph.nodes.size());
        Set<Edge> edges = new HashSet<>(graph.edges.size());
        Graph newGraph = new Graph(edges,nodes);
        // 定义优先级队列 .通过自定义比较器，利用Node的distance属性比较Node之间的优先级。distance越小，优先级越高
        PriorityQueue<DijkstraNode> processingNode =
                new PriorityQueue<DijkstraNode>(nodeMap.size(), (o1, o2) -> {
                    if (o1.cost < o2.cost) {
                        return -1;
                    } else if (o1.cost > o2.cost) {
                        return 1;
                    }
                    return 0;
                });
        // 将起始位置添加到优先级队列中
        HashSet<DijkstraNode> hasInQuene = new HashSet<>(nodeMap.size());
        processingNode.offer(nodeMap.get(startNode));
        nodeMap.get(startNode).cost = 0;  // 默认不行速度 4km/h
        while (!processingNode.isEmpty()) {
            DijkstraNode minNode = processingNode.poll();
            minNode.isVisited = true;
            if(costThreshold >= 0 && minNode.cost > costThreshold){
                continue;
            }
            nodes.add(minNode.node);
            // 遍历所有的出边
            List<Edge> outEdges = minNode.node.outEdges;
            for (int i = 0; i < outEdges.size(); i++) {
                Edge edge = outEdges.get(i);
                edges.add(edge);
                DijkstraNode outNode = nodeMap.get(edge.out);
                if(outNode.isVisited) continue;
                if (minNode.cost + edge.weight < outNode.cost) {
                    // 如果新的路径的distance更小，则更新distance
                    outNode.cost = minNode.cost + edge.weight;
                    outNode.father = minNode.node;
                    outNode.fromEdge = edge;
                    outNode.length = minNode.length + edge.length;
                    if (hasInQuene.contains(outNode)) {
                        // 先将待更新的Node出队列，再入队列，实现堆的更新
                        processingNode.remove(outNode);
                        processingNode.offer(outNode);
                    } else {
                        processingNode.offer(outNode);
                        hasInQuene.add(outNode);
                    }
                }
            }
        }
        return new DijkstraResult(newGraph, startNode, nodeMap);
    }
}
