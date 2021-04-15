package Access1.graph.ShortestPath.AStar;

import Access1.graph.Edge;
import Access1.graph.Graph;
import Access1.graph.Node;
import Access1.graph.ShortestPath.PathFinder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static Access1.graph.ShortestPath.MathFunc.manhatten;

public class AStarPathFinder extends PathFinder {

    Map<Node, AStarNode> map;
    Node target;

    public AStarPathFinder(Graph graph, Node startNode, Node target) {
        super(graph, startNode);
        map = graph.nodes.parallelStream()
                .collect(Collectors.toMap(e -> e, e -> new AStarNode(startNode, e, Double.MAX_VALUE)));
        this.target = target;
    }

    public AStarResult calculate(Node target) {
        // 定义优先级队列 .通过自定义比较器，利用Node的distance属性比较Node之间的优先级。distance越小，优先级越高
        PriorityQueue<AStarNode> processingNode =
                new PriorityQueue<AStarNode>(map.size(), (o1, o2) -> {
                    if (o1.cost < o2.cost) {
                        return -1;
                    } else if (o1.cost > o2.cost) {
                        return 1;
                    }
                    return 0;
                });
        // 将起始位置添加到优先级队列中
        HashSet<AStarNode> hasInQuene = new HashSet<>(map.size());
        processingNode.offer(map.get(startNode));
        map.get(startNode).cost = 0.0;
        for (Node node : map.keySet()) {
            AStarNode aStarNode = map.get(node);
            aStarNode.hcost = manhatten(map.get(startNode).node, aStarNode.node);
        }
        while (!processingNode.isEmpty()) {
            AStarNode minNode = processingNode.poll();
            minNode.isVisited = true;
            List<Edge> outEdges = minNode.node.outEdges;
            for (int i = 0; i < outEdges.size(); i++) {
                Edge edge = outEdges.get(i);
                AStarNode outNode = map.get(edge.out);
                if(outNode == map.get(target)) break;
                if (outNode.isVisited) continue;
                if (minNode.cost + edge.weight < outNode.cost) {
                    // 如果新的路径的distance更小，则更新cost
                    outNode.cost = minNode.cost + edge.weight;
                    outNode.hcost = manhatten(outNode.node, map.get(target).node);
                    outNode.fcost = outNode.cost + outNode.hcost;
                    outNode.father = minNode.node;
                    outNode.fromEdge = edge;
                    if (hasInQuene.contains(outNode)) {
                        // 先将待更新的Node出队列，再入队列，到达更新的效果
                        processingNode.remove(outNode);
                        processingNode.offer(outNode);
                    } else {
                        processingNode.offer(outNode);
                        hasInQuene.add(outNode);
                    }
                }
            }
        }
        return new AStarResult(graph, startNode, map);
    }
}
