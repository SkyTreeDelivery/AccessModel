package Access2.accessModel.dataPoint;

import Access1.GeoUtils;
import Access2.graph.Edge;
import Access2.graph.Node;
import Access2.utils.GeoFileUtils;
import Access2.utils.GeometryUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataPointFactory {

    private final static GeometryFactory geometryFactory = new GeometryFactory();

    /**
     *  从资源点shp文件中生成资源点对象，通常从POI数据中生成资源点
     * @param filePath 资源点shp文件的路径
     * @return 资源点集合
     * @throws Exception
     */
    public static List<ResourcePoint> getResourcePoints(String filePath) throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> poiSource =
                GeoFileUtils.generateShapefileSource(filePath);
        FeatureCollection<SimpleFeatureType, SimpleFeature> pois = poiSource.getFeatures();
        List<ResourcePoint> resourcePoints = new ArrayList<>(pois.size());

        pois.accepts(poiFeature -> {
            SimpleFeature simpleFeature = (SimpleFeature) poiFeature;
            // 资源点权重
            Object data = simpleFeature.getAttribute("sj");
            double weight = 0.0;
            // 如果数据为整数，则会为转换为Long类型，如数据为小数，则会被转换为Double类型
            if(data instanceof Long){
                weight = ((Long)data).intValue();
            }else if(data instanceof Double){
                weight = ((Double)data).intValue();
            }else if(data instanceof Integer){
                weight = (Integer) data;
            }
            ResourcePoint resourcePoint = new ResourcePoint();
            Object defaultGeometry = simpleFeature.getDefaultGeometry();
            if(defaultGeometry instanceof Point){
                resourcePoint.point = (Point) defaultGeometry;
            }
            resourcePoint.resourceWeight = weight;
            resourcePoint.d0 = 10;//(double) simpleFeature.getAttribute("d0");
            resourcePoints.add(resourcePoint);
        }, null);

        // 过滤掉point为空的资源点
        List<ResourcePoint> noEmptyResourcePoints = resourcePoints.stream()
                .filter(resourcePoint -> !resourcePoint.point.isEmpty())
                .collect(Collectors.toList());
        if(resourcePoints.size() != noEmptyResourcePoints.size()){
            int difference = resourcePoints.size() - noEmptyResourcePoints.size();
            System.out.println("warring! : 资源点数据中包含" + difference + "个空数据");
        }
        return noEmptyResourcePoints;
    }

    /**
     * 从人口点shp文件中生成人口点对象，通常从人口栅格中生成人口点shp
     * @param filePath 人口点shp文件的文件路径
     * @return 人口点集合
     * @throws Exception
     */
    public static List<PopPoint> getPopPoints(String filePath) throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> popShpSource =
                GeoUtils.generateShapefileSource(filePath);
        SimpleFeatureCollection pops = (SimpleFeatureCollection) popShpSource.getFeatures();
        List<PopPoint> popPoints = new ArrayList<>(pops.size());

        // 从SimpleFeatureCollection中生成人口点对象
        pops.accepts(popFeature -> {
            SimpleFeature simpleFeature = (SimpleFeature) popFeature;
            Object defaultGeometry = simpleFeature.getDefaultGeometry();
            PopPoint popPoint = new PopPoint();
            popPoints.add(popPoint);
            if(defaultGeometry instanceof Point){
                popPoint.point = (Point) defaultGeometry;
            }
            popPoint.popNum = (double) simpleFeature.getAttribute("GRID_CODE") / 100000;
        }, null);

        // 过滤掉point为空的资源点
        List<PopPoint> noEmptyPopPoints = popPoints.stream()
                .filter(popPoint -> !popPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(popPoints.size() != noEmptyPopPoints.size()){
            int difference = popPoints.size() - noEmptyPopPoints.size();
            System.out.println("warring! : 人口点数据中包含" + difference + "个空数据");
        }
        return noEmptyPopPoints;
    }

    /**
     * 从人口点中生成需求点
     * @param popPoints 人口点结合
     * @return 需求点结合
     * @throws Exception
     */
    public static List<DemandPoint> getDemandPoints(List<PopPoint> popPoints) throws Exception {
        // 根据人口点生成需求点
        List<DemandPoint> demandPoints = popPoints.stream().map(popPoint -> {
            DemandPoint demandPoint = new DemandPoint();
            demandPoint.point = popPoint.point;
            demandPoint.closestNode = popPoint.closestNode;
            demandPoint.connCost = popPoint.connCost;
            demandPoint.originalGeom = popPoint.point;
            demandPoint.type = DemandPoint.DemandType.GRID_FROM_POP;
            return demandPoint;
        }).collect(Collectors.toList());

        // 过滤掉point属性为空的需求点
        List<DemandPoint> noEmptyDemandPoints = demandPoints.stream()
                .filter(demandPoint -> !demandPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(demandPoints.size() != noEmptyDemandPoints.size()){
            int difference = demandPoints.size() - noEmptyDemandPoints.size();
            System.out.println("warring! : 需求点数据中包含" + difference + "个空数据");
        }
        return noEmptyDemandPoints;
    }

    /**
     * 根据图节点生成需求点
     * @param nodes 节点集合
     * @return 需求点集合
     */
    public static List<DemandPoint> getDemandPointsFromNode(Set<Node> nodes){
        // 根据图节点生成需求点
        List<DemandPoint> demandPoints = nodes.stream().map(node -> {
            DemandPoint demandPoint = new DemandPoint();
            demandPoint.point = node.point;
            demandPoint.closestNode = node;
            //节点到最近节点的成本为0
            demandPoint.connCost = 0;
            demandPoint.originalGeom = node.point;
            demandPoint.type = DemandPoint.DemandType.NODE;
            return demandPoint;
        }).collect(Collectors.toList());

        // 过滤掉point属性为空的需求点
        List<DemandPoint> noEmptyDemandPoints = demandPoints.stream()
                .filter(demandPoint -> !demandPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(demandPoints.size() != noEmptyDemandPoints.size()){
            int difference = demandPoints.size() - noEmptyDemandPoints.size();
            System.out.println("warring! : 需求点数据中包含" + difference + "个空数据");
        }
        return noEmptyDemandPoints;
    }

    /**
     * 根据图数据结构的边生成需求点
     * @param edges
     * @return
     */
    public static List<DemandPoint> getDemandPointsFromEdge(Set<Edge> edges){
        // 根据图数据结构的边生成需求点
        List<DemandPoint> demandPoints = edges.stream().map(edge -> {
            DemandPoint demandPoint = new DemandPoint();
            // 取边的中点作为需求点的位置
            demandPoint.point = GeometryUtils.getBreakPoint(edge.in.point, edge.out.point, 0.5);
            demandPoint.closestNode = edge.in;
            demandPoint.connCost = edge.weight / 2;
            demandPoint.originalGeom = edge.lineString;
            demandPoint.type = DemandPoint.DemandType.EDGE;
            return demandPoint;
        }).collect(Collectors.toList());

        // 过滤掉point属性为空的需求点
        List<DemandPoint> noEmptyDemandPoints = demandPoints.stream()
                .filter(demandPoint -> !demandPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(demandPoints.size() != noEmptyDemandPoints.size()){
            int difference = demandPoints.size() - noEmptyDemandPoints.size();
            System.out.println("warring! : 需求点数据中包含" + difference + "个空数据");
        }
        return noEmptyDemandPoints;
    }

    /**
     *  从shp文件中生成需求点对象，如建筑物shp文件、AOI数据shp文件等。
     * @param filePath
     * @return
     * @throws Exception
     */
    public static List<DemandPoint> getDemandPointsFromPolygonShp(String filePath) throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> polygonSource =
                GeoFileUtils.generateShapefileSource(filePath);
        FeatureCollection<SimpleFeatureType, SimpleFeature> polygonFeatures = polygonSource.getFeatures();
        List<DemandPoint> demandPoints = new ArrayList<>(polygonFeatures.size());
        polygonFeatures.accepts(polygonFeature -> {
            SimpleFeature simpleFeature = (SimpleFeature) polygonFeature;
            Geometry defaultGeometry = (Geometry)simpleFeature.getDefaultGeometry();

            // 将polygon转换为multipolygon
            MultiPolygon geometry;
            if (defaultGeometry instanceof Polygon){
                Polygon[] polys = new Polygon[1];
                polys[0] = (Polygon) defaultGeometry;
                geometry = geometryFactory.createMultiPolygon(polys);
            } else {
                geometry = (MultiPolygon) defaultGeometry;
            }

            // 过滤掉point属性为空的需求点
            DemandPoint demandPoint = new DemandPoint();
            demandPoints.add(demandPoint);
            demandPoint.point = geometry.getCentroid();
            demandPoint.originalGeom = geometry ;
            demandPoint.type = DemandPoint.DemandType.POLYGON;
        }, null);

        List<DemandPoint> noEmptyDemandPoints = demandPoints.stream()
                .filter(demandPoint -> !demandPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(demandPoints.size() != noEmptyDemandPoints.size()){
            int difference = demandPoints.size() - noEmptyDemandPoints.size();
            System.out.println("warring! : 需求点数据中包含" + difference + "个空数据");
        }
        return noEmptyDemandPoints;
    }
}
