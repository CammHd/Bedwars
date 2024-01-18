package me.camm.productions.bedwars.Util.Locations;

import me.camm.productions.bedwars.Util.BlockTag;
import me.camm.productions.bedwars.Util.Helpers.BlockTagManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;


/*
Notes for generic classes:

t cannot be instantiated directly. So that means that you will need to throw an illegal
args exception and handle it externally.
 */


public class BridgeFiller
{
    private int x1, x2, y1, y2, z1, z2;
    private int[] bounds;

    private final BlockTagManager manager;

    public BridgeFiller(Location one, Location two)
    {
        this.bounds = new int[] {one.getBlockX(),two.getBlockX(),one.getBlockY(), two.getBlockY(),one.getBlockZ(), two.getBlockZ()};
        reArrange();
        dissectArray();
        manager = BlockTagManager.get();
    }

    private void dissectArray() {
        if (bounds != null && bounds.length == 6) {
            x1 = bounds[0];
            x2 = bounds[1];
            y1 = bounds[2];
            y2 = bounds[3];
            z1 = bounds[4];
            z2 = bounds[5];
        } else {
            this.bounds = new int[]{0, 0, 0, 0, 0, 0};
        }
    }

    private void reArrange(int one, int two, int [] order, int repetition) //method loop for rearranging
    {
        int placeHold;

        final int LENGTH = 6;
        final int TOTAL = 2;  //total amount of repetitions for the method

        if (order.length == LENGTH)  //if the length of the array is 6
        {
            if (order[one] > order[two]) {
                placeHold = order[two];
                order[two] = order[one];
                order[one] = placeHold;
            }
            one += 2;
            two += 2;

            if (repetition < TOTAL) {
                repetition++;
                reArrange(one, two, order, repetition);
            } else
                bounds = order;
        }
        else
            bounds = new int[]{0,0,0,0,0,0};
    }

    private void reArrange()  //invoking the method loop
    {
        final int ONE = 0;
        final int TWO = 1;
        final int REPETITION = 0;
        reArrange(ONE, TWO, bounds, REPETITION);
    }

    @SuppressWarnings("deprecation")
    public void fill(Material filler, byte data, World world)
    {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);


                    if (canFill(block)) {
                        block.setType(filler);  // block.set type
                        block.setData(data);
                    }
                }
            }
        }
    }


    private boolean canFill(Block b)
    {
        if (!manager.isInbounds(b))
            return false;

        if (!manager.hasTag(b))
            return true;

        return (manager.getTag(b) != BlockTag.NONE.getTag()) &&
                (b.getType() == Material.AIR);




        // TODO: FIX METADATA CHECKER
        /*
         if block does not have 0000 0000 and in bounds
         then can place
         */
    }
}
