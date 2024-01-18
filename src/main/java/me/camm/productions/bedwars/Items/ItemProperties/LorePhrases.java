package me.camm.productions.bedwars.Items.ItemProperties;


/**
 * @author CAMM
 * enum for lore that the items in the shops have
 */
public enum LorePhrases
{
    COST("Cost:"),
    SELL("Sell amount:");

    private final String phrase;
    LorePhrases(String phrase)
    {
        this.phrase = phrase;
    }

    public String getPhrase()
    {
        return phrase;
    }
}
