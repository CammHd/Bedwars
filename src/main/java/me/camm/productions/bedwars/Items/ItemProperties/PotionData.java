package me.camm.productions.bedwars.Items.ItemProperties;


/**
 * @author CAMM
 * Data for potions
 */
public enum PotionData
{
    SPEED_DURATION(900),
    JUMP_DURATION(900),
    INVIS_DURATION(600),

    SPEED_LEVEL(1),
    JUMP_LEVEL(4),
    INVIS_LEVEL(0);

    private final int value;

    PotionData(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
