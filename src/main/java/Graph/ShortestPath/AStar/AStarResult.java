package Graph.ShortestPath.AStar;

import Graph.Graph;
import Graph.Node;
import Graph.ShortestPath.Path;
import Graph.ShortestPath.ShortestPathResult;

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
