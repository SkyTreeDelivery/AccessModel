package Access2.accessModel.databox;

import Access2.accessModel.dataPoint.DemandPoint;
import Access2.accessModel.dataPoint.PopPoint;
import Access2.accessModel.dataPoint.ResourcePoint;

import java.util.List;

/**
 *  保存可达性计算的所有数据点
 */
public class DataBox {
    // 资源点集合
    public List<ResourcePoint> resourcePoints;

    // 人口点集合
    public List<PopPoint> popPoints;

    // 需求点集合
    public List<DemandPoint> demandPoints;

    public DataBox() {
    }

    public DataBox(List<ResourcePoint> resourcePoints, List<PopPoint> popPoints, List<DemandPoint> demandPoints) {
        this.resourcePoints = resourcePoints;
        this.popPoints = popPoints;
        this.demandPoints = demandPoints;
    }
}