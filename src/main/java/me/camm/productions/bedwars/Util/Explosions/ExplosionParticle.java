package me.camm.productions.bedwars.Util.Explosions;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;



/**
 *
 * @author CAMM
 * Helper for creating explosion particles.
 */
public class ExplosionParticle
{
    private final World world;
    private final Location location;
    private final Plugin plugin;

    public ExplosionParticle(Location location, World world, Plugin plugin) {
        this.world = world;
        this.location = location;
        this.plugin = plugin;
        playParticle();
    }

    private void playParticle() {


        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) {

                new BukkitRunnable() {
                    public void run() {
                        PacketPlayOutWorldParticles particles =
                                new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_HUGE, true, (float) location.getX(), (float) location.getY(),
                                        (float) location.getZ(),0,0,0,1,1);

                        PacketPlayOutWorldParticles smoke = new PacketPlayOutWorldParticles(EnumParticle.CLOUD,true,(float)location.getX(),(float)location.getY(),(float)location.getZ(),0,0,0,(float)1,30);

                        Player current = (Player) entity;

                        ((CraftPlayer) current).getHandle().playerConnection.sendPacket(particles);
                        ((CraftPlayer)current).getHandle().playerConnection.sendPacket(smoke);
                        cancel();
                    }
                }.runTaskAsynchronously(plugin);
            }
        }
    }
}