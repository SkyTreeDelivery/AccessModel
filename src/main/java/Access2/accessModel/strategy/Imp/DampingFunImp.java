package Access2.accessModel.strategy.Imp;

import Access2.accessModel.strategy.DampingFun;
import Access2.accessModel.strategy.FunFactory.DampingFunFactory;

import static java.lang.Math.*;

public class DampingFunImp {

    private final static double TWO_PI_SQRT = sqrt(2 * PI);

    final public static DampingFun noDamp = (d, d0) -> d;

    final public static DampingFun expDamp = DampingFunFactory.expDampingFun(1);

    final public static DampingFun expDamp_2 = DampingFunFactory.expDampingFun(2);

    // 高斯衰减
    final public static DampingFun gauss = (d, d0) -> {
        double x = d / d0;
        return (1.0 / TWO_PI_SQRT) * exp(-0.5 * x * x);
    };

    // 抛物线衰减
    final public static DampingFun parabolic = (d, d0) -> {
        double x = d / d0;
        if (Math.abs(x) > 1) {
            throw new IllegalArgumentException("输入的值的绝对值不能大于1");
        }
        return (3.0 / 4.0) * (1 - x * x);
    };

    // 四次方衰减
    final public DampingFun quartic = (d, d0) -> {
        double x = d / d0;
        if (Math.abs(x) > 1) {
            throw new IllegalArgumentException("输入的值的绝对值不能大于1");
        }
        return (15.0 / 16.0) * Math.pow(1 - x * x, 2);
    };

    // 分级，多权重衰减
    final public static DampingFun multiClassAndWeight = (d, d0) -> {
        if (d>d0) {
            throw new IllegalArgumentException("参数距离"+d+"的值需要小于阈值："+d0);
        }
        // 根据 d0 设置
        if (d0 == 40) {
            if (d >= 0 && d <= 15) {
                return 1.0;
            } else if (d <= 25) {
                return 0.82;
            } else if (d <= 40) {
                return 0.54;
            } else return 0;
        } else if (d0 == 20) {
            if (d >= 0 && d <= 10) {
                return 1.0;
            } else if (d <= 15) {
                return 0.70;
            } else if (d <= 20) {
                return 0.38;
            }else return 0;
        } else if(d0==10){
            if(d>=0 &&d<=4){
                return 1.0;
            }else if(d<=8){
                return 0.80;
            }else if(d<=10){
                return 0.30;
            }else return 0;
        } else {
            return parabolic.damping(d, d0);
        }
    };
}
