package Access2;

import Access2.graph.Node;

public class MathFunc {

    public static double meter2Minute(double meter, double walkSpeed){
        return (meter / 1000) / walkSpeed  * 60;
    }

    public static boolean doubleIsLegal(double num){
        return num != Double.MAX_VALUE && num != Double.MIN_VALUE && !Double.isNaN(num);
    }

    public static double manhatten(Node v1, Node v2) {
        return Math.abs(v1.point.getX() - v2.point.getY()) + Math.abs(v1.point.getX() - v2.point.getY());
    }

    public static double[] normalize(double[] rowArray){
        double[] result = new double[rowArray.length];
        double min = min(rowArray);
        double max = max(rowArray);
        double gap = max - min;
        for (int i = 0; i < rowArray.length; i++) {
            result[i] = (rowArray[i] - min ) / gap + 0.0001;
        }
        return result;
    }

    public static double min(double[] rowArray){
        if(rowArray.length <= 0){
            throw new IllegalArgumentException();
        }
        double min = rowArray[0];
        for (int i = 1; i < rowArray.length; i++) {
            if(min > rowArray[i]){
                min = rowArray[i];
            }
        }
        return min;
    }

    public static double max(double[] rowArray){
        if(rowArray.length <= 0){
            throw new IllegalArgumentException();
        }
        double max = rowArray[0];
        for (int i = 1; i < rowArray.length; i++) {
            if(max < rowArray[i]){
                max = rowArray[i];
            }
        }
        return max;
    }
}