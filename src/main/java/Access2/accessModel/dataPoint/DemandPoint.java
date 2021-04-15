package Access2.accessModel.dataPoint;

import Access2.accessModel.AccessModel;
import org.locationtech.jts.geom.Geometry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DemandPoint extends DataPoint {
    public Geometry originalGeom;
    public double access;
    public List<AccessModel.DemandPackage> demandPackages = new CopyOnWriteArrayList<>();
    public DemandType type;

    public enum DemandType{
        NODE,
        EDGE,
        POLYGON,
        GRID_FROM_POP,
        GRID
    }
}
