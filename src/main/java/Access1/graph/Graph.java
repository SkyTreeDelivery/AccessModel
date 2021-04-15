package Access1.graph;

import java.util.HashSet;

public class Graph {
    public HashSet<Access1.graph.Edge> edges;
    public HashSet<Access1.graph.Node> nodes;

    public Graph() {
        this.edges = new HashSet<>();
        this.nodes = new HashSet<>();
    }

    public Graph(HashSet<Edge> edges, HashSet<Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }
}
