package Access2.accessModel;

import Access2.accessModel.dataPoint.DemandPoint;
import Access2.accessModel.dataPoint.PopPoint;
import Access2.accessModel.dataPoint.ResourcePoint;

import java.util.List;

public class DataBox {
    List<ResourcePoint> resourcePoints;
    List<PopPoint> popPoints;
    List<DemandPoint> demandPoints;

    public DataBox() {
    }

    public DataBox(List<ResourcePoint> resourcePoints, List<PopPoint> popPoints, List<DemandPoint> demandPoints) {
        this.resourcePoints = resourcePoints;
        this.popPoints = popPoints;
        this.demandPoints = demandPoints;
    }
}