package me.camm.productions.bedwars.Listeners;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.BedBug;
import me.camm.productions.bedwars.Game.Entities.BridgeEgg;
import me.camm.productions.bedwars.Util.PacketSound;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

public class ProjectileListener implements Listener
{
    private final Plugin plugin;
    private final Arena arena;
    private final EntityActionListener listener;

    public ProjectileListener(Plugin plugin, Arena arena,EntityActionListener actionListener) {
        this.plugin = plugin;
        this.arena = arena;
        this.listener = actionListener;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event)
    {
       PlayerTeleportEvent.TeleportCause cause = event.getCause();
       switch (cause) {
           case ENDER_PEARL:
           case END_PORTAL:
           case NETHER_PORTAL:
               event.setCancelled(true);
       }

    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event)
    {
        Projectile p = event.getEntity();

        ProjectileSource source = p.getShooter();
        if (!(source instanceof Player))
            return;

        Player shooter = (Player)source;

        if (p instanceof EnderPearl)
            handlePearlPlacement((EnderPearl) p, shooter);
        else if (p instanceof Snowball)
            handleSnowBallHit((Snowball)p, shooter);

        //So now it's either a pearl, snowball, or arrow.
        //we'll handle arrows in the damage listener.

    }

    public void handlePearlPlacement(EnderPearl pearl, Player shooter)
    {
        Vector direction = pearl.getVelocity().clone().normalize().multiply(-1);
        Vector origin = pearl.getLocation().toVector();
        Location loc = pearl.getLocation();
        double distance = shooter.getLocation().distance(loc);
        World w = shooter.getWorld();

        final float pitch, yaw;
        pitch = shooter.getLocation().getPitch();
        yaw = shooter.getLocation().getYaw();
        Location foot = origin.toLocation(w);

        int travelled = 0;
        do
        {

            Block bottom = foot.getBlock();
            Block top = foot.clone().add(0,1,0).getBlock();



            if ( (bottom == null || bottom.getType()== Material.AIR ) && (top == null || top.getType()==Material.AIR))
            {


                Location floored = new Location(foot.getWorld(),foot.getBlockX(),foot.getBlockY(),foot.getBlockZ());
                floored.setPitch(pitch);
                floored.setYaw(yaw);
                shooter.teleport(floored.add(0.5,0,0.5), PlayerTeleportEvent.TeleportCause.PLUGIN);
                arena.sendLocalizedSound(PacketSound.ENDERMAN,floored, 10);
                shooter.setFallDistance(0f);
                //shooter.sendMessage("[DEBUG] Teleport loc: "+ foot.getX()+" "+foot.getY()+" "+foot.getZ());

                break;
            }


            travelled += 1;
            foot = origin.add(direction.clone()).toLocation(w);//
        }
        while (travelled < distance);
    }

    public void handleSnowBallHit(Snowball ball, Player shooter)
    {
        Map<UUID, BattlePlayer> registeredPlayers = arena.getPlayers();
        if (!registeredPlayers.containsKey(shooter.getUniqueId()))
            return;

        BattlePlayer player = registeredPlayers.get(shooter.getUniqueId());
        BedBug bug = new BedBug(player.getTeam(),player, arena,listener,ball.getLocation());
        bug.spawn();
    }



    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        Projectile p = event.getEntity();
        if (p.getShooter() == null || !(p.getShooter() instanceof Player))
            return;

        Player shooter = ((Player) p.getShooter());

        if (!arena.getPlayers().containsKey(shooter.getUniqueId()))
            return;

        if (p instanceof Egg)
            new BridgeEgg((Egg)p,plugin,(byte)arena.getPlayers().get(shooter.getUniqueId()).getTeam().getTeamColor().getValue());
    }
}
