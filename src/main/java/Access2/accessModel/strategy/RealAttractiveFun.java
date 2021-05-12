package Access2.accessModel.strategy;

@FunctionalInterface
public interface RealAttractiveFun {
    /**
     * 资源点的实际吸引力计算
     * 如果不考虑区域竞争效应，则 实际吸引力 = 资源总量
     * 如果考虑区域竞争效应，则 实际吸引力 = 资源总量 / 竞争水平
     * @param resource 资源总量
     * @param completeFactor 竞争水平
     * @return 实际吸引力
     */
    double realAttractive(double resource, double completeFactor);
}
