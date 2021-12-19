package Access2.accessModel.strategy;

@FunctionalInterface
public interface DampingFun {
    /**
     * 距离衰减函数
     * @param d 距离
     * @param d0 阈值 1.0 表示不考虑阈值
     * @return 衰减因子
     */
    double damping(double d, double d0);
}
