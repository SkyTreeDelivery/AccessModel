package Graph.ShortestPath;

import Graph.Graph;
import Graph.Node;

public abstract class PathFinder {
    public Graph graph;
    public Node startNode;


    public PathFinder(Graph graph, Node startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }
}
