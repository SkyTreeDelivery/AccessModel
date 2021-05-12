import Access2.accessModel.AccessModel;
import Access2.accessModel.AccessModelFactory;
import Access2.accessModel.AccessModelSaver;
import Access2.accessModel.databox.DataBox;
import Access2.accessModel.databox.DataBoxFactory;
import Access2.accessModel.databox.DataBoxHandler;
import Access2.accessModel.strategy.Imp.AggregationFunImp;
import Access2.accessModel.strategy.Imp.DampingFunImp;
import Access2.graph.Graph;
import Access2.graph.GraphFactory;
import Access2.graph.GraphHandler;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Access2Test {

    // link.csv文件与node.csv文件存储的文件夹路径
    String dirPath = "E:\\Data\\可达性研究\\Accessibility_wuhan_重构";

    // 资源
    String poiPath = "E:\\Data\\可达性研究\\医院数据\\二三级医院_加权_CGCS2000_114E_all.shp";

    // 人口分布
    String popPath = "E:\\Data\\可达性研究\\人口数据\\武汉市人口点_CGCS2000_114E.shp";

    // 建筑数据
    String polygonPath = "E:\\Data\\可达性研究\\建筑数据\\武汉建筑_CGCS_2000_114E.shp";

    static {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "7");
    }

    double bandWith = 20;

    double splitLength = 50;

    private Graph graph;
    private DataBox dataBox;

    public void init() throws Exception {

        LocalDateTime before = LocalDateTime.now();
        Graph myGraph = GraphFactory.generateGraphFromCSV(dirPath + "\\link.csv"
                , dirPath + "\\node.csv");

//        graph = GraphHandler.extractMaxGraphWithoutHangingEdge(myGraph);
        graph = GraphHandler.extractMaxGraph(myGraph);

//        graph = GraphFactory.splitGraph(graph, splitLength);

        dirPath += "\\withoutHanging";

        LocalDateTime after = LocalDateTime.now();
        System.out.println("step1 | 构建路网耗时：" + Duration.between(before,after).toMillis() + "ms");

//        before = LocalDateTime.now();
//        dataBox = DataBoxFactory.getDataBox_poi_popGrid_nodes(poiPath, popPath, graph.nodes);
//        dirPath += "\\node\\不裁切";
//        after = LocalDateTime.now();
//        System.out.println("step2 | 读取数据耗时：" + Duration.between(before,after).toMillis() + "ms");

        before = LocalDateTime.now();
        dataBox = DataBoxFactory.getDataBox_poi_popGrid_edges(poiPath, popPath, this.graph.edges);
        dirPath += "\\Edge";
        after = LocalDateTime.now();
        System.out.println("step2 | 读取数据耗时：" + Duration.between(before,after).toMillis() + "ms");

//        before = LocalDateTime.now();
//        dataBox = DataBoxFactory.getDataBox_poi_popGrid_polygon(poiPath, popPath, polygonPath);
//        dirPath += "\\Polygon";
//        after = LocalDateTime.now();
//        System.out.println("step2 | 读取数据耗时：" + Duration.between(before,after).toMillis() + "ms");

//        before = LocalDateTime.now();
//        dataBox = DataBoxFactory.getDataBox_poi_popGrid_gridFromPop(poiPath,popPath);
//        dirPath += "\\Grid\\";
//        after = LocalDateTime.now();
//        System.out.println("step2 | 读取数据耗时：" + Duration.between(before,after).toMillis() + "ms");


        before = LocalDateTime.now();
        DataBoxHandler.attachDataPoint(this.graph, dataBox,1000,4000,4000, AccessModel.DEFAULT_WALK_SPEED);
        after = LocalDateTime.now();
        System.out.println("step3 | 数据点附着耗时：" + Duration.between(before,after).toMillis() + "ms");


//        IntStream.rangeClosed(1, 50).map(i -> i * 200).forEach(popSearchDistance ->{
//            try {
//                System.out.println("搜索半径：" + popSearchDistance + "m");
//
//                LocalDateTime before0 = LocalDateTime.now();
//                DataBoxHandler.attachDataPoint(this.graph, dataBox,3600,3600,popSearchDistance, AccessModel.DEFAULT_WALK_SPEED);
//                LocalDateTime after0 = LocalDateTime.now();
//                System.out.println("step3 | 数据点附着耗时：" + Duration.between(before0,after0).toMillis() + "ms");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    @Test
    public void kernel_basic_test() throws Exception {

        init();

        LocalDateTime before = LocalDateTime.now();

        AccessModel accessModel = AccessModelFactory.kernel_basic(
                graph,
                dataBox,
                DampingFunImp.parabolic,
                bandWith,
                AggregationFunImp.sum
        );

        accessModel.calculate();

        String filePath = dirPath +
                "\\kernel_basic_" +
                "带宽_" + bandWith +
                ".shp";

        LocalDateTime after = LocalDateTime.now();
        System.out.println("step4 | 可达性计算：" + Duration.between(before,after).toMillis() + "ms");
//        AccessModelSaver.save(filePath, accessModel);

    }

    @Test
    public void kernel_complete_test() throws Exception {

        init();

        LocalDateTime before = LocalDateTime.now();

        AccessModel accessModel = AccessModelFactory.kernel(
                graph,
                dataBox,
                DampingFunImp.parabolic,
                bandWith,
                AggregationFunImp.sum
        );

        accessModel.calculate();

        String filePath = dirPath +
                "\\kernel_complete_" +
                "带宽" + bandWith +
                ".shp";

        LocalDateTime after = LocalDateTime.now();
        System.out.println("step4 | 可达性计算：" + Duration.between(before,after).toMillis() + "ms");

        AccessModelSaver.save(filePath, accessModel);
    }

    @Test
    public void kernel_basic_batch_test() throws Exception {

        init();

        List<Integer> bandWiths = Arrays.asList(10, 15, 20);

        for (Integer bandWith : bandWiths) {
            AccessModel accessModel = AccessModelFactory.kernel_basic(
                    graph,
                    dataBox,
                    DampingFunImp.parabolic,
                    bandWith,
                    AggregationFunImp.sum
            );

            accessModel.calculate();

            String filePath = dirPath +
                    "\\kernel_basic_" +
                    "带宽_" + bandWith +
                    ".shp";
            AccessModelSaver.save(filePath, accessModel);
        }
    }

    @Test
    public void kernel_complete_batch_test() throws Exception {

        init();

        List<Integer> bandWiths = Arrays.asList(10, 15, 20);

        for (Integer bandWith : bandWiths) {
            AccessModel accessModel = AccessModelFactory.kernel(
                    graph,
                    dataBox,
                    DampingFunImp.parabolic,
                    bandWith,
                    AggregationFunImp.sum
            );

            accessModel.calculate();

            String filePath = dirPath +
                    "\\kernel_complete_" +
                    "带宽" + bandWith +
                    ".shp";
            AccessModelSaver.save(filePath, accessModel);
        }
    }

    @Test
    public void opportunity_accumulate_basic_test() throws Exception {
        init();

        double dis0 = 20;

        AccessModel accessModel = AccessModelFactory.opportunity_accumulate_basic(
                graph,
                dataBox,
                dis0,
                AggregationFunImp.sum
        );

        accessModel.calculate();

        String filePath = dirPath +
                "\\opportunity_accumulate_basic" +
                "dis0_" + dis0 +
                ".shp";
        AccessModelSaver.save(filePath, accessModel);
    }

    @Test
    public void opportunity_accumulate() throws Exception {

        init();

        Map<Double, Double> disFactorMap = new HashMap<>();
        disFactorMap.put(5.0,1.0);
        disFactorMap.put(10.0,2.0 / 3.0);
        disFactorMap.put(15.0,1.0 / 3.0);

        AccessModel accessModel = AccessModelFactory.opportunity_accumulate(
                graph,
                dataBox,
                disFactorMap,
                bandWith,
                AggregationFunImp.sum
        );

        accessModel.calculate();

        String filePath = dirPath +
                "\\opportunity_accumulate" +
                "带宽" + bandWith +
                ".shp";
        AccessModelSaver.save(filePath, accessModel);
    }



    @Test
    public void pointTest(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(1.1, 2.0));
        HashSet<Point> points = new HashSet<>();
        points.add(point);
        Point newPoint = geometryFactory.createPoint(new Coordinate(1.1, 2.0));
        System.out.println(points.contains(newPoint));
    }
}
