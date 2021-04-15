package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.DampingFun;
import Access2.accessModel.strategy.FunFactory.DampingFunFactory;

import static java.lang.Math.*;

public class DampingFunImp {

    private final static double TWO_PI_SQRT = sqrt(2 * PI);

    final public static DampingFun noDamp = x -> x;

    final public static DampingFun expDamp = DampingFunFactory.expDampingFun(1);

    final public static DampingFun expDamp_2 = DampingFunFactory.expDampingFun(2);

    final public static DampingFun gauss = (x) -> {
        return (1.0 / TWO_PI_SQRT) * exp(-0.5 * x * x);
    };

    final public static DampingFun parabolic = (x) -> {
        if(Math.abs(x) > 1){
            throw new IllegalArgumentException("输入的值的绝对值不能大于1");
        }
        return (3.0 / 4.0) * (1 -  x * x);
    };

    final public DampingFun quartic = (x) -> {
        if(Math.abs(x) > 1){
            throw new IllegalArgumentException("输入的值的绝对值不能大于1");
        }
        return (15.0 / 16.0) * Math.pow(1 - x * x, 2);
    };
}
