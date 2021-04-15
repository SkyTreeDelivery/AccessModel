package Access1.graph.ShortestPath;

import Access1.graph.Edge;
import Access1.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class Path {
    public List<Node> nodes;
    public List<Edge> edges;

    public Path() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Path{" +
                "nodes=" + nodes +
                '}';
    }
}
