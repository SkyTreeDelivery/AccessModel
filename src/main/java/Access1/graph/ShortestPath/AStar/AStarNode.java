package Access1.graph.ShortestPath.AStar;

import Access1.graph.Node;
import Access1.graph.ShortestPath.ShortestPathNode;

public class AStarNode extends ShortestPathNode {
    public Double fcost;
    public Double hcost;

    public AStarNode(Node startNode, Node node, Double cost) {
        super(startNode, node, cost);
    }
}
