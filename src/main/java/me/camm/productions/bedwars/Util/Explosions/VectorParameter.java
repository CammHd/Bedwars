package me.camm.productions.bedwars.Util.Explosions;


/**
 *
 * @author CAMM
 * Enumeration for parameters that a vector can use
 */
public enum VectorParameter
{

    DROP_CHANCE(80),
    MAX_MAGNITUDE(2),
    BLOCK_ADDITION(0.3),

    CEILING(101),// the ceiling value for a random number from n to 100
    FIREBALL_RANGE(6),
    TNT_RANGE(8),

    BLOCK_DIVISOR(3);

    private final double value;

    VectorParameter(double value)
    {
        this.value = value;
    }

    public double getValue()
    {
        return value;
    }
}
