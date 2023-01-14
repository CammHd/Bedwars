package me.camm.productions.bedwars.Arena.Teams;

import org.bukkit.ChatColor;
import org.bukkit.Color;

/**
 * @author CAMM
 * This enum represents the possible colors that a team can take.
 */
public enum TeamColor
{
    RED("Red",14,Color.RED,ChatColor.RED,"R"),
    BLUE("Blue",11,Color.BLUE,ChatColor.BLUE,"B"),
    YELLOW("Yellow",4,Color.YELLOW,ChatColor.YELLOW,"Y"),
    WHITE("White",0,Color.WHITE,ChatColor.WHITE,"W"),
    AQUA("Aqua",9,Color.AQUA,ChatColor.AQUA,"A"),
    GRAY("Gray",7,Color.GRAY,ChatColor.GRAY,"G"),
    PINK("Pink",6,Color.FUCHSIA,ChatColor.LIGHT_PURPLE,"P"),
    GREEN("Green",5,Color.LIME,ChatColor.GREEN,"G" );


    //data value for the color
    private final int value;

    private final String name;
    private final Color color;
    private final ChatColor chatColor;
    private final String symbol;

    TeamColor(String name, int value, Color color, ChatColor chatColor, String symbol)
    {
       this.value = value;
       this.name = name;
       this.color = color;
       this.chatColor = chatColor;
       this.symbol = symbol;

    }

    public int getValue()
    {
        return value;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public String getName()
    {
        return name;
    }

    public Color getColor()
    {
        return color;
    }

    public ChatColor getChatColor()
    {
        return chatColor;
    }

    public String format(){
        return getChatColor()+getName();
    }


}
