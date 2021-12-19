package Access2.accessModel.dataPoint;

public class ResourcePoint extends DataPoint {
    // 资源点的资源权重，权重越大，资源量越多 分子
    public double resourceWeight;

    // 该资源点服务范围的竞争系数 分母
    public double competeFactor;

    // 搜索阈值 d0
    public double d0;

    // 该资源点的真实可达性 rj
    public double realAttractive;
}
