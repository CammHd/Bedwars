package me.camm.productions.bedwars.Util.Locations.Boundaries;


import me.camm.productions.bedwars.Util.Locations.IRegistratable;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public abstract class Boundary<T extends Number> implements IRegistratable {
    protected T x1, x2, y1, y2, z1, z2;
    protected abstract void analyze();
    protected abstract void reArrange();
    protected abstract void dissectArray();
    protected abstract T[] reset();

    public void register(World world, String type, Plugin plugin){

    }

    public void unregister(World world, String type, Plugin plugin){

    }

    public void register(World world, String type, int blocks, Plugin plugin) {

    }

}
