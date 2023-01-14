package me.camm.productions.bedwars.Files.FileKeywords;

/**
@author CAMM
  This class holds the significant keys used in file reading for the plugin.
 */
@Deprecated
public enum DataSeparatorKeys
{
    SEPARATOR("/"),
    COMMA(","),
    DECLARATION(":"),
    COMMENT("#");


    private final String key;

    private DataSeparatorKeys(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public char getKeyAsChar()
    {
        return key.charAt(0);
    }
}
