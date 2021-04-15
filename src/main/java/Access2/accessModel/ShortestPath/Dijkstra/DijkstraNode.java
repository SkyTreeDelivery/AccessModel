package Access2.accessModel.ShortestPath.Dijkstra;

import Access2.accessModel.ShortestPath.ShortestPathNode;
import Access2.graph.Node;

public class DijkstraNode extends ShortestPathNode {

    public DijkstraNode(Node startNode, Node node, Double cost) {
        super(startNode, node, cost);
    }
}
