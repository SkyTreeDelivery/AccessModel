package Graph.ShortestPath.AStar;

import Graph.Node;
import Graph.ShortestPath.ShortestPathNode;

public class AStarNode extends ShortestPathNode {
    public Double fcost;
    public Double hcost;

    public AStarNode(Node startNode, Node node, Double cost) {
        super(startNode, node, cost);
    }
}
