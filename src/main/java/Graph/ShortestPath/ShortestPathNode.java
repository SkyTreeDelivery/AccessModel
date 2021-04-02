package Graph.ShortestPath;

import Graph.Edge;
import Graph.Node;

public abstract class ShortestPathNode {
    final public Node startNode;
    public  Node father;
    public Edge fromEdge;
    final public Node node;
    public Boolean isVisited = false;
    // 从startNode到该node的旅行成本
    public double cost;
    // 从startNode到该node的旅行距离
    public double length;

    public ShortestPathNode(Node startNode, Node node, Double cost) {
        this.startNode = startNode;
        this.node = node;
        this.cost = cost;
    }
}
