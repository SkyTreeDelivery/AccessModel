package Access2.accessModel.strategy;

@FunctionalInterface
public interface RegionalCompeteFun {
    /**
     * 区域竞争水平估计
     * @param pop 某个人口点的人口数
     * @param cost 资源点与该人口点的成本
     * @param dampingFun 衰减函数
     * @param cost0 最大成本阈值
     * @return 竞争水平
     */
    double completeFactor(double pop, double cost, DampingFun dampingFun, double cost0);
}
