package Access2.accessModel.strategy;

@FunctionalInterface
public interface RegionalCompleteFun {
    double completeFactor(double pop, double distance, DampingFun dampingFun, double dis0);
}
