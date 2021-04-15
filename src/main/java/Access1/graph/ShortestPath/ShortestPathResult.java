package Access1.graph.ShortestPath;

import Access1.graph.Graph;
import Access1.graph.Node;

public abstract class ShortestPathResult {
    public Graph graph;
    public Node startNode;
    public Integer processTime;

    public ShortestPathResult(Graph graph, Node startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }
}
