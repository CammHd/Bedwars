package me.camm.productions.bedwars.Entities;

import me.camm.productions.bedwars.Util.Locations.BridgeFiller;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Egg;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @author CAMM
 * Models a bridge egg object that the player throws
 */

public class BridgeEgg
{
    private final Egg egg;
    private final Plugin plugin;
    private final byte data;

    //Data is the wool color to fill with (it is a byte)
    //The object is created once the egg has already been launched
    public BridgeEgg(Egg egg, Plugin plugin, byte data)
    {
        this.egg = egg;
        this.plugin = plugin;
        this.data = data;
        trackFlying();
    }

    //At this point the egg is already flying (We call this from the ProjectileLaunchEvent)
    public void trackFlying()
    {

        //new thread synchronized to the game's tick speed for setting blocks
        new BukkitRunnable()
        {
            final World world = egg.getWorld();
            int iterations = 0;

            @Override
            public void run()
            {
                //If the egg has been removed by the game or it is out of the world, cancel the flighy
                if (egg.isDead() || !egg.isValid() || egg.getLocation().getY() <0) {
                    cancel();
                    return;
                }

                //cancel the flight after a certain amount of iterations
                if (iterations > 35)
                {
                    egg.remove();
                    cancel();
                    return;
                }


                //get the areas slightly behind the egg to fill
                Vector currentLocation = egg.getLocation().toVector().add(egg.getVelocity().clone().multiply(-1.25));
                Vector fillTo = egg.getLocation().toVector().add(egg.getVelocity().clone().multiply(-2.5));

                //convert to locations
                Location current = currentLocation.toLocation(world);
                Location filler = fillTo.toLocation(world);

                //make a filler object, and then fill the area.
                BridgeFiller fillZone = new BridgeFiller(current,filler);
                fillZone.fill(Material.WOOL,data, world);
                iterations ++;

            }

        }.runTaskTimer(plugin,5,1);



    }
}