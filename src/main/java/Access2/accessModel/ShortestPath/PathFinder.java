package Access2.accessModel.ShortestPath;

import Access2.graph.Graph;
import Access2.graph.Node;

public abstract class PathFinder {
    public Graph graph;
    public Node startNode;


    public PathFinder(Graph graph, Node startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }
}
