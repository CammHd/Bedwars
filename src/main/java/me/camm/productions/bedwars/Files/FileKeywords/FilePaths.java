package me.camm.productions.bedwars.Files.FileKeywords;

/**
 * @author CAMM
 * This enum holds the paths to the files required for the game
 */
@Deprecated
public enum FilePaths
{
    PLAYER("Players"),
    WORLD("World Data.txt"),
    TEAMS("Teams.txt"),
    CREDITS("Credits.txt"),
    INVENTORY("Inventory.txt"),
    HOTBAR("HotBar.txt"),
    INSTRUCTIONS("Instructions.txt"),
    MAIN("BedWars Data"),


    BACKSLASH("\\"),
    FORWARD_SLASH("/");

    private final String value;

    FilePaths(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }



}
