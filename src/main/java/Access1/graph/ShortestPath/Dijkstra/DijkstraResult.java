package Access1.graph.ShortestPath.Dijkstra;

import Access1.graph.Edge;
import Access1.graph.Graph;
import Access1.graph.Node;
import Access1.graph.ShortestPath.Path;
import Access1.graph.ShortestPath.ShortestPathResult;

import java.util.HashMap;
import java.util.Map;

public class DijkstraResult extends ShortestPathResult {

    public Map<Node,DijkstraNode> nodeMap;

    public double popSum;
    public double resourceWeight;

    public DijkstraResult(Graph graph, Node startNode, Map<Node, DijkstraNode> nodeMap) {
        super(graph, startNode);
        this.nodeMap = nodeMap;
    }

    public DijkstraResult(Graph graph, Node startNode, Map<Node, DijkstraNode> nodeMap, double popSum, double resourceWeight) {
        this(graph,startNode,nodeMap);
        this.popSum = popSum;
        this.resourceWeight = resourceWeight;
    }

    public Map<Node,Path> generatePaths(){
        Map<Node,Path> pathMap = new HashMap<>(graph.nodes.size());
        for (Node node : nodeMap.keySet()) {
            Path path = generatePath(node);
            pathMap.put(node,path);
        }
        return pathMap;
    }

    public Path generatePath(Node target){
        Path path = new Path();
        return recurPrint(target,path);
    }

    public Path recurPrint(Node target, Path path){
        Node father = nodeMap.get(target).father;
        path.nodes.add(target);
        Edge fromEdge = nodeMap.get(target).fromEdge;
        if(fromEdge != null) path.edges.add(fromEdge);
        if(father != null) recurPrint(father,path);
        return path;
    }
}
