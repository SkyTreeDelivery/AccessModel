package Access1;

import Access1.graph.Edge;
import Access1.graph.Node;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GeoUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static FeatureSource<SimpleFeatureType, SimpleFeature> generateShapefileSource(String shapefilePath) throws Exception {
        File file = new File(shapefilePath);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        return dataStore.getFeatureSource(typeName);
    }

//    public static Graph generateGraph(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) throws IOException {
//
//        //create a linear graph generate
//        DirectedLineStringGraphGenerator lineStringGen = new DirectedLineStringGraphGenerator();
//
//        //wrap it in a feature graph generator
//        FeatureGraphGenerator featureGen = new FeatureGraphGenerator(lineStringGen);
//
//        // get a feature collection somehow
//        SimpleFeatureCollection fc = (SimpleFeatureCollection) featureSource.getFeatures();
//        fc.accepts(
//                featureGen::add,
//                null);
//        Graph graph = featureGen.getGraph();
//
//        // ??????linestring???link_id????????????Graph???????????????id???
//        // ??????linestring???fromnode???tonode???id??????graph???nodeA???NodeB???id
//        Collection<Edge> edges = graph.getEdges();
//        for (Edge edge : edges) {
//            SimpleFeature feature = (SimpleFeature) edge.getObject();
//            // ??????edge???id
//            String link_id = (String) feature.getAttribute("LINK_ID");
//            edge.setID(Integer.parseInt(link_id));
//            org.geotools.graph.structure.Node nodeA = edge.getNodeA();
//            org.geotools.graph.structure.Node nodeB = edge.getNodeB();
//            // ??????node???id
//            String from_node_ = (String) feature.getAttribute("FROM_NODE_");
//            String to_node_id = (String) feature.getAttribute("TO_NODE_ID");
//            nodeA.setID(Integer.parseInt(from_node_));
//            nodeB.setID(Integer.parseInt(to_node_id));
//        }
//
//        return graph;
//    }

    public static DijkstraIterator.EdgeWeighter getDefaultWeighter() {
        // create a strategy for weighting edges in the graph
        // in this case we are using geometry length
        return e -> {
            SimpleFeature feature = (SimpleFeature) e.getObject();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            return geometry.getLength();
        };
    }

    public static DijkstraShortestPathFinder dijkstra(Graph graph, org.geotools.graph.structure.Node start, DijkstraIterator.EdgeWeighter weighter) {
        // Create GraphWalker - in this case DijkstraShortestPathFinder
        DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph, start, weighter);
        // calculate()?????????????????????????????????????????????????????????????????????????????????
        LocalDateTime before = LocalDateTime.now();
        pf.calculate();
        LocalDateTime after = LocalDateTime.now();
        //??????????????????
        System.out.println("geotools????????????Dijkstra???????????????" + Duration.between(before, after).toMillis());
        return pf;
    }

    /**
     * ??????R???
     *
     * @param sfc
     * @return
     */
    public static STRtree generateNodeRtree(SimpleFeatureCollection sfc) throws IOException {
        STRtree stRtree = new STRtree(sfc.size());
        sfc.accepts(ft -> {
            Geometry geometry = (Geometry) ((SimpleFeature) ft).getDefaultGeometry();
            stRtree.insert(geometry.getEnvelopeInternal(), ft);
        }, null);
        stRtree.build();
        System.out.println("str????????????" + stRtree.getRoot().getLevel());
        return stRtree;
    }

    public static STRtree generateNodeRtree(Set<Node> nodes){
        STRtree stRtree = new STRtree(nodes.size());
        nodes.forEach(node -> stRtree.insert(node.point.getEnvelopeInternal(),node));
        stRtree.build();
        return stRtree;
    }

    public static STRtree generateEdgeRtree(Set<Edge> edges){
        STRtree stRtree = new STRtree(edges.size());
        edges.forEach(edge -> stRtree.insert(edge.lineString.getEnvelopeInternal(),edge));
        stRtree.build();
        return stRtree;
    }

    public static STRtree generatePopRtree(Set<SimpleFeature> features){
        STRtree stRtree = new STRtree(features.size());
        features.forEach(feature ->{
            Point point = (Point) feature.getDefaultGeometry();
            stRtree.insert(point.getEnvelopeInternal(), point);
        } );
        stRtree.build();
        return stRtree;
    }

    public static STRtree generatePointRtree(Set<Point> points){
        STRtree stRtree = new STRtree(points.size());
        points.forEach(point ->{
            stRtree.insert(point.getEnvelopeInternal(), point);
        } );
        stRtree.build();
        return stRtree;
    }

    /**
     *  ?????????
     * @param stRtree
     * @param feature
     * @return
     */
    public static SimpleFeature searchClosestNode(STRtree stRtree, SimpleFeature feature) {
        Point target = (Point) feature.getDefaultGeometry();
        Envelope search = new Envelope(target.getCoordinate());
        double searchDistance = 0.001;
        List<SimpleFeature> result;
        // ???????????????????????????
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
            } else {
                break;
            }
        }
        // ?????????????????????
        SimpleFeature clostestPoint = result.get(0);
        double distance = target.distance((Geometry) clostestPoint.getDefaultGeometry());
        for (int i = 1; i < result.size(); i++) {
            double d = target.distance((Geometry) result.get(i).getDefaultGeometry());
            if (distance > d) {
                distance = d;
                clostestPoint = result.get(i);
            }
        }
        return clostestPoint;
    }

    public static Node searchClosestNode(STRtree stRtree, Geometry targetGeom) {
        Envelope search = targetGeom.getEnvelopeInternal();
        double searchDistance = 100;
        List<Node> result;
        // ???????????????????????????
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
            } else {
                break;
            }
        }
        // ?????????????????????
        Node clostestPoint = result.get(0);
        double distance = targetGeom.distance(clostestPoint.point);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).point);
            if (distance > d) {
                distance = d;
                clostestPoint = result.get(i);
            }
        }
        return clostestPoint;
    }

    public static Point searchClosestPoint(STRtree stRtree, Geometry targetGeom) {
        Envelope search = targetGeom.getEnvelopeInternal();
        double searchDistance = 1600;
        List<Point> result;
        // ???????????????????????????
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
            } else {
                break;
            }
        }
        // ?????????????????????
        Point clostestPoint = result.get(0);
        double distance = targetGeom.distance(clostestPoint);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i));
            if (distance > d) {
                distance = d;
                clostestPoint = result.get(i);
            }
        }
        return clostestPoint;
    }

    public static Edge searchClosestEdge(STRtree stRtree, Geometry targetGeom) {
        Envelope search = targetGeom.getEnvelopeInternal();
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        double searchDistance = 300;
        List<Edge> result;
        // ???????????????????????????
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
//                if(searchDistance > 3200){
//                    return null;
//                }
            } else {
                break;
            }
        }
        // ?????????????????????
        Edge clostestPoint = result.get(0);
        double distance = targetGeom.distance(clostestPoint.lineString);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).lineString);
            if (distance > d) {
                distance = d;
                clostestPoint = result.get(i);
            }
        }
        return clostestPoint;
    }


    public static Edge searchClosestHasDataEdge(STRtree stRtree, Geometry targetGeom) {
        Envelope search = targetGeom.getEnvelopeInternal();
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        double searchDistance = 300;
        List<Edge> result;
        // ???????????????????????????
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
//                if(searchDistance > 3200){
//                    return null;
//                }
            } else {
                result = result.stream().filter(edge -> edge.eNum != 0).collect(Collectors.toList());
                if(result.size()==0){
                    searchDistance *= 2;
                }else{
                    break;
                }
            }
        }
        // ?????????????????????
        Edge clostestPoint = result.get(0);
        double distance = targetGeom.distance(clostestPoint.lineString);
        for (int i = 1; i < result.size(); i++) {
            double d = targetGeom.distance(result.get(i).lineString);
            if (distance > d) {
                distance = d;
                clostestPoint = result.get(i);
            }
        }
        return clostestPoint;
    }

    public static <T extends Feature,U extends Object > List<SimpleFeature> addColumn(
            SimpleFeatureCollection fc, String columnName, Map<T, U> featureValueMap) throws IOException {
        if(columnName.length() >= 10){
            throw new IllegalArgumentException("column???????????????10?????????");
        }

        // create new schema
        SimpleFeatureType schema = fc.getSchema();
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(schema.getName());
        builder.setSuperType((SimpleFeatureType) schema.getSuper());
        builder.addAll(schema.getAttributeDescriptors());
        // add new attribute(s)
        builder.add(columnName, Double.class);
        // build new schema
        SimpleFeatureType nSchema = builder.buildFeatureType();

        // loop through features adding new attribute
        List<SimpleFeature> features = new ArrayList<>();
        fc.accepts(f -> {
            SimpleFeature f2 = DataUtilities.reType(nSchema, (SimpleFeature) f);
            Object value = featureValueMap.get(f);
            f2.setAttribute(columnName, value);
            features.add(f2);
        },null);
        return features;
    }

    public static void saveFeatures(List<SimpleFeature> features, String saveFilePath) throws IOException {
        if (features.size() == 0) {
            return;
        }
        File newFile = getNewFile(saveFilePath);

        // ???????????????????????????????????????
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        // ????????????????????????
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        // ????????????????????????????????????
        ShapefileDataStore newDataStore =
                (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        // ?????????????????? ??????????????????
        newDataStore.createSchema(features.get(0).getFeatureType());

        // FeatureStore??????????????????????????????create
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0]; // points
        // ?????????
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection collection = new ListFeatureCollection(features.get(0).getFeatureType(), features);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
        } else {
            System.out.println(typeName + " does not support read/write access");
        }
    }


    public static File getNewFile(String filePath) throws IOException {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
            if (newFile) {
                return file;
            }
        } else {
            int i = 1;
            while (true) {
                String nameFilePath = file.getPath().split("\\.")[0] + "(" + i + ")" + ".shp";
                File newFile = new File((nameFilePath));
                if (!newFile.exists()) {
                    boolean b = newFile.createNewFile();
                    if (b) {
                        return newFile;
                    }
                }
                i++;
            }
        }
        return null;
    }



    public static String wktToGeoJson(String wkt) {
        try {
            if (wkt != null) {
                Geometry geom = wktToGeom(wkt);
                if (geom != null) {
                    return geomToGeoJson(geom);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Geometry wktToGeom(String wkt) {
        if (wkt != null) {
            try {
                WKTReader reader = new WKTReader();
                return reader.read(wkt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    public static String geomToGeoJson(Geometry geom) {
        if (geom != null) {
            StringWriter writer = new StringWriter();
            GeometryJSON g = new GeometryJSON();
            try {
                g.write(geom, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return writer.toString();
        }
        return null;
    }

    public static String geoJsonToWkt(String geoJson) {
        if (geoJson != null) {
            Geometry geometry = geoJsonToGeom(geoJson);
            if (geometry != null) {
                return geomToWkt(geometry);
            }
        }
        return null;
    }


    public static Geometry geoJsonToGeom(String geoJson) {
        if (geoJson != null) {
            try {
                GeometryJSON gjson = new GeometryJSON();
                Reader reader = new StringReader(geoJson);
                return gjson.read(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public static String geomToWkt(Geometry geometry) {
        if (geometry != null) {
            return geometry.toText();
        }
        return null;
    }


    public static org.postgis.Geometry geomToPostGisGeom(Geometry geometry) {
        if (geometry != null) {
            String geometryType = geometry.getGeometryType();
            switch (geometryType) {
                case "Point":
                    Point oriPoint = (Point) geometry;
                    return new org.postgis.Point(oriPoint.getX(), oriPoint.getY());
            }
            return null;
        } else {
            return null;
        }
    }

    public static Geometry postGisGeomToGeom(org.postgis.Geometry postGisGeom) {
        if (postGisGeom != null) {
            StringBuffer wktBuffer = new StringBuffer();
            postGisGeom.outerWKT(wktBuffer);
            return wktToGeom(wktBuffer.toString());
        } else {
            return null;
        }
    }

    public static String allValueToStirng(Object object) {
        SerializeWriter out = new SerializeWriter();
        try {
            //????????????new??????SerializeConfig,???????????????????????????
            JSONSerializer serializer = new JSONSerializer(out, new SerializeConfig());
            serializer.getMapping().put(Date.class, new ObjectSerializer() {
                @Override
                public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
                    if (object == null) {
                        serializer.writeNull();
                    }
                    serializer.write(object.toString());
                }
            });
            serializer.write(object);
            return out.toString();
        } finally {
            out.close();
        }
    }
}
