package Access2.accessModel;

import Access2.accessModel.dataPoint.DemandPoint;
import Access2.utils.GeoFileUtils;
import org.geotools.feature.SchemaException;

import java.io.IOException;

public class AccessModelSaver {

    public static void save(String savePath, AccessModel accessModel) throws SchemaException, IOException {
        DemandPoint.DemandType type = accessModel.dataBox.demandPoints.get(0).type;
        if(type == DemandPoint.DemandType.NODE){
            GeoFileUtils.saveNodeFeature(accessModel.dataBox.demandPoints, savePath);
        }else if(type == DemandPoint.DemandType.EDGE){
            GeoFileUtils.saveEdgeFeature(accessModel.dataBox.demandPoints, savePath);
        }
    }
}
