package Graph.ShortestPath;

import Graph.Graph;
import Graph.Node;

public abstract class ShortestPathResult {
    public Graph graph;
    public Node startNode;
    public Integer processTime;

    public ShortestPathResult(Graph graph, Node startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }
}
