package Access2.accessModel.strategy;

@FunctionalInterface
public interface DampingFun {
    /**
     * 距离衰减函数
     * @param input 距离
     * @return 衰减因子
     */
    double damping(double input);
}
