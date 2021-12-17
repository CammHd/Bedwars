package me.camm.productions.bedwars.Entities.ActiveEntities;

import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Entities.ActiveEntities.Hierarchy.IGameOwnable;
import net.minecraft.server.v1_8_R3.EntityFireball;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class ThrownFireball implements IGameOwnable
{
    private final Plugin plugin;
    private final BattleTeam team;
    private final BattlePlayer owner;
    private Fireball ball;

    public ThrownFireball(Plugin plugin, BattlePlayer owner) {
        this.plugin = plugin;
        this.owner = owner;
        this.team = owner.getTeam();
        shoot();
    }

    private void shoot()
    {
        Player player = owner.getRawPlayer();

       ball = player.getWorld().spawn(player.getEyeLocation().toVector()
                .add(player.getLocation().getDirection().multiply(1.25)).toLocation
                        (player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch()),Fireball.class);  //Most likely 1.25


        //Note: set to 0 for physics.
        ball.setYield((float)4.0);
        ball.setShooter(player);

        Vector direction = player.getEyeLocation().getDirection();

        double x = direction.getX();
        double y = direction.getY();
        double z = direction.getZ();

        double distance = Math.sqrt(x * x + y * y + z * z);
        EntityFireball fireBall = ((CraftFireball)ball).getHandle();

        fireBall.dirX = (x/distance)*0.1;
        fireBall.dirY = (y/distance)*0.1;
        fireBall.dirZ = (z/distance)*0.1;


        ((CraftPlayer)player).getHandle().collidesWithEntities = false;

        new BukkitRunnable()
        {
            int time = 0;
            public void run()
            {
                if (ball.isDead()||time>30)
                {
                    ((CraftPlayer)player).getHandle().collidesWithEntities = true;
                    ball.remove();
                    cancel();
                }
                else
                {
                    ((CraftPlayer)player).getHandle().collidesWithEntities = true;
                    time++;
                }
            }
        }.runTaskTimer(plugin, 0,5L);
    }


    @Override
    public BattlePlayer getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return ball == null? null: ball.getCustomName();
    }

    @Override
    public UUID getUUID() {
        return ball == null ? null : ball.getUniqueId();
    }

    @Override
    public BattleTeam getTeam() {
        return team;
    }
}
