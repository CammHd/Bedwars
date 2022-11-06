package me.camm.productions.bedwars.Validation;


import org.bukkit.ChatColor;


public class ParameterException extends ConfigException
{
    protected final String file;
    protected final int line;
    protected final String expected;
    protected final String given;

    public ParameterException(String file, int line, String expected, String given) {
        super(null);

        this.file = file;
        this.line = line;
        this.expected = expected;
        this.given = given;
    }

    @Override
    public String toString(){
        return getMessage();
    }

    public String getMessage(){
        return ChatColor.RED+"Encountered unexpected value in: "+file+", line: "+line+", expected: "+expected+", Got: "+given;
    }
}

