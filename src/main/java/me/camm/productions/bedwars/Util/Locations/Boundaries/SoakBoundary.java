package me.camm.productions.bedwars.Util.Locations.Boundaries;

import me.camm.productions.bedwars.Util.Locations.RegisterType;
import me.camm.productions.bedwars.Util.Locations.WallFace;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class SoakBoundary extends Boundary<Double>
{
    private volatile Double[] bounds;

    public SoakBoundary(Double[] bounds) {
        this.bounds = bounds;
        analyze();
        reArrange();
        dissectArray();
    }

    public SoakBoundary(double x1,double x2,double y1,double y2,double z1,double z2) {
        this.x1 = x1;
        this.x2 = x2;

        this.y1 = y1;
        this.y2 = y2;

        this.z1 = z1;
        this.z2 = z2;

        bounds = new Double[]{x1, x2, y1, y2, z1, z2};
        reArrange();
    }

    @Override
    protected Double[] reset() {
        return new Double[]{0d, 0d, 0d, 0d, 0d, 0d};
    }

    @Override
    protected void analyze()
    {
        if (bounds==null||bounds.length != 6)
            bounds = reset();
    }


    @Override
    protected void dissectArray() {
        if (bounds != null && bounds.length == 6) {
            x1 = bounds[0];
            x2 = bounds[1];
            y1 = bounds[2];
            y2 = bounds[3];
            z1 = bounds[4];
            z2 = bounds[5];
        } else {
            this.bounds = reset();
        }
    }


    private void order(int one, int two, Double[] order, int repetition) //method loop for rearranging
    {
        double placeHold;

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
                order(one, two, order, repetition);
            } else
                bounds = order;
        }
        else
            bounds = reset();
    }

    @Override
    protected void reArrange()  //invoking the method loop
    {
        final int ONE = 0;
        final int TWO = 1;
        final int REPETITION = 0;
        order(ONE, TWO, bounds, REPETITION);
    }


    public void unregister(String metadata, World world, Plugin plugin) {
        for (double x = x1; x <= x2; x++) {
            for (double y = y1; y <= y2; y++) {
                for (double z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt((int)x, (int)y, (int)z);
                    block.removeMetadata(metadata, plugin);
                }
            }
        }
    }

    public void register(World world, String type, int blocks, Plugin plugin)//include stuff to register different blocks [e.g air, wood, wool{include colors}]
    {

        if (blocks == RegisterType.EVERYTHING.getType()) //if 0, then register blocks that are not air
            registerAll(world, type, plugin);  //if 0, then register blocks that are not air
        else if (blocks == RegisterType.NOT_AIR.getType())
            registerSolids(world, type, plugin);
        else
            registerAir(world, type, plugin); //only register the air

    }
    //1 = all, 0 = !air, -1 = air only


    //Expands(Or shrinks) the values by the factor.
    //Magnitude is original value + factor
    //format is x1 x2 y1 y2 z1 z2
    public void expand(int [] expansionFactor)
    {
        if (expansionFactor==null||expansionFactor.length!=6)
            return;

        for (int slot=0;slot< expansionFactor.length;slot++)
        {
            //The values are already sorted, so we know which ones are smaller or larger, so we
            //know to expand in the negative or positive direction.
            if (slot%2==0)
                bounds[slot] += expansionFactor[slot]*-1;
            else
                bounds[slot] += expansionFactor[slot];
        }
        dissectArray();
    }

    public void expand(int expansion)
    {
        expand(new int[] {expansion,expansion,expansion,expansion,expansion,expansion});
    }


    //the wallface specifies the pivot in where redundant values are converted to.
    /*
     NORTH(), //towards neg z
    EAST(),  //towards pos x
    SOUTH(),  //towards pos z
    WEST();  //towards neg x

    Basically, a cube can become a flat surface if values are the same.
    We are applying this concept to generate 6 faces, each for a cube wall
    so that we get a hollow area in the middle instead of a cube
    filled with particles when the sponge is used.
     */
    public PointWall generateWall(WallFace toCollapseUpon)
    {
        if (toCollapseUpon==null)
            return new PointWall(reset());

        Double[] coords = bounds.clone();
        switch (toCollapseUpon)
        {
            //x1 to x2, y1 ... y2, z1... z2

            case UP:
                coords[2] = coords[3];
                break;

            case DOWN:
                coords[3] = coords[2];
                break;

            case NORTH:
                coords[5]  = coords[4];
                break;

            case EAST:
                coords[0] = coords[1];
                break;

            case SOUTH:
                coords[4] = coords[5];
                break;

            case WEST:
                coords[1] = coords[0];
        }
        return new PointWall(coords);
    }



    //registers all blocks in the bounds with a metadata of string type
    private void registerAll(World world, String type, Plugin plugin) {

        for (double x = x1; x <= x2; x++) {
            for (double y = y1; y <= y2; y++) {
                for (double z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt((int)x, (int)y, (int)z);
                    if (!block.hasMetadata(type))
                        block.setMetadata(type, new FixedMetadataValue(plugin, 1));

                }
            }
        }
    }

    //Registering everything that is not air
    private void registerSolids(World world, String type, Plugin plugin) {
        for (double x = x1; x <= x2; x++) {
            for (double y = y1; y <= y2; y++) {
                for (double z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt((int)x,(int) y,(int) z);
                    if (block.getType() != Material.AIR && !block.hasMetadata(type))
                        block.setMetadata(type, new FixedMetadataValue(plugin, 1));
                }
            }
        }
    }


    private void registerAir(World world, String type, Plugin plugin) {
        for (double x = x1; x <= x2; x++) {
            for (double y = y1; y <= y2; y++) {
                for (double z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt((int)x,(int) y,(int) z);
                    if (block.getType() == Material.AIR && !block.hasMetadata(type))
                        block.setMetadata(type, new FixedMetadataValue(plugin, 1));
                }
            }
        }
    }

    public Double[] getValues() {
        reArrange();
        return bounds;
    }
}
