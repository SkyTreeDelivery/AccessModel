package Access1;

import Access1.graph.Edge;
import Access1.graph.Graph;
import Access1.graph.Node;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.graph.structure.basic.BasicDirectedEdge;
import org.geotools.graph.structure.basic.BasicDirectedNode;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TransformUtils {
    private static Map<org.geotools.graph.structure.Node, Node> nodeMap;
    private static Map<org.geotools.graph.structure.Edge, Edge> edgeMap;

    // 预编译正则表达式
    private static Pattern pattern= Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
    private static GeometryFactory geometryFactory = new GeometryFactory();

    public static MathTransform transform_4326_to_3857;
    public static MathTransform transform_3857_to_4326;
    static {
        try {
            // 从wgs84坐标系转换到web墨卡托坐标系，通过静态对象共用转换器而优化执行效率（而不是在处理每个geometry时都创建转换器）
            CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
            CoordinateReferenceSystem crs4326 = factory.createCoordinateReferenceSystem("EPSG:4326");
            CoordinateReferenceSystem crs3857 = factory.createCoordinateReferenceSystem("EPSG:3857");
            // 投影转换，如果GeoTools不支持该投影转换（缺少转换参数）会报缺少参数异常。
            transform_4326_to_3857 = CRS.findMathTransform(crs4326, crs3857);
            transform_3857_to_4326 = CRS.findMathTransform(crs3857, crs4326);
        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    public static Graph geoToolsGraphToMyGraph(org.geotools.graph.structure.Graph geoGraph){
        nodeMap = new HashMap<org.geotools.graph.structure.Node, Node>(geoGraph.getNodes().size());
        edgeMap = new HashMap<org.geotools.graph.structure.Edge, Edge>(geoGraph.getEdges().size());

        // 首先将所有的geotools的node对象转换为自定义node对象
        Iterator<BasicDirectedNode> nodeIter = geoGraph.getNodes().iterator();
        while (nodeIter.hasNext()){
            BasicDirectedNode node = nodeIter.next();
            Point point = (Point) node.getObject();
            Node myNode = new Node(node.getID(), point.getX(), point.getY());
            nodeMap.put(node,myNode);
        }

        // 将所有的geotools的Edge对象转换为自定义Edge对象，同时将自定义Node装入到自定义Edge中
        Iterator<BasicDirectedEdge> iterator = geoGraph.getEdges().iterator();
        while(iterator.hasNext()){
            BasicDirectedEdge edge = iterator.next();
            org.geotools.graph.structure.Node in = edge.getInNode();
            org.geotools.graph.structure.Node out = edge.getOutNode();
            SimpleFeature feature = (SimpleFeature) edge.getObject();
            MultiLineString multiLineString = (MultiLineString) feature.getDefaultGeometry();
            LineString lineString = (LineString) multiLineString.getGeometryN(0);
            Edge myEdge = new Edge(edge.getID(), nodeMap.get(in), nodeMap.get(out),lineString.getLength(), lineString.getLength(),lineString);
            edgeMap.put(edge,myEdge);
        }

        //将node对象的出入边处理好
        nodeIter = geoGraph.getNodes().iterator();
        while (nodeIter.hasNext()){
            BasicDirectedNode node = nodeIter.next();
            Node myNode = nodeMap.get(node);
            List<BasicDirectedEdge> inEdges = node.getInEdges();
            for (BasicDirectedEdge edge : inEdges) {
                myNode.inEdges.add(edgeMap.get(edge));
            }
            List<BasicDirectedEdge> outEdges = node.getOutEdges();
            for (BasicDirectedEdge edge : outEdges) {
                myNode.outEdges.add(edgeMap.get(edge));
            }
        }

        // 将nodeMap和EdgeMap中的数据集中到自定义Graph中
        Graph graph = new Graph(new HashSet<Edge>(new HashSet<Edge>(edgeMap.values())), new HashSet<Node>(nodeMap.values()));

        return graph;
    }

    public static Graph generateGraphFromCSV(String linkCSVPath, String nodeCSVPath){
        GeometryFactory geometryFactory = new GeometryFactory();
        WKTReader wktReader = new WKTReader();

        HashMap<Integer,Node> nodeMap = new HashMap<Integer, Node>();
        try {
            HashSet<Edge> edges = new HashSet<Edge>();
            HashSet<Node> nodes = new HashSet<Node>();
            // 生成node对象。
            Stream<String> nodeStream = Files.lines(Paths.get(nodeCSVPath));
            final int[] i = {0};
            nodeStream.skip(1).forEach(line->{
                String[] data = pattern.split(line);
                Integer linkId = Integer.parseInt(data[0]);
                Point point = null;
                try {
                    point = (Point) wktReader.read(data[3]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Node node = new Node(linkId, point);
                nodeMap.put(linkId,node);
                nodes.add(node);
            });

            // 生成link对象。
            Stream<String> linkStream = Files.lines(Paths.get(linkCSVPath));
            linkStream.skip(1).parallel().forEach(line->{
                String[] data = pattern.split(line);
                Integer linkId = Integer.parseInt(data[0]);
                String name = data[1];
                Node from = nodeMap.get(Integer.parseInt(data[2]));
                Node to = nodeMap.get(Integer.parseInt(data[3]));
                LineString lineString = null;
                try {
                    lineString = (LineString) wktReader.read(data[11].substring(1,data[11].length()-1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                double length = lineString.getLength();
                double freeSpeed = Integer.parseInt(data[6]);
                double transitTime = (length / 1000) / freeSpeed * 60; // 通行时间单位：分钟
                Edge edge = new Edge(linkId, from, to, transitTime, length,lineString);
                from.outEdges.add(edge);
                to.inEdges.add(edge);
                edges.add(edge);
                edge.isTop = true;
            });

            return new Graph(edges, nodes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends Geometry> T transform(T geom, MathTransform transform){
        try {
            return (T) JTS.transform(geom, transform);
        } catch (TransformException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveEdgeFeature(Map<Edge, Double> edgeDataMap, String filePath) throws SchemaException, IOException {
        //设置要素的字段名称及其类型
        final SimpleFeatureType TYPE =
                DataUtilities.createType(
                        "SubEdge",
                        "the_geom:LineString:srid=4547,"// geometry属性设置
                                + "cost:Double,"
                );
        //创建要素集合
        List<SimpleFeature> features = new ArrayList<>();
        //创建要素模板
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        edgeDataMap.entrySet()
            .forEach(edgeDoubleEntry -> {
                Edge edge = edgeDoubleEntry.getKey();
                Double cost = edgeDoubleEntry.getValue();
                //添加geometry属性
                featureBuilder.add(edge.lineString);
                //添加name属性
                featureBuilder.add(cost);
                //构建要素
                SimpleFeature feature = featureBuilder.buildFeature(null);
                //将要素添加到要素几何中
                features.add(feature);
            });
        File newFile = GeoUtils.getNewFile(filePath);
        //创建shapefileDataStore工厂
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        //参数设置
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        //根据关键字创建shapefileDataStore
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        //设置编码，防止中文乱码
        Charset charset = StandardCharsets.UTF_8;
        newDataStore.setCharset(charset);
        //创建文件描述内容
        newDataStore.createSchema(TYPE);

        // FeatureStore事务控制，这里是创建create
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0]; // points
        // 要素源
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
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


    public static void saveNodeFeature(Map<Node, Double> nodeDataMap, String filePath) throws SchemaException, IOException {
        //设置要素的字段名称及其类型
        final SimpleFeatureType TYPE =
                DataUtilities.createType(
                        "SubEdge",
                        "the_geom:Point:srid=4547,"// geometry属性设置
                                + "cost:Double,"
                );
        //创建要素集合
        List<SimpleFeature> features = new ArrayList<>();
        //创建要素模板
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        nodeDataMap.entrySet()
                .parallelStream().forEach(nodeDoubleEntry -> {
                    Node node = nodeDoubleEntry.getKey();
                    Double cost = nodeDoubleEntry.getValue();
                    //添加geometry属性
                    featureBuilder.add(node.point);
                    //添加name属性
                    featureBuilder.add(cost);
                    //构建要素
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    //将要素添加到要素几何中
                    features.add(feature);
                });
        File newFile = GeoUtils.getNewFile(filePath);
        //创建shapefileDataStore工厂
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        //参数设置
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        //根据关键字创建shapefileDataStore
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        //设置编码，防止中文乱码
        Charset charset = StandardCharsets.UTF_8;
        newDataStore.setCharset(charset);
        //创建文件描述内容
        newDataStore.createSchema(TYPE);

        // FeatureStore事务控制，这里是创建create
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0]; // points
        // 要素源
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
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
}
