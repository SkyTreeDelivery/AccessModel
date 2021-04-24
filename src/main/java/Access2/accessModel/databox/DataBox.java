package Access2.accessModel.databox;

import Access2.accessModel.dataPoint.DemandPoint;
import Access2.accessModel.dataPoint.PopPoint;
import Access2.accessModel.dataPoint.ResourcePoint;

import java.util.List;

public class DataBox {
    public List<ResourcePoint> resourcePoints;
    public List<PopPoint> popPoints;
    public List<DemandPoint> demandPoints;

    public DataBox() {
    }

    public DataBox(List<ResourcePoint> resourcePoints, List<PopPoint> popPoints, List<DemandPoint> demandPoints) {
        this.resourcePoints = resourcePoints;
        this.popPoints = popPoints;
        this.demandPoints = demandPoints;
    }
}