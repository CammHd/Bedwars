package me.camm.productions.bedwars.Game.Entities.ActiveEntities;

import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.DeathMessages.DeathCause;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy.IGameOwnable;
import net.minecraft.server.v1_8_R3.EntityFireball;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

/*
 * @author CAMM
 * Models a travelling fireball with an owner.
 */
public class ThrownFireball implements IGameOwnable
{
    private final Plugin plugin;
    private final BattleTeam team;
    private final BattlePlayer owner;
    private Fireball ball;

    private static final int TIME;
    private static final double MULTIPLIER;

    static {
        TIME = 30;
        MULTIPLIER = 0.15;
    }

    public ThrownFireball(Plugin plugin, BattlePlayer owner) {
        this.plugin = plugin;
        this.owner = owner;
        this.team = owner.getTeam();
        shoot();
    }

    private void shoot()
    {
        Player player = owner.getRawPlayer();
        Location locPlayer = player.getLocation();
        Location locEye = player.getEyeLocation();
        World world = player.getWorld();

        //getting this vector gives a position slightly infront of the player.
        Vector lookDir = locEye.toVector().add(locPlayer.getDirection().clone().multiply(1.25));

        //converting the vector to a location in the world with a pitch and yaw.
        Location spawnLoc = lookDir.toLocation(world, locPlayer.getYaw(),locPlayer.getPitch());


        //spawning the ball
       ball = world.spawn(spawnLoc, Fireball.class);


        //Note: set to 0 for physics.
        ball.setYield(0F);
        ball.setShooter(player);

        Vector direction = locEye.getDirection();

        double x = direction.getX();
        double y = direction.getY();
        double z = direction.getZ();

        double distance = Math.sqrt(x * x + y * y + z * z);
        EntityFireball fireBall = ((CraftFireball)ball).getHandle();

        //converting to unit vector * 0.1 (note that 1 m/tick = 20 m/s, so *0.1 makes it less fast)
        //initially *0.1
        fireBall.dirX = (x/distance)*MULTIPLIER;  //getting the unit value ratio then multiply it.
        fireBall.dirY = (y/distance)*MULTIPLIER;   //so as long as it is the same ratio for each, it is fine.
        fireBall.dirZ = (z/distance)*MULTIPLIER;

        //make sure that the player does not collide with their thrown fireball
        ((CraftPlayer)player).getHandle().collidesWithEntities = false;

        new BukkitRunnable()
        {
            int time = 0;
            public void run()
            {
                //remove the ball after a period of time.
                if (ball.isDead()||time>TIME)
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
        }.runTaskTimer(plugin, 0,5);
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


    @Override
    public String getType() {
        return "fireball";
    }

    @Override
    public DeathCause getCauseType() {
        return DeathCause.FIREBALL;
    }
}
