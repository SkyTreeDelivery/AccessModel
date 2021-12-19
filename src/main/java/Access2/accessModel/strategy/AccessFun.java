package Access2.accessModel.strategy;

@FunctionalInterface
public interface AccessFun {
    /**
     * 可达性函数，根据需求点和资源点之间的成本、资源点的实际吸引力、可达性衰减函数、搜索半径，计算某资源点到某需求点的可达性
     * @param cost 资源点和需求点之间的成本
     * @param realAttractive 资源点的实际吸引力
     * @param dampingFun 可达性衰减函数
     * @param cost0 最大的成本阈值
     * @return 可达性结果值
     */
    double access(double cost, double realAttractive, DampingFun dampingFun, double cost0);
}
