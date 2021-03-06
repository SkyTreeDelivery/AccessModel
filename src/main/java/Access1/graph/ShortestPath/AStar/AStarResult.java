package Access1.graph.ShortestPath.AStar;

import Access1.graph.Graph;
import Access1.graph.Node;
import Access1.graph.ShortestPath.Path;
import Access1.graph.ShortestPath.ShortestPathResult;

import java.util.HashMap;
import java.util.Map;

public class AStarResult extends ShortestPathResult {
    public Map<Node,AStarNode> map;

    public AStarResult(Graph graph, Node startNode, Map<Node, AStarNode> map) {
        super(graph, startNode);
        this.map = map;
    }

    public Map<Node, Path> generatePaths(){
        Map<Node,Path> pathMap = new HashMap<>(graph.nodes.size());
        for (Node node : map.keySet()) {
            Path path = generatePath(node);
            pathMap.put(node,path);
        }
        return pathMap;
    }

    public Path generatePath(Node target){
        Path path = new Path();
        Node father = map.get(target).father;
        if(father != null) generatePath(father);
        path.nodes.add(target);
        return path;
    }
}
