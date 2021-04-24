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

    public static List<ResourcePoint> getResourcePoints(String filePath) throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> poiSource =
                GeoFileUtils.generateShapefileSource(filePath);
        FeatureCollection<SimpleFeatureType, SimpleFeature> pois = poiSource.getFeatures();
        List<ResourcePoint> resourcePoints = new ArrayList<>(pois.size());
        pois.accepts(poiFeature -> {
            SimpleFeature simpleFeature = (SimpleFeature) poiFeature;
            Object data = simpleFeature.getAttribute("weight");
            double weight = 0.0;
            if(data instanceof Long){
                weight = ((Long)data).intValue();
            }else if(data instanceof Double){
                weight = ((Double)data).intValue();
            }
            ResourcePoint resourcePoint = new ResourcePoint();
            resourcePoints.add(resourcePoint);
            Object defaultGeometry = simpleFeature.getDefaultGeometry();
            if(defaultGeometry instanceof Point){
                resourcePoint.point = (Point) defaultGeometry;
            }
            resourcePoint.resourceWeight = weight;
        }, null);
        List<ResourcePoint> noEmptyResourcePoints = resourcePoints.stream()
                .filter(resourcePoint -> !resourcePoint.point.isEmpty())
                .collect(Collectors.toList());
        if(resourcePoints.size() != noEmptyResourcePoints.size()){
            int difference = resourcePoints.size() - noEmptyResourcePoints.size();
            System.out.println("warring! : 资源点数据中包含" + difference + "个空数据");
        }
        return noEmptyResourcePoints;
    }

    public static List<PopPoint> getPopPoints(String filePath) throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> popShpSource =
                GeoUtils.generateShapefileSource(filePath);
        SimpleFeatureCollection pops = (SimpleFeatureCollection) popShpSource.getFeatures();
        List<PopPoint> popPoints = new ArrayList<>(pops.size());
        pops.accepts(popFeature -> {
            SimpleFeature simpleFeature = (SimpleFeature) popFeature;
            Object defaultGeometry = simpleFeature.getDefaultGeometry();
            PopPoint popPoint = new PopPoint();
            popPoints.add(popPoint);
            if(defaultGeometry instanceof Point){
                popPoint.point = (Point) defaultGeometry;
            }
            popPoint.popNum = (double) simpleFeature.getAttribute("grid_code") / 100000;
        }, null);
        List<PopPoint> noEmptyPopPoints = popPoints.stream().filter(popPoint -> !popPoint.point.isEmpty()).collect(Collectors.toList());
        if(popPoints.size() != noEmptyPopPoints.size()){
            int difference = popPoints.size() - noEmptyPopPoints.size();
            System.out.println("warring! : 人口点数据中包含" + difference + "个空数据");
        }
        return noEmptyPopPoints;
    }

    public static List<DemandPoint> getDemandPoints(List<PopPoint> popPoints) throws Exception {
        List<DemandPoint> demandPoints = popPoints.stream().map(popPoint -> {
            DemandPoint demandPoint = new DemandPoint();
            demandPoint.point = popPoint.point;
            demandPoint.closestNode = popPoint.closestNode;
            demandPoint.connDis = popPoint.connDis;
            demandPoint.originalGeom = popPoint.point;
            demandPoint.type = DemandPoint.DemandType.GRID_FROM_POP;
            return demandPoint;
        }).collect(Collectors.toList());

        List<DemandPoint> noEmptyDemandPoints = demandPoints.stream()
                .filter(demandPoint -> !demandPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(demandPoints.size() != noEmptyDemandPoints.size()){
            int difference = demandPoints.size() - noEmptyDemandPoints.size();
            System.out.println("warring! : 需求点数据中包含" + difference + "个空数据");
        }
        return noEmptyDemandPoints;
    }

    public static List<DemandPoint> getDemandPointsFromNode(Set<Node> nodes){
        List<DemandPoint> demandPoints = nodes.stream().map(node -> {
            DemandPoint demandPoint = new DemandPoint();
            demandPoint.point = node.point;
            demandPoint.closestNode = node;
            demandPoint.connDis = 0;
            demandPoint.originalGeom = node.point;
            demandPoint.type = DemandPoint.DemandType.NODE;
            return demandPoint;
        }).collect(Collectors.toList());

        List<DemandPoint> noEmptyDemandPoints = demandPoints.stream()
                .filter(demandPoint -> !demandPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(demandPoints.size() != noEmptyDemandPoints.size()){
            int difference = demandPoints.size() - noEmptyDemandPoints.size();
            System.out.println("warring! : 需求点数据中包含" + difference + "个空数据");
        }
        return noEmptyDemandPoints;
    }

    public static List<DemandPoint> getDemandPointsFromEdge(Set<Edge> edges){
        List<DemandPoint> demandPoints = edges.stream().map(edge -> {
            DemandPoint demandPoint = new DemandPoint();
            demandPoint.point = GeometryUtils.getBreakPoint(edge.in.point, edge.out.point, 0.5);
            demandPoint.closestNode = edge.in;
            demandPoint.connDis = edge.weight / 2;
            demandPoint.originalGeom = edge.lineString;
            demandPoint.type = DemandPoint.DemandType.EDGE;
            return demandPoint;
        }).collect(Collectors.toList());

        List<DemandPoint> noEmptyDemandPoints = demandPoints.stream()
                .filter(demandPoint -> !demandPoint.point.isEmpty())
                .collect(Collectors.toList());
        if(demandPoints.size() != noEmptyDemandPoints.size()){
            int difference = demandPoints.size() - noEmptyDemandPoints.size();
            System.out.println("warring! : 需求点数据中包含" + difference + "个空数据");
        }
        return noEmptyDemandPoints;
    }

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
