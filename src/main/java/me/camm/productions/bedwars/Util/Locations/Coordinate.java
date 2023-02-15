package me.camm.productions.bedwars.Util.Locations;

import me.camm.productions.bedwars.Util.BlockTag;
import me.camm.productions.bedwars.Util.Helpers.BlockTagManager;
import me.camm.productions.bedwars.Util.Locations.Boundaries.SoakBoundary;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class Coordinate implements IRegistratable
{
    private double x,y,z, yaw;

    public Coordinate(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
    }

    public Coordinate(double x, double y, double z, double yaw)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    public Coordinate(Location loc)
    {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        yaw = loc.getYaw();
    }

    public Coordinate(double[] values)
    {
        if (values==null) {
            defaultInstantiation();
            return;
        }

        if (values.length!=4&&values.length!=3)
            defaultInstantiation();

        this.x = values[0];
        this.y = values[1];
        this.z = values[2];

        try
        {
            this.yaw = values[3];
        }
        catch (IndexOutOfBoundsException e)
        {
            this.yaw  = 0;
        }
    }

    private void defaultInstantiation()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.yaw = 0;
    }

    @Override
    public void register(World world, String type, int blocks, Plugin plugin) {

        Block block = world.getBlockAt((int)x,(int)y,(int)z);
        FixedMetadataValue value = new FixedMetadataValue(plugin, 1);

        if (blocks == RegisterType.AIR_ONLY.getType()) {
            if (block.getType() == Material.AIR)
                 block.setMetadata(type, value);
        }
        else if (blocks==RegisterType.NOT_AIR.getType())
        {
            if (block.getType() != Material.AIR)
                block.setMetadata(type, value);
        }
        else
            block.setMetadata(type, value);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public double getYaw()
    {
        return yaw;
    }


    public Location getAsLocation(World world)
    {
        return new Location(world, x, y, z,(float)yaw, 0);
    }

    @Override
    public void register(World world, String type, Plugin plugin)
    {
        world.getBlockAt((int)x,(int)y,(int)z).setMetadata(type,new FixedMetadataValue(plugin,1));
    }

    public void unregister() {
        BlockTagManager manager = BlockTagManager.get();
        manager.removeTag((int)x,(int)y,(int)z);
    }

    public void unregister(World world, String type, Plugin p){
        world.getBlockAt((int)x,(int)y,(int)z).removeMetadata(type, p);
    }

    public void register(byte tag) {
        BlockTagManager manager = BlockTagManager.get();
        manager.addBlock((int)x,(int)y,(int)z, tag);
    }

    public void registerWithOverrides(byte tag) {
        BlockTagManager manager = BlockTagManager.get();
        manager.overrideTag((int)x,(int)y,(int)z, tag);
    }

    public SoakBoundary toBoundaryPoint()
    {
        return new SoakBoundary(x,x,y,y,z,z);
    }

    public boolean isBlock(World world, Block comparison)
    {
        Block coord = getAsLocation(world).getBlock();
        return coord.equals(comparison);

    }



}
