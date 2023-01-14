package me.camm.productions.bedwars.Util.Helpers;

import me.camm.productions.bedwars.BedWars;
import me.camm.productions.bedwars.Files.FileKeywords.DataSeparatorKeys;
import me.camm.productions.bedwars.Files.FileKeywords.FilePaths;
import org.bukkit.entity.Player;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;


public class StringHelper
{

   // private final Plugin plugin;
    private static final String deliminator= "\\";

    //gets the string with comments (#abc) accounted for
    //returns null if the entire string is a comment
    public static String checkForComments(String original)
    {
        if (original == null)
            return null;

        int index = original.indexOf(DataSeparatorKeys.COMMENT.getKey());
        if (index==-1)
            return original;
        original = (original.substring(0,index)).trim();
        if (original.length()==0)
            return null;
        return original;
    }


    //returns the section of a string after the ":" keyword, trimmed.
    //returns null if: ":" dne or the number of ":" > 1
    public static String getInfoSection(String original)
    {
        if (original == null)
            return null;


        char comparator = DataSeparatorKeys.DECLARATION.getKey().charAt(0);
        int occurrences = 0;
        for (int position=0;position<original.length();position++)
        {
            if (original.charAt(position)==comparator)
                occurrences++;
        }
        if (occurrences!=1)
            return null;

        int index = original.indexOf(comparator);
        return (original.substring(index+1)).trim();
    }






    //takes a string, returns a numerical array depending on the context
    //if runs into error --> returns null
    public static @Nullable double[] getNumbers(String original)
    {
        original = getInfoSection(original);
        if (original==null) {
            return null;
        }
        ArrayList<Double> numbers = new ArrayList<>();
        StringTokenizer separatorCutter = new StringTokenizer(original, DataSeparatorKeys.SEPARATOR.getKey());

        while (separatorCutter.hasMoreTokens())
        {
            String value = separatorCutter.nextToken();
            StringTokenizer commaCutter = new StringTokenizer(value, DataSeparatorKeys.COMMA.getKey());
            while (commaCutter.hasMoreTokens())
                numbers.add(toNumber(commaCutter.nextToken()));
        }

        double[] processed = new double[numbers.size()];
        for (int slot=0;slot<numbers.size();slot++) {
            processed[slot] = numbers.get(slot);
        }
        return processed;

    }


    //takes a double array and converts it into an int array
    public static Integer[] doubleToIntArray(double[] values)
    {

        if (values == null)
            throw new NullPointerException("values is null!");



        Integer[] processed = new Integer[values.length];
        for (int slot=0;slot<values.length;slot++) {
            processed[slot] = (int) values[slot];
        }
        return processed;
    }

    //changes a string to a double. Returns 0 if fails
    public static double toNumber(String format)
    {
        try
        {
            return Double.parseDouble(format);
        }
        catch (NumberFormatException e)
        {
            printParseError(format);
            return 0;
        }
    }

    //gets the plugin folder of the server.
    public static String getServerFolder()
    {
        return BedWars.getInstance().getDataFolder().getParentFile().getAbsolutePath();
    }

    public static String getMainFolderPath()
    {
        return getServerFolder()+deliminator+ FilePaths.MAIN.getValue();
    }
    //gets the path of the world txt file
    public static String getWorldPath()
    {
       return getMainFolderPath()+deliminator+ FilePaths.WORLD.getValue();
    }

    public static String getTeamPath()
    {
        return getMainFolderPath()+deliminator+ FilePaths.TEAMS.getValue();
    }

    public static String getCreditsPath()
    {
        return getMainFolderPath()+deliminator+ FilePaths.CREDITS.getValue();
    }

    public static String getInstructionsPath()
    {
        return getMainFolderPath()+deliminator+ FilePaths.INSTRUCTIONS.getValue();
    }

    public static String getPlayerFolderPath()
    {
        return getMainFolderPath()+deliminator+ FilePaths.PLAYER.getValue();
    }

    public static String getHotBarPath(Player player)
    {
        return getPlayerPath(player)+deliminator+ FilePaths.HOTBAR.getValue();
    }

    //gets the specified player folder
    public static String getPlayerPath(Player player)
    {
        String uuid = player.getUniqueId().toString();
        return getPlayerFolderPath()+deliminator+uuid;
    }

    public static String getInventoryPath(Player player)
    {
        return getPlayerPath(player)+deliminator+ FilePaths.INVENTORY.getValue();
    }





    public static void printParseError(String value)
    {
       ChatSender sender = ChatSender.getInstance();
       sender.sendConsoleMessage(value, Level.WARNING);

    }

}
