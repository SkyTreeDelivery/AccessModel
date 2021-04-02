package Graph;

import java.util.HashSet;

public class Graph {
    public HashSet<Edge> edges;
    public HashSet<Node> nodes;

    public Graph() {
        this.edges = new HashSet<>();
        this.nodes = new HashSet<>();
    }

    public Graph(HashSet<Edge> edges, HashSet<Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }
}
