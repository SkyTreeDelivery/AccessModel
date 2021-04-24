package Access2.graph;

import java.util.HashSet;
import java.util.Set;

public class Graph {
    public Set<Edge> edges;
    public Set<Node> nodes;

    public Graph() {
        this.edges = new HashSet<>();
        this.nodes = new HashSet<>();
    }

    public Graph(Set<Edge> edges, Set<Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }
}
