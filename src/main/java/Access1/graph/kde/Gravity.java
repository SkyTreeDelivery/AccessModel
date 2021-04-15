package Access1.graph.kde;

import Access1.graph.ShortestPath.MathFunc;

import java.util.Arrays;

public class Gravity {

    public static double gravity_weight(double[] disArray, double dampingFactor, Poi[] poiArray) {
        double result = 0;
        for (int i = 0; i < disArray.length; i++) {
            result += poiArray[i].weight * Math.exp(-dampingFactor * disArray[i]);
        }
        return result;
    }

    public static double gravity_pop_resource_weight(double[] disArray, double dampingFactor, Poi[] poiArray) {
        double[] normalize = MathFunc.normalize(Arrays.stream(poiArray).mapToDouble(poi -> poi.popCover).toArray());
        double result = 0;
        for (int i = 0; i < disArray.length; i++) {
            result += poiArray[i].weight * Math.exp(-dampingFactor * disArray[i]) / normalize[i];
        }
        return result;
    }
}
