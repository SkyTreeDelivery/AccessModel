import Graph.Edge;
import Graph.Node;
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
//        // 使用linestring的link_id属性替换Graph生成的默认id，
//        // 使用linestring的fromnode和tonode的id替换graph中nodeA和NodeB的id
//        Collection<Edge> edges = graph.getEdges();
//        for (Edge edge : edges) {
//            SimpleFeature feature = (SimpleFeature) edge.getObject();
//            // 替换edge的id
//            String link_id = (String) feature.getAttribute("LINK_ID");
//            edge.setID(Integer.parseInt(link_id));
//            org.geotools.graph.structure.Node nodeA = edge.getNodeA();
//            org.geotools.graph.structure.Node nodeB = edge.getNodeB();
//            // 替换node的id
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
        // calculate()方法计算了从源点到其他所有点的最短路径。这一步最耗时。
        LocalDateTime before = LocalDateTime.now();
        pf.calculate();
        LocalDateTime after = LocalDateTime.now();
        //输出计算耗时
        System.out.println("geotools计算出的Dijkstra路径耗时：" + Duration.between(before, after).toMillis());
        return pf;
    }

    /**
     * 生成R树
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
        System.out.println("str树深度：" + stRtree.getRoot().getLevel());
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

    /**
     *  已过时
     * @param stRtree
     * @param feature
     * @return
     */
    public static SimpleFeature searchClosestNode(STRtree stRtree, SimpleFeature feature) {
        Point target = (Point) feature.getDefaultGeometry();
        Envelope search = new Envelope(target.getCoordinate());
        double searchDistance = 0.001;
        List<SimpleFeature> result;
        // 查找出待处理节点。
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
            } else {
                break;
            }
        }
        // 找到最近的节点
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
        // 查找出待处理节点。
        while (true) {
            search.expandBy(searchDistance);
            result = stRtree.query(search);
            if (result.size() == 0) {
                searchDistance *= 2;
            } else {
                break;
            }
        }
        // 找到最近的节点
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

    public static Edge searchClosestEdge(STRtree stRtree, Geometry targetGeom) {
        Envelope search = targetGeom.getEnvelopeInternal();
        // 搜索半径，设置合适的搜索半径可以大幅提高执行速度。因为空间查询是一个比较耗时的操作
        double searchDistance = 300;
        List<Edge> result;
        // 查找出待处理节点。
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
        // 找到最近的节点
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

    public static List<SimpleFeature> addColumn(SimpleFeatureCollection fc,
                                                String columnName, Map<Feature, Object> featureValueMap) throws IOException {
        if(columnName.length() >= 10){
            throw new IllegalArgumentException("column名不能超过10个字符");
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

        // 数据仓库工厂，生成数据仓库
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        // 数据仓库生成参数
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        // 数据仓库工厂生成数据仓库
        ShapefileDataStore newDataStore =
                (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        // 定义数据仓库 数据要素类型
        newDataStore.createSchema(features.get(0).getFeatureType());

        // FeatureStore事务控制，这里是创建create
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0]; // points
        // 要素源
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
            //此处必须new一个SerializeConfig,防止修改默认的配置
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
