package me.camm.productions.bedwars.Util.Explosions.Vectors;

import org.bukkit.World;
import org.bukkit.util.Vector;



/**
 * @author CAMM
 * Models a vector for the game
 */
public abstract class GameVector
{
    protected Vector direction;
    protected Vector origin;
    protected World world;


    public Vector getPosition(double blocks)
    {
        return direction.clone().multiply(blocks).add(origin.clone());
    }

}
