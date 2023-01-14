package me.camm.productions.bedwars.Arena.Game.Events;

import org.bukkit.ChatColor;

/**
 * @author CAMM
 * This enum is for the chat text that comes up during certain events
 */
public enum GameEventText
{
    BED_DESTROY_SCHEDULED_WARNING(ChatColor.RED+"All beds will be destroyed in 5 minutes!"),
    SPAWN_DRAGONS(ChatColor.RED+"Sudden Death"),
    DESTROY_BEDS(ChatColor.RED+" All beds have been destroyed!"),
    DRAGON_SPAWN_SCHEDULED_WARNING(ChatColor.RED+"Sudden Death in 5 minutes!"),
    GAME_END_SCHEDULED_WARNING(ChatColor.RED+"The game ends in 5 minutes!"),
    GAME_END_TEXT(ChatColor.YELLOW +"The game has ended!"),
    UPGRADE_DIAMONDS(ChatColor.AQUA+"Diamond"+ChatColor.YELLOW+" generators have been upgraded to tier "+ChatColor.RED),
    UPGRADE_EMERALDS(ChatColor.GREEN+"Emerald"+ ChatColor.YELLOW+" generators have been upgraded to tier "+ChatColor.RED);

    private final String text;

    GameEventText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
