package Access2.accessModel;

import Access2.accessModel.dataPoint.DemandPoint;
import Access2.utils.GeoFileUtils;
import org.geotools.feature.SchemaException;

import java.io.IOException;

public class AccessModelSaver {

    /**
     *  将可达性模型的计算结果存储为shp文件
     * @param savePath 文件存储路径
     * @param accessModel 已经计算好的可达性模型
     * @throws SchemaException
     * @throws IOException
     */
    public static void save(String savePath, AccessModel accessModel) throws SchemaException, IOException {
        DemandPoint.DemandType type = accessModel.dataBox.demandPoints.get(0).type;
        if(type == DemandPoint.DemandType.NODE){
            GeoFileUtils.saveNodeFeature(accessModel.dataBox.demandPoints, savePath);
        }else if(type == DemandPoint.DemandType.EDGE){
            GeoFileUtils.saveEdgeFeature(accessModel.dataBox.demandPoints, savePath);
        }else if(type == DemandPoint.DemandType.POLYGON){
            GeoFileUtils.savePolygonFeature(accessModel.dataBox.demandPoints, savePath);
        }else if(type == DemandPoint.DemandType.GRID_FROM_POP)   {
            GeoFileUtils.saveNodeFeature(accessModel.dataBox.demandPoints, savePath);
        }else if(type == DemandPoint.DemandType.GRID){
            GeoFileUtils.savePolygonFeature(accessModel.dataBox.demandPoints, savePath);
        }
    }
}
