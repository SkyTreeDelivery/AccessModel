import Access1.GeoUtils;
import Access1.GraphHandleUtils;
import Access1.TransformUtils;
import Access1.graph.Edge;
import Access1.graph.Graph;
import Access1.graph.Node;
import Access1.graph.ShortestPath.Dijkstra.DijkstraPathFinder;
import Access1.graph.ShortestPath.MathFunc;
import Access1.graph.kde.KDE;
import Access1.graph.kde.Poi;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class MainTest {

    private HashSet<Poi> poiSet;
    private Graph myGraph;
    private List<Edge> subEdgeList;
    private String dirPath;
    private String poiPath;
    private Map<SimpleFeature, Edge> popPointEdgeMap;
    private SimpleFeatureCollection pops;
    private STRtree popRtree;
    private String subDirName;
    private Map<Edge, SimpleFeature> edgeSimpleFeatureMap;
    private Map<Point, Edge> centerSubEdgeMap;

    ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Test
    public void accessibilityTest() throws Exception {

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

        int[] costThresholdArray = IntStream.rangeClosed(2,4).map(i -> i * 15).toArray();
        costThresholdArray[0] = -1;

        int[] bandWithArray = IntStream.rangeClosed(1, 8).map(i -> i * 2).toArray();

        // 剖分长度 单位：m
        double SPLIT_LENGTH = 200;

        // link.csv文件与node.csv文件存储的文件夹路径
        dirPath = "E:\\Data\\可达性研究\\Accessibility_wuhan_投影";

        // 结果输出Dir
        subDirName = "优化人口附着_清除无效子Graph_" +
                "cost_" + costThresholdArray.length +
                "band_" + bandWithArray.length;

        poiPath = "E:\\Data\\可达性研究\\医院数据\\二三级医院_加权_CGCS2000_114E_all.shp";

        LocalDateTime start = LocalDateTime.now();

        init(SPLIT_LENGTH);

        int account = costThresholdArray.length * bandWithArray.length;
        int num = 0;
        for (int i = 0; i < costThresholdArray.length; i++) {
            for (int j = 0; j < bandWithArray.length; j++) {

                // 剪枝阈值 单位：min
                double COST_THRESHOLD = costThresholdArray[i];

                // 带宽，单位：m
                double BAND_WITH = bandWithArray[j];

                System.out.println("-------------------------");
                System.out.println("start | 第" + ++num + "次计算");

                calculate(COST_THRESHOLD, BAND_WITH);

                LocalDateTime now = LocalDateTime.now();

                System.out.println("-------------------------");
                System.out.println("已完成：" + num + "/" + account + " = " + num * 1.0 / account);
                System.out.println("已耗时：" + Duration.between(start, now).toMinutes() + "min");
                System.out.println("-------------------------");
            }
        }

        LocalDateTime end = LocalDateTime.now();
        System.out.println("总耗时：" + Duration.between(start, end).toMinutes() + "min");
    }


    public void init(final double SPLIT_LENGTH) throws Exception {
        System.out.println("------------- 初始化 -------------------");

        LocalDateTime before = LocalDateTime.now();
        // 生成自定义Graph
        myGraph = TransformUtils.generateGraphFromCSV(dirPath + "\\link.csv"
                , dirPath + "\\node.csv");
        myGraph = GraphHandleUtils.extractMaxGraph(myGraph);
        Map<Integer, Node> myNodeMap = myGraph != null ? myGraph.nodes.parallelStream()
                .collect(toMap(e -> e.id, e -> e)) : null;
        LocalDateTime after = LocalDateTime.now();
        System.out.println("step1 | graph构建耗时：" + Duration.between(before, after).toMillis() + "ms");

        // 处理人口数据
        // 读取人口数据
        before = LocalDateTime.now();
        FeatureSource<SimpleFeatureType, SimpleFeature> popShpSource =
                GeoUtils.generateShapefileSource("E:\\Data\\可达性研究\\人口数据\\武汉市人口点_CGCS2000_114E.shp");
        pops = (SimpleFeatureCollection)popShpSource.getFeatures();
        SimpleFeature[] popFeatures = pops.toArray(new SimpleFeature[0]);
        popRtree = GeoUtils.generatePopRtree(Arrays.stream(popFeatures).collect(Collectors.toSet()));

//        // 构建edge空间索引
//        STRtree edgeRtree = GeoUtils.generateEdgeRtree(myGraph.edges);
//        // 将人口数据附着到edge上
//        // 将人口数据附着运算并行化，可以大幅度提升执行速度
//        popPointEdgeMap = new ConcurrentHashMap<SimpleFeature, Edge>(pops.size());
//        Arrays.stream(pops.toArray(new SimpleFeature[0])).parallel()
//                .forEach(popFeature -> {
//                    SimpleFeature simpleFeature = (SimpleFeature) popFeature;
//                    Edge edge = GeoUtils.searchClosestEdge(edgeRtree, (Geometry) simpleFeature.getDefaultGeometry());
//                    popPointEdgeMap.put(simpleFeature,edge);
//                    if (edge == null) return;
//                    double popnum = (double) simpleFeature.getAttribute("grid_code");
//                    edge.pop += popnum;
//                });
//
//        after = LocalDateTime.now();
//        System.out.println("step3 | 附着人口数据耗时：" + Duration.between(before, after).toMillis() + "ms");

        // 分割subEdge
        before = LocalDateTime.now();
        assert myGraph != null;
        myGraph.edges
                .parallelStream().forEach(edge -> edge.splitEdge(SPLIT_LENGTH));
        after = LocalDateTime.now();

        subEdgeList = myGraph.edges.parallelStream()
                .flatMap(edge -> edge.subEdges.stream())
                .collect(toList());

        // subEdge的中点
        centerSubEdgeMap = subEdgeList.parallelStream()
                .collect(toMap(subEdge -> subEdge.center, subEdge -> subEdge,(a,b)-> a));

        System.out.println("step2 | 分割edge耗时：" + Duration.between(before, after).toMillis() + "ms");

        // 人口附着
        before = LocalDateTime.now();
        // 构建edge空间索引
//        STRtree subEdgeRtree = GeoUtils.generateEdgeRtree(new HashSet<Edge>(subEdgeList));
        STRtree centerRtree = GeoUtils.generatePointRtree(centerSubEdgeMap.keySet());
        // 将人口数据附着到edge上
        // 将人口数据附着运算并行化，可以大幅度提升执行速度
        popPointEdgeMap = new ConcurrentHashMap<SimpleFeature, Edge>(pops.size());
        int threadNum = 8;
        int oneThreadTaskNum = popFeatures.length / threadNum;
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            int start = oneThreadTaskNum * i;
            int end = i != threadNum - 1 ? oneThreadTaskNum * (i+1) : popFeatures.length;
            Runnable task = ()->{
                for (int j = start; j < end; j++) {
                    SimpleFeature popFeature = popFeatures[j];
                    SimpleFeature simpleFeature = (SimpleFeature) popFeature;
                    Geometry point = (Geometry) simpleFeature.getDefaultGeometry();
//                    Edge subEdge = GeoUtils.searchClosestEdge(subEdgeRtree, point);
                    Point center = GeoUtils.searchClosestPoint(centerRtree, point);
                    Edge subEdge = centerSubEdgeMap.get(center);
                    popPointEdgeMap.put(simpleFeature,subEdge);
                    if (subEdge == null) return;
                    double popnum = (double) simpleFeature.getAttribute("grid_code");
                    subEdge.pop += popnum;
                }
                countDownLatch.countDown();
            };
            executorService.submit(task);
        }
        countDownLatch.await();


//        Arrays.stream(popFeatures).parallel()
//                .forEach(popFeature -> {
//                    SimpleFeature simpleFeature = (SimpleFeature) popFeature;
//                    Geometry point = (Geometry) simpleFeature.getDefaultGeometry();
////                    Edge subEdge = GeoUtils.searchClosestEdge(subEdgeRtree, point);
//                    Point center = GeoUtils.searchClosestPoint(centerRtree, point);
//                    Edge subEdge = centerSubEdgeMap.get(center);
//                    popPointEdgeMap.put(simpleFeature,subEdge);
//                    if (subEdge == null) return;
//                    double popnum = (double) simpleFeature.getAttribute("grid_code");
//                    subEdge.pop += popnum;
//                });
        after = LocalDateTime.now();
        System.out.println("step3 | 附着人口数据耗时：" + Duration.between(before, after).toMillis() + "ms");


        // 生成空间索引
        before = LocalDateTime.now();
        STRtree nodeRtree = GeoUtils.generateNodeRtree(myGraph.nodes);
        after = LocalDateTime.now();
        System.out.println("step4 | 构建node空间索引耗时：" + Duration.between(before, after).toMillis() + "ms");

        before = LocalDateTime.now();
        // 读取poi数据
        FeatureSource<SimpleFeatureType, SimpleFeature> poiSource =
                GeoUtils.generateShapefileSource(poiPath);
        FeatureCollection<SimpleFeatureType, SimpleFeature> pois = poiSource.getFeatures();
        poiSet = new HashSet<>(pois.size());
        pois.accepts(poiFeature -> {
            SimpleFeature simpleFeature = (SimpleFeature) poiFeature;
            Point point = (Point) simpleFeature.getDefaultGeometry();
            Node closestNode = GeoUtils.searchClosestNode(nodeRtree, point);
            Object data = simpleFeature.getAttribute("weight");
            double weight = 0.0;
            if(data instanceof Long){
                weight = ((Long)data).intValue();
            }else if(data instanceof Double){
                weight = ((Double)data).intValue();
            }
            Poi poi = new Poi(point, closestNode, weight);
            poiSet.add(poi);
        }, null);
        after = LocalDateTime.now();
        System.out.println("step5 | 读取poi数据耗时：" + Duration.between(before, after).toMillis() + "ms");

        before = LocalDateTime.now();
//        // 根据poi点的数量初始化subEdge和Node用于存储costDis的double数组
//        myGraph.nodes.parallelStream().forEach(node -> node.costDisArray = new double[pois.size()]);
//        myGraph.nodes.parallelStream().forEach(node -> node.pois = new Poi[pois.size()]);

        subEdgeList.parallelStream().forEach(subEdge -> subEdge.subEdgeCostDisArray = new double[pois.size()]);
        subEdgeList.parallelStream().forEach(subEdge -> subEdge.pois = new Poi[pois.size()]);
        after = LocalDateTime.now();
        System.out.println("step6 | 准备可达性存储数据结构耗时：" + Duration.between(before, after).toMillis() + "ms");

    }


    public void calculate(final double COST_THRESHOLD,
                          final double BAND_WITH) throws Exception {

        // 初始化容器
        clearDisArray();
        // 计算所有资源点到全部其他点的距离
        LocalDateTime before = LocalDateTime.now();
        poiSet.parallelStream().forEach(poi -> {
            DijkstraPathFinder dijkstraPathFinder = new DijkstraPathFinder(myGraph, poi, COST_THRESHOLD);
            dijkstraPathFinder.calculate();
        });
        LocalDateTime after = LocalDateTime.now();
        System.out.println("step7 | 计算最短路径耗 + 路网插值时：" + Duration.between(before, after).toMillis() + "ms");

//        before = LocalDateTime.now();
//        Map<Node, Double> collect = myGraph.nodes.parallelStream()
//                .collect(toMap(node -> node, node -> {
//                    if (node.nNum == 0) return 0.0;
//                    double[] disArray = new double[node.nNum];
//                    System.arraycopy(node.costDisArray, 0, disArray, 0, node.nNum);
//                    for (int i = 0; i < disArray.length; i++) {
//                        disArray[i] *= TIME_FACTOR;
//                    }
//                    Poi[] poiArray = new Poi[node.nNum];
//                    System.arraycopy(node.pois, 0, poiArray, 0, node.nNum);
//                    double result = KDE.kde_pop_resource_weight(disArray
//                            , BAND_WITH, KDE.Kernal.GUASS, poiArray);
//                    if(MathFunc.doubleIsLegal(result)){
//                        return result;
//                    }
//                    return 0.0;
//                }));
//        after = LocalDateTime.now();
//        System.out.println("step8 | node 可达性计算耗时：" + Duration.between(before, after).toMillis() + "ms");
//
//        before = LocalDateTime.now();
//        String filePath = dirPath +
//                "\\计算结果\\组合结果_" +
//                subDirName + "\\node\\" +
//                "_" + BAND_WITH + "m" +
//                "_" + COST_THRESHOLD + "min_" +
//                ".shp";
//        TransformUtils.saveNodeFeature(collect, filePath);
//        after = LocalDateTime.now();
//        System.out.println("step9 | 存储路网节点耗时：" + Duration.between(before,after).toMillis() + "ms");

        // 对路网剖分结构进行插值，并计算核密度可达性
        before = LocalDateTime.now();
        Map<Edge, Double> subEdgeCostMap = subEdgeList.parallelStream()
                .collect(toMap(subEdge -> subEdge, subEdge ->{
                    if(subEdge.eNum == 0) return 0.0;
                    double[] disArray = new double[subEdge.eNum];
                    System.arraycopy(subEdge.subEdgeCostDisArray, 0, disArray, 0, subEdge.eNum);
                    Poi[] poiArray = new Poi[subEdge.eNum];
                    System.arraycopy(subEdge.pois, 0, poiArray, 0, subEdge.eNum);
                    return KDE.kde_pop_resource_weight(disArray
                            , BAND_WITH, KDE.Kernal.GUASS, poiArray);
                }));
        after = LocalDateTime.now();
        System.out.println("step8 | 路网可达性计算耗时：" + Duration.between(before,after).toMillis() + "ms");

        // 存储路网数据结构
        before = LocalDateTime.now();
        String filePath = dirPath +
                "\\计算结果\\组合结果_" +
                subDirName + "\\link\\" +
                "_" + BAND_WITH + "m" +
                "_" + COST_THRESHOLD + "min_" +
                ".shp";

        TransformUtils.saveEdgeFeature(subEdgeCostMap, filePath);
        after = LocalDateTime.now();
        System.out.println("step9 | 存储路网剖分结构耗时：" + Duration.between(before,after).toMillis() + "ms");

        // 计算栅格可达性
        before = LocalDateTime.now();
        Map<SimpleFeature, Double> coverage = popPointEdgeMap.entrySet().parallelStream()
                .collect(toMap(entry -> entry.getKey(), entry -> {
                    SimpleFeature key = entry.getKey();
                    Edge subEdge = entry.getValue();
                    double cost = subEdgeCostMap.get(subEdge);
                    Point popPoint = (Point) key.getDefaultGeometry();
                    double popPointToEdgeDis = popPoint.distance(subEdge.lineString);
                    double popPointToEdgeCost =  (popPointToEdgeDis / 1000) / DijkstraPathFinder.defaultWalkSpeed * 60;

                    if(subEdge.eNum == 0) return 0.0;
                    double[] disArray = new double[subEdge.eNum];
                    System.arraycopy(subEdge.subEdgeCostDisArray, 0, disArray, 0, subEdge.eNum);
                    for (int i = 0; i < disArray.length; i++) {
                        disArray[i] += popPointToEdgeCost;
                    }
                    Poi[] poiArray = new Poi[subEdge.eNum];
                    System.arraycopy(subEdge.pois, 0, poiArray, 0, subEdge.eNum);
                    return KDE.kde_pop_resource_weight(disArray
                            , BAND_WITH, KDE.Kernal.GUASS, poiArray);
                }));

        after = LocalDateTime.now();
        System.out.println("step10 | 栅格可达性计算耗时：" + Duration.between(before,after).toMillis() + "ms");


        List<SimpleFeature> simpleFeatureList = GeoUtils.addColumn(pops, "cost", coverage);
        filePath = dirPath +
                "\\计算结果\\组合结果_" +
                subDirName + "\\pop\\" +
                "" + (int)BAND_WITH + "m" +
                "_" + (int)COST_THRESHOLD + "min" +
                ".shp";
        GeoUtils.saveFeatures(simpleFeatureList,filePath);
    }

    @Test
    public void normalizeTest() {
        double[] normalize = MathFunc.normalize(new double[]{-1.0, 1.0, 2.0, 3.0, 40.0});
        System.out.println(Arrays.toString(normalize));
    }

    @Test
    public void testDoubleisLegal(){
        System.out.println(MathFunc.doubleIsLegal(-1.0/0.0));
    }

    public void clearDisArray() {
//        for (Node node : myGraph.nodes) {
//            Arrays.fill(node.costDisArray, 0);
//            Arrays.fill(node.pois, null);
//            node.nNum = 0;
//        }
        subEdgeList.forEach(edge -> {
            Arrays.fill(edge.subEdgeCostDisArray, 0);
            Arrays.fill(edge.pois, null);
            edge.eNum = 0;
        });
    }

    @Test
    public void test4(){
        System.out.println(0.0/0 == 0.0/0);
    }

    @Test
    public void testGuass() {
        System.out.println(KDE.KernalFun.guass(0.0001));
    }

