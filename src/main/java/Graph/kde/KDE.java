package Graph.kde;
import Graph.ShortestPath.MathFunc;

import java.util.Arrays;

import static java.lang.Math.*;

public class KDE {

    public static double kde(double[] disArray, double bandWith, Kernal kernal){
        // 默认的带宽
        if(bandWith <= 0) bandWith = defaultBandwith(disArray);
        double kdeSum = 0.0;
        for (int i = 0; i < disArray.length; i++) {
            // 为不同的核函数提供不同的调用
            switch (kernal){
                case GUASS:
                    kdeSum += KernalFun.guass((disArray[i]) / bandWith);
            }
        }
        return kdeSum / (disArray.length * bandWith);
    }

    /**
     *  考虑了人口与资源权重的可达性计算，参数中的pop数组在传入前应进行归一化处理
     * @param disArray
     * @param bandWith
     * @param kernal
     * @param pop
     * @param resource
     * @return
     */
    public static double kde_pop_resource_weight(double[] disArray, double bandWith, Kernal kernal, double[] pop, double[] resource){
        // 默认的带宽
        if(bandWith <= 0) bandWith = defaultBandwith(disArray);
        double kdeSum = 0.0;
        for (int i = 0; i < disArray.length; i++) {
            // 为不同的核函数提供不同的调用
            switch (kernal){
                case GUASS:
                    kdeSum += KernalFun.guass((disArray[i]) / bandWith) * resource[i] / pop[i];
            }
        }
        return kdeSum / (disArray.length * bandWith);
    }

    public static double kde_pop_resource_weight(double[] disArray, double bandWith, Kernal kernal, Poi[] poiArray){
        double[] normalize = MathFunc.normalize(Arrays.stream(poiArray).mapToDouble(poi -> poi.popCover).toArray());
        // 默认的带宽
        if(bandWith <= 0) bandWith = defaultBandwith(disArray);
        double kdeSum = 0.0;
        for (int i = 0; i < disArray.length; i++) {
            // 为不同的核函数提供不同的调用
            switch (kernal){
                case GUASS:
                    kdeSum += KernalFun.guass((disArray[i]) / bandWith) * poiArray[i].weight / normalize[i];
            }
        }
        double result = kdeSum / (disArray.length * bandWith);
        // TODO 如果结果为正无穷或者负无穷，则改为默认值0.0
        if(result == 1.0/0.0 || result == -1.0/0.0){
            result = 0.0;
        }
        return result;
    }

    // 核密度估计的对外接口
    public static double kde(double x, double[] xArray, double bandWith, Kernal kernal){
        // 默认的带宽
        if(bandWith <= 0) bandWith = defaultBandwith(xArray);
        double kdeSum = 0.0;
        for (int i = 0; i < xArray.length; i++) {
            // 为不同的核函数提供不同的调用
            switch (kernal){
                case GUASS:
                    kdeSum += KernalFun.guass((x - xArray[i]) / bandWith);
            }
        }
        return kdeSum / (xArray.length * bandWith);
    }

    public static double kde(double dis, double bandWith, Kernal kernal){
        // 默认的带宽
        if(bandWith <= 0) bandWith = 1000;
        double kdeSum = 0.0;
        switch (kernal){
            case GUASS:
                kdeSum = KernalFun.guass((dis / 1000) / bandWith);
        }
        return kdeSum / bandWith;
    }

    private static double defaultBandwith(double[] xArray){
        return 1.05 * Sample_STD_dev(xArray) * pow(xArray.length,-0.2);
    }

    // 核函数实现
    public static class KernalFun{

        private final static double TWO_PI_SQRT = sqrt(2 * PI);


        public static double guass(double x){
            return (1.0 / TWO_PI_SQRT) * exp(-0.5 * x * x);
        }
    }

    // 核函数枚举类
    public enum Kernal {
        GUASS
    }


    // sample standard deviation 样本标准差
    static double Sample_STD_dev(double[] data) {
        double std_dev;
        std_dev = Math.sqrt(Sample_Variance(data));
        return std_dev;
    }

    //sample variance 样本方差
    static double Sample_Variance(double[] data) {
        double variance = 0;
        for (int i = 0; i < data.length; i++) {
            variance = variance + (Math.pow((data[i] - Mean(data)), 2));
        }
        variance = variance / (data.length-1);
        return variance;
    }


    static double Mean(double[] data) {
        double mean;
        mean = Sum(data) / data.length;
        return mean;
    }

    static double Sum(double[] data) {
        double sum = 0;
        for (int i = 0; i < data.length; i++)
            sum = sum + data[i];
        return sum;
    }
}
