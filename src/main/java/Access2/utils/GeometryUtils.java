package Access2.utils;

import org.locationtech.jts.geom.*;

public class GeometryUtils {

    private final static GeometryFactory geometryFactory = new GeometryFactory();

    public static Coordinate getBreakPoint(Coordinate start, Coordinate end, double scale){
        if(scale < 0 || scale > 1){
            throw new IllegalArgumentException();
        }
        return new Coordinate(start.x + scale * ( end.x - start.x), start.y + scale * ( end.y - start.y));
    }

    public static Point getBreakPoint(Point startP, Point endP, double scale){
        if(scale < 0 || scale > 1){
            throw new IllegalArgumentException();
        }
        Coordinate start = startP.getCoordinate();
        Coordinate end = endP.getCoordinate();
        return geometryFactory.createPoint(new Coordinate(
                start.x + scale * ( end.x - start.x), start.y + scale * ( end.y - start.y)));
    }

    public static String multiLineStringToLineString(MultiLineString multiLineString){
        Coordinate[] coordinates = multiLineString.getCoordinates();
        StringBuilder res = new StringBuilder("LINESTRING (");
        for (Coordinate coordinate: coordinates){
            res.append(coordinate.x).append(" ").append(coordinate.y).append(", ");
        }
        res.deleteCharAt(res.length()-2);
        res.append(")");
        return res.toString();
    }
}
