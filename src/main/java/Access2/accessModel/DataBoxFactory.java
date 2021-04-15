package Access2.accessModel;

import Access2.accessModel.dataPoint.DataPointFactory;
import Access2.accessModel.dataPoint.DemandPoint;
import Access2.accessModel.dataPoint.PopPoint;
import Access2.accessModel.dataPoint.ResourcePoint;
import Access2.graph.Edge;
import Access2.graph.Node;

import java.util.List;
import java.util.Set;

public class DataBoxFactory {

    public static DataBox getDataBox_poi_popgrid_edges(String poiFilePath, String popFilePath, Set<Edge> edges) throws Exception {
        List<ResourcePoint> resourcePoints = DataPointFactory.getResourcePoints(poiFilePath);
        List<PopPoint> popPoints = DataPointFactory.getPopPoints(popFilePath);
        List<DemandPoint> demandPoints = DataPointFactory.getDemandPointsFromEdge(edges);
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }

    public static DataBox getDataBox_poi_popgrid_nodes(String poiFilePath, String popFilePath, Set<Node> nodes) throws Exception {
        List<ResourcePoint> resourcePoints = DataPointFactory.getResourcePoints(poiFilePath);
        List<PopPoint> popPoints = DataPointFactory.getPopPoints(popFilePath);
        List<DemandPoint> demandPoints = DataPointFactory.getDemandPointsFromNode(nodes);
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }

    public static DataBox getDataBox_poi_popgrid_nodes(
            List<ResourcePoint> resourcePoints, List<PopPoint> popPoints,
            List<DemandPoint> demandPoints) throws Exception {
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }
}
