package me.camm.productions.bedwars.Files;

import me.camm.productions.bedwars.Arena.Teams.TeamColor;
import me.camm.productions.bedwars.BedWars;
import me.camm.productions.bedwars.Generators.Forge;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;


import java.util.Map;

public class JsonBuilder {


    public static Coordinate buildCoordinate(Map<String,String> values) throws IllegalArgumentException {
        //(double x, double y, double z, double yaw)

        try {
            String rotation = null;
            double[] coords = getXYZAsDouble(values);


            if (values.containsKey(JsonParser.Identifier.ROTATION.id))
                rotation = values.get(JsonParser.Identifier.ROTATION.id);

            if (rotation == null)
                return new Coordinate(coords[0], coords[1], coords[2]);
            else
                return new Coordinate(coords[0], coords[1], coords[2], toDouble(rotation));
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error building coordinate location: "+e.getMessage());
        }
    }





    //double x, double y, double z, World world, TeamColor color, long initialTime, Plugin plugin, double pickup
    public static Forge buildForge(Map<String, String> values, World world, TeamColor color) {
        double[] coords = getXYZAsDouble(values);
        Plugin plugin = BedWars.getInstance();

        int time = -1;
        if (values.containsKey(TeamFileJsonParser.DataHolder.FORGE_SPEED.data))
            time = toInt(values.get(TeamFileJsonParser.DataHolder.FORGE_SPEED.data));


        double pickup = -1;
        if (values.containsKey(TeamFileJsonParser.DataHolder.FORGE_PICKUP.data))
            pickup = toDouble(values.get(TeamFileJsonParser.DataHolder.FORGE_SPEED.data));



        if (time == -1 || pickup == -1)
            throw new IllegalArgumentException("Forge speed, pickup values are invalid");

        return new Forge(coords[0], coords[1], coords[2],world,color, time, plugin, pickup);



    }


    //(Integer[] bounds)
    //x1 x2, y1 y2, z1 z2
    public static GameBoundary buildBoundary(Map<String, String> values) throws IllegalArgumentException {

        try {
            int[] xyz1 = getXYZInt(values);
            int[] xyz2 = getXYZInt2(values);
            Integer[] coordinates = new Integer[]{xyz1[0], xyz2[0], xyz1[1], xyz2[1], xyz1[2], xyz2[2]};

            return new GameBoundary(coordinates);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error building boundary: "+e.getMessage());
        }
    }




    public static double toDouble(String value) throws IllegalArgumentException {
        try {
            return Double.parseDouble(value);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Tried to parse "+value+" to double");
        }
    }


    public static int toInt(String value) throws IllegalArgumentException {
        try {
            double number = Double.parseDouble(value);
            return (int)number;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Tried to parse "+value+" to double");
        }
    }




    private static int[] getXYZInt(Map<String, String> values) {
        String[] coords = getXYZFirstPair(values);
        return getInts(coords);

    }

    private static int[] getXYZInt2(Map<String, String> values) {
      String[] coords = getXYZSecondPair(values);
        return getInts(coords);

    }


    private static int[] getInts(String[] coords) {
        if (coords == null)
            throw new IllegalArgumentException("Invalid arguments for x,y,z coordinates");

        int xd, yd, zd;
        xd = toInt(coords[0]);
        yd = toInt(coords[1]);
        zd = toInt(coords[2]);

        return new int[]{xd,yd, zd};
    }



    private static double[] getXYZAsDouble(Map<String, String> values) {

        String[] coords = getXYZFirstPair(values);
        if (coords == null)
            throw new IllegalArgumentException("Invalid arguments for x,y,z coordinates");

        double xd, yd, zd;
        xd = toDouble(coords[0]);
        yd = toDouble(coords[1]);
        zd = toDouble(coords[2]);

        return new double[]{xd, yd, zd};
    }



    private static String[] getXYZFirstPair(Map<String, String> values){
        String x,y,z;
        x = y = z = null;

        if (values.containsKey(JsonParser.Identifier.X.id))
            x = values.get(JsonParser.Identifier.X.id);

        if (values.containsKey(JsonParser.Identifier.Y.id))
            y = values.get(JsonParser.Identifier.Y.id);

        if (values.containsKey(JsonParser.Identifier.Z.id))
            z = values.get(JsonParser.Identifier.Z.id);

        if (x == null || y == null || z == null) {
            return null;
        }
        return new String[]{x,y,z};
    }

    private static String[] getXYZSecondPair(Map<String, String> values) {
        String x,y,z;
        x = y = z = null;

        if (values.containsKey(JsonParser.Identifier.X1.id))
            x = values.get(JsonParser.Identifier.X1.id);

        if (values.containsKey(JsonParser.Identifier.Y1.id))
            y = values.get(JsonParser.Identifier.Y1.id);

        if (values.containsKey(JsonParser.Identifier.Z1.id))
            z = values.get(JsonParser.Identifier.Z1.id);

        if (x == null || y == null || z == null) {
            return null;
        }
        return new String[]{x,y,z};
    }
}
