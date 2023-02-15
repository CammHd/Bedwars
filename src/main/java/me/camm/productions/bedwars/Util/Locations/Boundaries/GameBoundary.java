package me.camm.productions.bedwars.Util.Locations.Boundaries;


import me.camm.productions.bedwars.Util.Helpers.BlockTagManager;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import me.camm.productions.bedwars.Util.Locations.RegisterType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class GameBoundary extends Boundary<Integer>
{
    private Integer[] bounds;
    private final Random rand;
    private static ChatSender sender;
    private final BlockTagManager blockTagManager;
    public static ConcurrentMap<Long, Byte> blocks = new ConcurrentHashMap<>(1000);

    public GameBoundary(Integer[] bounds) {
        this.bounds = bounds;
        rand = new Random();
        analyze();
        reArrange();
        dissectArray();
        sender = ChatSender.getInstance();
       blockTagManager = BlockTagManager.get();
    }


    @Override
    protected void analyze()
    {
            if (bounds==null||bounds.length != 6)
                bounds = reset();
    }


    @Override
    protected void dissectArray() {
        if (bounds == null || bounds.length != 6) {
            this.bounds = reset();
        } else {
            x1 = bounds[0];
            x2 = bounds[1];
            y1 = bounds[2];
            y2 = bounds[3];
            z1 = bounds[4];
            z2 = bounds[5];
        }
    }

    @Override
    protected Integer[] reset() {
        return new Integer[]{0,0,0,0,0,0};
    }

    @Override
    protected void reArrange()  //invoking the method loop
    {
        final int ONE = 0;
        final int TWO = 1;
        final int REPETITION = 0;
        reArrange(ONE, TWO, bounds, REPETITION);
    }

    private void reArrange(int one, int two, Integer[] order, int repetition) //method loop for rearranging
    {
        int placeHold;

        final int LENGTH = 6;
        final int TOTAL = 2;  //total amount of repetitions for the method

        if (order.length == LENGTH)  //if the length of the array is 6
        {
            if (!(order[one] <= order[two])) {
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
            bounds = reset();
    }



    @SuppressWarnings("deprecation")
    public void replace(Material replacement, Material toReplace, byte[] toReplaceData, World world)
    {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    CURRENT_BLOCK:
                    {
                        if (block.getType() != toReplace)
                        {
                            continue;
                        }

                            for (byte data : toReplaceData)
                            {
                                if (data != block.getData())
                                    continue;

                                block.setType(replacement);

                                //Breaking out of the for loop: for (byte data: ...)
                                break CURRENT_BLOCK;
                                //Break from the current targeted block in the registration process.

                            }
                    }
                }
            }
        }
    }

    public boolean doesBoxContainBlock(Material mat, World world)
    {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                  if (block.getType()==mat)
                      return true;
                }
            }
        }
        return false;
    }

    public void unregister(World world) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (blockTagManager.hasTag(block)) {
                        blockTagManager.removeTag(x,y,z);
                    }
                }
            }
        }
    }

    //todo Fix Memory Leak

    public void register(World world, byte tag, RegisterType type) {
        if(type == RegisterType.EVERYTHING)
        {
            registerAll(tag);
        } else if (type == RegisterType.AIR_ONLY) {
            registerAir(world, tag);
        } else
        {
            registerNotAir(world, tag);
        }
    }

    public void registerWithOverrides(byte tag) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    blockTagManager.overrideTag(x,y,z, tag);
                }
            }
        }
    }



    public void registerAll(byte tag){
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    blockTagManager.addBlock(x,y,z,tag);
                }
            }
        }
    }

    public void registerAir(World world, byte tag){
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR)
                        blockTagManager.addBlock(x,y,z,tag);
                }
            }
        }
    }

    public void registerNotAir(World world, byte tag){
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR)
                        blockTagManager.addBlock(x,y,z,tag);
                }
            }
        }
    }


    //1 = all, 0 = !air, -1 = air only

    public Coordinate getRandomCoordinateWithin() {
        double x = (rand.nextDouble() * (x2-x1) ) + x1;
        double y = (rand.nextDouble() * (y2-y1) ) + y1;
        double z = (rand.nextDouble() * (z2-z1) ) + z1;
        return new Coordinate(x,y,z);
    }

    public Coordinate getCoordinateAverage(){
        double x = (x2+x1) /2.0;
        double y = (y2+y1) /2.0;
        double z = (z2+z1) /2.0;
        return new Coordinate(x,y,z);
    }



    //registers all blocks in the bounds with a metadata of string type


    //registering all materials except for that provided, and air


    public void unregister(World world, String type, Plugin plugin) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.removeMetadata(type, plugin);
                }
            }
        }
        sendUnregistry(type);

    }

    //Checking if the boundary contains a coordinate
    public boolean containsCoordinate(int x, int y, int z)
    {
        return (x1<=x && x<=x2) && (y1<=y && y<=y2) && (z1<=z && z<=z2);
    }

    public boolean containsCoordinate(double x, double y, double z)
    {
        return (x1<=x && x<=x2) && (y1<=y && y<=y2) && (z1<=z && z<=z2);
    }

    public boolean containsEntity(Entity entity)
    {
        Location loc = entity.getLocation();
        return (x1<= loc.getX() && loc.getX()<=x2) && (y1<= loc.getY() && loc.getY() <=y2) && (z1<= loc.getZ() && loc.getZ() <=z2);
    }

    private void sendInfo(String type)
    {
        sender.sendMessage(ChatColor.YELLOW + "[MAP REGISTER] Registered Zone from (x1=" + x1 + ",y1=" + y1 + ",z1=" + z1 + ") to (x2=" + x2 + ",y2=" + y2 + ",z2=" + z2 + ") with " + type);
    }


    private void sendUnregistry(String type) {
        sender.sendMessage(ChatColor.YELLOW + "[MAP REGISTER] Unregistered Zone from (x1=" + x1 + ",y1=" + y1 + ",z1=" + z1 + ") to (x2=" + x2 + ",y2=" + y2 + ",z2=" + z2 + ") from " + type);
    }

    public Integer[] getValues() {
        return bounds;
    }

    public Location getCenter(World world)
    {
        return new Location(world,(x2+x1)/2.0, (y2+y1)/2.0, (z2+z1)/2.0);
    }

    public Collection<Entity> getCloseEntities(World world)
    {
        Location center = getCenter(world);
        return world.getNearbyEntities(center, (x2-x1)/2.0, (y2-y1)/2.0,(z2-z1)/2.0);
    }
}
