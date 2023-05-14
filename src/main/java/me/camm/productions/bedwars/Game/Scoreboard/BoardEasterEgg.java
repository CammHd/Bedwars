package me.camm.productions.bedwars.Game.Scoreboard;

import org.bukkit.ChatColor;

/**
 * @author CAMM
 * List of easter eggs that can be put onto the scoreboard
 * length > 32 characters  (pretty sure) will
 * crash the client.
 */
public enum BoardEasterEgg
{
    MARCARONI(ChatColor.YELLOW+"Big Man Marc"),
    PASTA(ChatColor.YELLOW+"Macaroni"),
    CAMM(ChatColor.GOLD+"Chippy"),
    TEAVEE(ChatColor.AQUA+"MrTeavee"),
    PRINTER(ChatColor.GREEN+"Printer screen"),

    TELLYBOI(ChatColor.YELLOW+"Tellyboi"),
    MC_DONALDS(ChatColor.YELLOW+"McDeezNuts"),
    NOT_HYPIXEL(ChatColor.YELLOW+"not hypixel.net"),
    LACHI_MOLALA(ChatColor.YELLOW+"Lachi_Molala"),
    KIWI(ChatColor.GREEN+"KiwiGod888"),
    BUSTER(ChatColor.YELLOW+"Buster"),
    CATS(ChatColor.RED+"Cats!"),

    POTATOES(ChatColor.DARK_PURPLE+"Potatoes"),
    LERCERPE(ChatColor.YELLOW+"Lercerpe"),
    DARKNESS(ChatColor.YELLOW+"GR3aterG0ld"),

    COOL_KIDS(ChatColor.BLACK+"COOLKIDSONLY"),
    QUACK(ChatColor.YELLOW+"Quack");


    private final String phrase;

    BoardEasterEgg(String phrase) {
        this.phrase = phrase;
    }

    public String getPhrase() {
        return phrase;
    }
}
