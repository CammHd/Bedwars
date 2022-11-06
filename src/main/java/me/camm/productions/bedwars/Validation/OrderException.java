package me.camm.productions.bedwars.Validation;

import org.bukkit.ChatColor;

public class OrderException extends ConfigException
{
    private final int expectedOrder, position;
    private final String value;

    public OrderException(int expectedOrder, int position, String value) {
        super(null);
        this.expectedOrder = expectedOrder;
        this.position = position;
        this.value = value;
    }

    public String toString(){
        return getMessage();
    }

    @Override
    public String getMessage() {
        return ChatColor.RED+"Expected: "+value+" to be in position "+expectedOrder+" but was found out of order as the "+position+"th argument";
    }
}
