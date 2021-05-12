package Access2.accessModel.dataPoint;

import Access2.accessModel.AccessModel;
import org.locationtech.jts.geom.Geometry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 需求点，保存与可达性模型中需求方有关的数据
 */
public class DemandPoint extends DataPoint {
    // 需求点的原始几何对象
    public Geometry originalGeom;

    // 可达性估值
    public double access = -1;

    // 需求点可及的资源点的相关数据
    public List<AccessModel.DemandPackage> demandPackages = new CopyOnWriteArrayList<>();

    // 需求点的原始类型
    public DemandType type;

    // 枚举类，表示需求点的原始类型
    public enum DemandType{
        NODE,
        EDGE,
        POLYGON,
        GRID_FROM_POP,
        GRID
    }
}
