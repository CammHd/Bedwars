package me.camm.productions.bedwars.Arena.Players.Scoreboards;

import org.bukkit.ChatColor;

/**
 * @author CAMM
 * Enum for the headers in the scoreboard for the players
 */
public enum ScoreBoardHeader
{
    DIAMOND_TWO_HEADER("Diamond II in "+ ChatColor.GREEN),
    DIAMOND_THREE_HEADER("Diamond III in "+ChatColor.GREEN),
    EMERALD_TWO_HEADER("Emerald II in "+ChatColor.GREEN),
    EMERALD_THREE_HEADER("Emerald III in "+ChatColor.GREEN),
    BED_GONE_HEADER("Bed Gone in "+ChatColor.GREEN),
    SUDDEN_DEATH_HEADER("S.Death in "+ChatColor.GREEN),
    GAME_END_HEADER("Game end in "+ChatColor.RED),

    //blank lines in the scoreboard
    B_ONE(ChatColor.RED.toString()),
    B_TWO(ChatColor.YELLOW.toString()),
    B_THREE(ChatColor.GREEN.toString()),

    TITLE(ChatColor.YELLOW+""+ChatColor.BOLD+"BedWars"),
    TEAM_ALIVE(ChatColor.BOLD+"\u2714"),
    TEAM_DEAD(ChatColor.RED+""+ChatColor.BOLD+"\u2718"),
    KILLS("Kills: "+ChatColor.GREEN),
    FINALS("Finals: "+ChatColor.GREEN),
    BEDS("Beds Broken: "+ChatColor.GREEN),
    CURRENT_TEAM(ChatColor.GRAY+"You"),
    HEART(" "+ChatColor.RED+"\u2764"+ChatColor.RESET),

    SPACE_CREDIT(ChatColor.YELLOW+"credit"),

    DUMMY("dummy"),
    HEALTH_CATEGORY("Health"),
    HEALTH_CATEGORY_TWO("Health 2"),
    HEALTH_CRITERIA("health"),
    PRIMARY_OBJECTIVE("Primary"),
    BUFFER_OBJECTIVE("Buffer"),
    INITIALIZER_ONE("INTIALIZER_ONE"),
    INITIALIZER_TWO("INITIALIZER_TWO"),

    //QUICK_BUY("QuickBuy"),  //Use the inventory database titles instead

    TIME("Time");





    private final String phrase;

    ScoreBoardHeader(String phrase)
    {
        this.phrase = phrase;
    }

    public String getPhrase()
    {
        return phrase;
    }

    @Override
    public String toString() {
        return getPhrase();
    }

}
