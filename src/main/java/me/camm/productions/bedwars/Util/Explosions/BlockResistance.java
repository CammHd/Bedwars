package me.camm.productions.bedwars.Util.Explosions;


/**
 *
 * @author CAMM
 * Enum for block resistance types
 */
public enum BlockResistance
{

    END_STONE(9),
    CLAY(4.2),
    WOOL(0.8),
    WOOD(3),
    NON_SOLID(0),
    LADDER(0.4),
    UNBREAKABLE(Integer.MAX_VALUE);


    private final double resistance;

    BlockResistance(double resistance)
    {
        this.resistance = resistance;
    }

    public double getResistance()
    {
        return resistance;
    }


}
