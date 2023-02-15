package me.camm.productions.bedwars.Arena.Teams;

import me.camm.productions.bedwars.Util.BlockTag;
import org.bukkit.ChatColor;
import org.bukkit.Color;

/**
 * @author CAMM
 * This enum represents the possible colors that a team can take.
 */
public enum TeamColor
{
    RED("Red",14,Color.RED,ChatColor.RED,"R", BlockTag.RED),
    BLUE("Blue",11,Color.BLUE,ChatColor.BLUE,"B",BlockTag.BLUE),
    YELLOW("Yellow",4,Color.YELLOW,ChatColor.YELLOW,"Y",BlockTag.YELLOW),
    WHITE("White",0,Color.WHITE,ChatColor.WHITE,"W",BlockTag.WHITE),
    AQUA("Aqua",9,Color.AQUA,ChatColor.AQUA,"A",BlockTag.AQUA),
    GRAY("Gray",7,Color.GRAY,ChatColor.GRAY,"G",BlockTag.GRAY),
    PINK("Pink",6,Color.FUCHSIA,ChatColor.LIGHT_PURPLE,"P",BlockTag.PINK),
    GREEN("Green",5,Color.LIME,ChatColor.GREEN,"G",BlockTag.GREEN);


    //data value for the color
    private final int value;

    private final BlockTag tag;

    private final String name;
    private final Color color;
    private final ChatColor chatColor;
    private final String symbol;

    TeamColor(String name, int value, Color color, ChatColor chatColor, String symbol, BlockTag tag)
    {
       this.value = value;
       this.name = name;
       this.color = color;
       this.chatColor = chatColor;
       this.symbol = symbol;
       this.tag = tag;

    }


    public int getValue()
    {
        return value;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public BlockTag getTag()
    {
        return tag;
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
