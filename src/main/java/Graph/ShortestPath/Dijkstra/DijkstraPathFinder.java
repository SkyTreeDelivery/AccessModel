package Graph.ShortestPath.Dijkstra;

import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Graph.ShortestPath.PathFinder;
import Graph.kde.Poi;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * 处理一次Dijkstra算法的执行
 */
public class DijkstraPathFinder extends PathFinder {

    // 默认的成本阈值为-1，即进行剪枝，单位：min
    private final static double defaultCostThreshold = -1;

    Map<Node, DijkstraNode> nodeMap;

    public double popSum;

    // 成本阈值
    double costThreshold;
    // 本次Dijkstra算法执行的poi对象的资源权重
    double resourceWeight;

    public Poi startPoi;

    public DijkstraPathFinder(Graph graph, Node startNode) {
        this(graph,startNode,defaultCostThreshold);
    }

    public DijkstraPathFinder(Graph graph, Node startNode, double costThreshold) {
        super(graph, startNode);
        nodeMap = graph.nodes.parallelStream()
                .collect(Collectors.toMap(e -> e, e -> new DijkstraNode(startNode, e, Double.MAX_VALUE)));
        this.costThreshold = costThreshold;
        resourceWeight = 1;
    }

    public DijkstraPathFinder(Graph graph, Poi poi, double costThreshold) {
        super(graph, poi.closestNode);
        nodeMap = graph.nodes.parallelStream()
                .collect(Collectors.toMap(e -> e, e -> new DijkstraNode(startNode, e, Double.MAX_VALUE)));
        this.costThreshold = costThreshold;
        this.startPoi = poi;
        resourceWeight = poi.weight;
    }

    /**
     * 调用最短路径函数之前，需要初始化（预处理）node对象的cost属性
     *
     */
    public DijkstraResult calculate() {
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
        nodeMap.get(startNode).cost = startNode.point.distance(startPoi.point) / 30;
        while (!processingNode.isEmpty()) {
            DijkstraNode minNode = processingNode.poll();
            minNode.isVisited = true;
            synchronized (minNode.node){
                int nNum = minNode.node.nNum;  // 耦合代码
                minNode.node.costDisArray[nNum] = minNode.cost; // 耦合代码
                minNode.node.pois[nNum] = startPoi;  // 耦合代码
                minNode.node.nNum += 1;  // 耦合代码
            }
            // 剪枝操作
            if(costThreshold >= 0 && minNode.cost > costThreshold){
                continue;
            }
            // 遍历所有的出边
            List<Edge> outEdges = minNode.node.outEdges;
            for (int i = 0; i < outEdges.size(); i++) {
                Edge edge = outEdges.get(i);
                for (Edge subEdge : edge.subEdges) {    // 耦合代码
                    double costDis = minNode.cost + subEdge.childCenterCoefficient * edge.weight;   // 耦合代码
                    if(costThreshold >= 0 && minNode.cost > costThreshold){
                        break;
                    }
                    synchronized (subEdge){
                        int eNum = subEdge.eNum;  // 耦合代码
                        subEdge.subEdgeCostDisArray[eNum] = costDis;   // 耦合代码
                        subEdge.pois[eNum] = startPoi;    // 耦合代码
                        subEdge.eNum += 1;    // 耦合代码
                        // 统计人口
                        popSum += subEdge.pop;
                    }
                }
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
        startPoi.popCover = popSum;
        return new DijkstraResult(graph, startNode, nodeMap, popSum, resourceWeight);
    }

    public DijkstraResult calculate_pure() {
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
        nodeMap.get(startNode).cost = 0.0;
        while (!processingNode.isEmpty()) {
            DijkstraNode minNode = processingNode.poll();
            minNode.isVisited = true;
            // 遍历所有的出边
            List<Edge> outEdges = minNode.node.outEdges;
            for (int i = 0; i < outEdges.size(); i++) {
                Edge edge = outEdges.get(i);
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
        return new DijkstraResult(graph, startNode, nodeMap);
    }
}
