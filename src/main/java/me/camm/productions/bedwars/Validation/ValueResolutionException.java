package me.camm.productions.bedwars.Validation;

import org.bukkit.ChatColor;


//when we try to equate something to a value but are unable to
public class ValueResolutionException extends ParameterException
{
   private final String result;
    public ValueResolutionException(String file, int line, String expectedResult, String given, String result) {
        super(file, line, expectedResult, given);
        this.result = result;
    }

    public String toString(){
        return getMessage();
    }

    @Override
    public String getMessage() {
        return ChatColor.RED+"Could not resolve value: File:"+file+" Line: "+line+" Expected:"+expected+" Got: "+result;
    }
}
