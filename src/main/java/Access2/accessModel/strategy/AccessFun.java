package Access2.accessModel.strategy;

@FunctionalInterface
public interface AccessFun {
    double access(double dis, double realAttractive, DampingFun dampingFun, double dis0);
}
