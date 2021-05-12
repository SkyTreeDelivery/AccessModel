package Access2.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * 表示有向图
 */
public class Graph {
    // 图的边
    public Set<Edge> edges;
    // 图的节点
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