//    @Test
//    public void geoToolsGraphTest() throws Exception {
//
//        FeatureSource<SimpleFeatureType, SimpleFeature> source =
//                GeoUtils.generateShapefileSource("E:\\Data\\Accessibility\\路网数据\\路网数据.shp");
//        BasicDirectedGraph graph = (BasicDirectedGraph) GeoUtils.generateGraph(source);
//        System.out.println(graph.getEdges().size());
//        System.out.println(graph.getNodes().size());
//
//        Collection<DirectedNode> nodes = graph.getNodes();
//        ArrayList<DirectedNode> nodeArrayList = new ArrayList<>(nodes);
//        Map<Integer, DirectedNode> nodeMap = nodes.stream().collect(toMap(DirectedNode::getID, e -> e));
//        ArrayList<Integer> pathCountOfNotEmptys = new ArrayList<>(nodes.size());
//
//        DirectedNode node = nodeMap.get(5000);
//        DijkstraShortestPathFinder dijkstra = GeoUtils.dijkstra(graph, node, GeoUtils.getDefaultWeighter());
//        org.geotools.graph.path.Path path = dijkstra.getPath(nodeMap.get(24000));
//        List edges = path.getEdges();
//        System.out.println(edges.size());
//        System.out.println(
//                Arrays.toString(edges.toArray()));
//        System.out.println(path.getFirst());
//        System.out.println(path.getLast());
//
//        Graph myGraph = TransformUtils.geoToolsGraphToMyGraph(graph);
//        Map<Integer, Node> myNodeMap = myGraph.nodes.parallelStream()
//                .collect(toMap(e -> e.id, e -> e));
//        DijkstraPathFinder dijkstraPathFinder = new DijkstraPathFinder(myGraph, myNodeMap.get(5000));
//        LocalDateTime before = LocalDateTime.now();
//        DijkstraResult result = dijkstraPathFinder.calculate_pure();
//        LocalDateTime after = LocalDateTime.now();
//        System.out.println("我的算法的执行时间" + Duration.between(before,after).toMillis());;
//
//        Path mypath = result.generatePath(myNodeMap.get(24000));
//        System.out.println(Arrays.toString(mypath.nodes.toArray()));
//        System.out.println(Arrays.toString(mypath.edges.toArray()));
//
//    }
}
