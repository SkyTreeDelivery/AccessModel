package Graph.ShortestPath.Dijkstra;

import Graph.Node;
import Graph.ShortestPath.ShortestPathNode;

public class DijkstraNode extends ShortestPathNode {

    public DijkstraNode(Node startNode, Node node, Double cost) {
        super(startNode, node, cost);
    }
}
