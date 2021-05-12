package Access2.accessModel.databox;

import Access2.accessModel.dataPoint.DataPointFactory;
import Access2.accessModel.dataPoint.DemandPoint;
import Access2.accessModel.dataPoint.PopPoint;
import Access2.accessModel.dataPoint.ResourcePoint;
import Access2.graph.Edge;
import Access2.graph.Node;

import java.util.List;
import java.util.Set;

public class DataBoxFactory {

    public static DataBox getDataBox_poi_popGrid_demand(
            List<ResourcePoint> resourcePoints, List<PopPoint> popPoints,
            List<DemandPoint> demandPoints) throws Exception {
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }

    /**
     * 根据poi shp文件、人口点shp文件和边集合，生成DataBox对象
     * @param poiFilePath poi shp文件路径
     * @param popFilePath 人口点shp文件路径
     * @param edges 边结合
     * @return DataBox对象
     * @throws Exception
     */
    public static DataBox getDataBox_poi_popGrid_edges(String poiFilePath, String popFilePath, Set<Edge> edges) throws Exception {
        List<ResourcePoint> resourcePoints = DataPointFactory.getResourcePoints(poiFilePath);
        List<PopPoint> popPoints = DataPointFactory.getPopPoints(popFilePath);
        List<DemandPoint> demandPoints = DataPointFactory.getDemandPointsFromEdge(edges);
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }

    /**
     * 根据poi shp文件、人口点shp文件和节点集合，生成DataBox对象
     * @param poiFilePath poi shp文件路径
     * @param popFilePath 人口点shp文件路径
     * @param nodes 节点集合
     * @return DataBox对象
     * @throws Exception
     */
    public static DataBox getDataBox_poi_popGrid_nodes(String poiFilePath, String popFilePath, Set<Node> nodes) throws Exception {
        List<ResourcePoint> resourcePoints = DataPointFactory.getResourcePoints(poiFilePath);
        List<PopPoint> popPoints = DataPointFactory.getPopPoints(popFilePath);
        List<DemandPoint> demandPoints = DataPointFactory.getDemandPointsFromNode(nodes);
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }

    /**
     * 根据poi shp文件、人口点shp文件和面shp文件，生成DataBox对象
     * @param poiFilePath poi shp文件路径
     * @param popFilePath 人口点shp文件路径
     * @param polygonFilePath 面shp文件路径
     * @return DataBox对象
     * @throws Exception
     */
    public static DataBox getDataBox_poi_popGrid_polygon(String poiFilePath, String popFilePath, String polygonFilePath) throws Exception {
        List<ResourcePoint> resourcePoints = DataPointFactory.getResourcePoints(poiFilePath);
        List<PopPoint> popPoints = DataPointFactory.getPopPoints(popFilePath);
        List<DemandPoint> demandPoints = DataPointFactory.getDemandPointsFromPolygonShp(polygonFilePath);
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }

    /**
     * 根据poi shp文件、人口点shp文件，生成DataBox对象，需求点与人口点保持一致
     * @param poiFilePath poi shp文件路径
     * @param popFilePath 人口点shp文件路径
     * @return DataBox对象
     * @throws Exception
     */
    public static DataBox getDataBox_poi_popGrid_gridFromPop(String poiFilePath, String popFilePath) throws Exception {
        List<ResourcePoint> resourcePoints = DataPointFactory.getResourcePoints(poiFilePath);
        List<PopPoint> popPoints = DataPointFactory.getPopPoints(popFilePath);
        List<DemandPoint> demandPoints = DataPointFactory.getDemandPoints(popPoints);
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }

    /**
     * 根据poi shp文件、人口点shp文件，格网shp文件 生成DataBox对象
     * @param poiFilePath poi shp文件路径
     * @param popFilePath 人口点shp文件路径
     * @param gridFilePath 格网shp文件路径
     * @return DataBox对象
     * @throws Exception
     */
    public static DataBox getDataBox_poi_popGrid_Grid(String poiFilePath, String popFilePath, String gridFilePath) throws Exception {
        List<ResourcePoint> resourcePoints = DataPointFactory.getResourcePoints(poiFilePath);
        List<PopPoint> popPoints = DataPointFactory.getPopPoints(popFilePath);
        List<DemandPoint> demandPoints = DataPointFactory.getDemandPointsFromPolygonShp(gridFilePath);
        return new DataBox(resourcePoints,popPoints,demandPoints);
    }
}
