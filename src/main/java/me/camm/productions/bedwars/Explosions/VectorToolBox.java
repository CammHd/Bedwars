package me.camm.productions.bedwars.Explosions;


import me.camm.productions.bedwars.Util.BlockTag;
import me.camm.productions.bedwars.Util.Helpers.BlockTagManager;
import org.bukkit.Material;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;


import java.util.Random;



/**
@author CAMM
Helper for vectors
 */
public class VectorToolBox
{

    private static final Random rand;

    static {
        rand = new Random();
    }


    /*
    @Author CAMM
    Returns boolean whether a block meets certain parameters, depending on the registered teams, the block
    check type, and the block data.
    If the block meets the params, then it can be broken safely in the game.

    SEE: checkWoodData() and checkColoredData()

    isDistinctlyColorable refers to colors.
    if is wood, then false, else, if clay or wool, true
     */
    public static boolean isDataDestructable(byte data, boolean isDistinctlyColorable, Block block, int[] colors)
    {
        return isDistinctlyColorable ? checkColoredData(data, block, colors):checkWoodData(data, block);
    }

    /*
    Returns whether an entity should be given velocity from an explosion
     */
    public static boolean isValidVelocityType(Entity entity) {
        boolean valid = true;
        EntityType type = entity.getType();

        switch (type)
        {
            case FIREBALL:
            case ARMOR_STAND:
            case ENDER_DRAGON:
            case DROPPED_ITEM:
            case ITEM_FRAME:
            case EXPERIENCE_ORB:
                valid = false;
        }
        return valid;
    }

    /*
    returns whether an entity can be damaged
     */
    public static boolean isValidDamageType(Entity entity)
    {
        boolean valid = true;
        EntityType type = entity.getType();

        switch (type)
        {
            case ARMOR_STAND:
            case DROPPED_ITEM:
            case FIREBALL:
                valid = false;


        }
        return valid;
    }






    /*
    @Author CAMM
    This method returns whether the block has metadata, and if the block type is oak wood (data==0)
    If returns true, the block can be broken. Else, it cannot.
     */
    private static boolean checkWoodData(byte data, Block block)
    {
        return data==0 && BlockTagManager.get().isInbounds(block);
    }



    /*
    @Author CAMM
    Checks for whether a colored block was placed.
    If the block color is the same as a team, and the block doesn't have metadata, it can be broken.
     */
    private static boolean checkColoredData(byte data, Block block, int[] teamColors)
    {
        if (teamColors==null||teamColors.length==0)
            return false;

        boolean canBreak  = false;
        for (int current: teamColors)
        {
            if (data==current)
            {
                canBreak = true;
                break;
            }
        }

        BlockTagManager manager = BlockTagManager.get();

        //no tag -> map block
        if (!manager.hasTag(block))
            return false;

        if (!manager.isInbounds(block))
            return false;

       byte tag = manager.getTag(block.getX(), block.getY(), block.getZ());

        //ensure it's not a bed, chest, generator
       if (tag != BlockTag.ALL.hashCode())
           return false;

        return canBreak;

    }




    /*
    @Author CAMM
   Checks the resistance a block would put up in an explosion.
   this is a formula from the minecraft wiki.
     */
    public static double calculateResistance(double blockStrength)
    {
        return (blockStrength+VectorParameter.BLOCK_ADDITION.getValue())/
                VectorParameter.BLOCK_DIVISOR.getValue();
    }



    /*
    @Author CAMM
    Rolls a random chance with a maximum of the ceiling vector parameter.
    If the chance is greater than the drop chance, then the block breaks naturally, and drops an item.
    Else, it is just set to air.
     */
    public static void breakAtPosition(Block block)
    {
        int chance = rand.nextInt((int)VectorParameter.CEILING.getValue());
        if (chance>VectorParameter.DROP_CHANCE.getValue())
            block.breakNaturally();
        else
            block.setType(Material.AIR);
    }
}
