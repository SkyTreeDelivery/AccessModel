package Access2.utils;

import Access1.GeoUtils;
import Access2.accessModel.dataPoint.DemandPoint;
import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoFileUtils {

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

    public static void saveNodeFeature(List<DemandPoint> demandPoints, String filePath) throws SchemaException, IOException {
        if(demandPoints.isEmpty()){
            throw new IllegalArgumentException("demandPoint数组为空");
        }
        DemandPoint.DemandType type = demandPoints.get(0).type;
        if(!(type == DemandPoint.DemandType.NODE || type == DemandPoint.DemandType.GRID_FROM_POP)){
            throw new IllegalArgumentException("数据格式不正确");
        }

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
        demandPoints.stream()
                .forEach(demandPoint -> {
                    //添加geometry属性
                    featureBuilder.add(demandPoint.originalGeom);
                    //添加name属性
                    featureBuilder.add(demandPoint.access);
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

    public static void saveEdgeFeature(List<DemandPoint> demandPoints, String filePath) throws SchemaException, IOException {
        if(demandPoints.isEmpty()){
            throw new IllegalArgumentException("demandPoint数组为空");
        }
        DemandPoint.DemandType type = demandPoints.get(0).type;
        if(type != DemandPoint.DemandType.EDGE){
            throw new IllegalArgumentException("数据格式不正确");
        }

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
        demandPoints.stream()
                .forEach(demandPoint -> {
                    //添加geometry属性
                    featureBuilder.add(demandPoint.originalGeom);
                    //添加name属性
                    featureBuilder.add(demandPoint.access);
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

    public static void savePolygonFeature(List<DemandPoint> demandPoints, String filePath) throws SchemaException, IOException {
        if(demandPoints.isEmpty()){
            throw new IllegalArgumentException("demandPoint数组为空");
        }
        DemandPoint.DemandType type = demandPoints.get(0).type;
        if(!(type == DemandPoint.DemandType.POLYGON || type == DemandPoint.DemandType.GRID)){
            throw new IllegalArgumentException("数据格式不正确");
        }

        //设置要素的字段名称及其类型
        final SimpleFeatureType TYPE =
                DataUtilities.createType(
                        "Polygon",
                        "the_geom:MultiPolygon:srid=4547,"// geometry属性设置
                                + "cost:Double,"
                );
        //创建要素集合
        List<SimpleFeature> features = new ArrayList<>();
        //创建要素模板
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        demandPoints.stream()
                .forEach(demandPoint -> {
                    //添加geometry属性
                    featureBuilder.add(demandPoint.originalGeom);
                    //添加name属性
                    featureBuilder.add(demandPoint.access);
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
