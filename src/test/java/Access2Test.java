import Access2.accessModel.*;
import Access2.accessModel.strategy.Imp.AggregationFunImp;
import Access2.accessModel.strategy.Imp.DampingFunImp;
import Access2.graph.Edge;
import Access2.graph.Graph;
import Access2.graph.GraphFactory;
import Access2.graph.GraphHandler;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Access2Test {

    // link.csv文件与node.csv文件存储的文件夹路径
    String dirPath = "E:\\Data\\可达性研究\\Accessibility_wuhan_重构";

    // 资源
    String poiPath = "E:\\Data\\可达性研究\\医院数据\\二三级医院_加权_CGCS2000_114E_all.shp";

    // 人口分布
    String popPath = "E:\\Data\\可达性研究\\人口数据\\武汉市人口点_CGCS2000_114E.shp";

    double bandWith = 10;

    private Graph graph;
    private DataBox dataBox;

    public void init() throws Exception {
        LocalDateTime before = LocalDateTime.now();
        Graph myGraph = GraphFactory.generateGraphFromCSV(dirPath + "\\link.csv"
                , dirPath + "\\node.csv");

        graph = GraphHandler.extractMaxGraph(myGraph);

        Set<Edge> edges = GraphHandler.splitEdge(graph.edges, 200);

        graph = GraphFactory.generateGraphFromEdges(edges);

        LocalDateTime after = LocalDateTime.now();
        System.out.println("step1 | 构建路网耗时：" + Duration.between(before,after).toMillis() + "ms");

//        before = LocalDateTime.now();
//        dataBox = DataBoxFactory.getDataBox_poi_popgrid_nodes(poiPath, popPath, graph.nodes);
//        dirPath += "\\node";
//        after = LocalDateTime.now();
//        System.out.println("step2 | 读取数据耗时：" + Duration.between(before,after).toMillis() + "ms");

        before = LocalDateTime.now();
        dataBox = DataBoxFactory.getDataBox_poi_popgrid_edges(poiPath, popPath, this.graph.edges);
        dirPath += "\\edge";
        after = LocalDateTime.now();
        System.out.println("step2 | 读取数据耗时：" + Duration.between(before,after).toMillis() + "ms");
    }

    @Test
    public void kernel_basic_test() throws Exception {

        init();

        DataBoxHandler.attachDataPointByWalk(graph, dataBox, AccessModel.DEFAULT_WALK_SPEED);

        AccessModel accessModel = AccessModelFactory.kernel(
                graph,
                dataBox,
                DampingFunImp.parabolic,
                bandWith,
                AggregationFunImp.sum
        );

        accessModel.calculate();

        String filePath = dirPath +
                "\\计算结果" +
                "带宽" + bandWith +
                ".shp";
        AccessModelSaver.save(filePath, accessModel);
    }

    @Test
    public void kernel_complete_test() throws Exception {
        init();

        DataBoxHandler.attachDataPointByWalk(graph, dataBox, AccessModel.DEFAULT_WALK_SPEED);

        AccessModel accessModel = AccessModelFactory.kernel_basic(
                graph,
                dataBox,
                DampingFunImp.parabolic,
                bandWith,
                AggregationFunImp.sum
        );

        accessModel.calculate();

        String filePath = dirPath +
                "\\计算结果" +
                "带宽_" + bandWith +
                ".shp";
        AccessModelSaver.save(filePath, accessModel);
    }

    @Test
    public void pointTest(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(1, 2));
        HashSet<Point> points = new HashSet<>();
        points.add(point);
        Point newPoint = geometryFactory.createPoint(new Coordinate(1, 2));
        System.out.println(points.contains(newPoint));
    }



}
