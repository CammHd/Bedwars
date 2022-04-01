package me.camm.productions.bedwars.Entities.ActiveEntities;

import me.camm.productions.bedwars.Arena.GameRunning.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.DeathMessages.Cause;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Entities.ActiveEntities.Hierarchy.ILifeTimed;
import me.camm.productions.bedwars.Listeners.EntityActionListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Silverfish;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.UUID;


public class BedBug implements ILifeTimed
{
    private final BattleTeam team;
    private final BattlePlayer owner;
    private final Arena arena;
    private Silverfish bug;
    private final Location loc;

    private final EntityActionListener listener;
    private static final int MAX_TIME;
    private int aliveTime;

    static {
        MAX_TIME = 15;
    }

    public BedBug(BattleTeam team, BattlePlayer owner,Arena arena,EntityActionListener listener,Location loc) {
        this.team = team;
        this.owner = owner;
        this.arena = arena;
        this.aliveTime = MAX_TIME;
        this.listener = listener;
        this.loc = loc;
    }

    @Override
    public void spawn()
    {
        World world = arena.getWorld();
        ChatColor chatColor = team.getTeamColor().getChatColor();
        String teamName = team.getTeamColor().getName();
        new BukkitRunnable()
        {
            @Override
            public void run() {
                bug = world.spawn(loc,Silverfish.class);
                bug.setCustomName(chatColor+teamName+getType());
                bug.setCustomNameVisible(true);
                register();
                cancel();
            }
        }.runTask(arena.getPlugin());
        handleLifeTime();


    }



    public synchronized void handleEntityTarget(Entity toTarget)
    {
        if (toTarget instanceof LivingEntity)
        bug.setTarget((LivingEntity)toTarget);
    }

    @Override
    public String getType(){
        return "Bed Bug";
    }

    @Override
    public void handleLifeTime()
    {
        listener.addEntity(this);
        Collection<BattlePlayer> players = arena.getPlayers().values();
        new BukkitRunnable() {
            BattlePlayer target = null;
            @Override
            public void run()
            {
                if (bug.isDead() || !bug.isValid())
                {
                    unregister();
                    cancel();
                    return;
                }

                aliveTime --;
                if (aliveTime <=0)
                {
                    bug.remove();
                    unregister();
                    remove();
                    cancel();
                }

                TARGET:
                {
                    if (bug.getTarget()!=null&&target!=null) {
                        if (bug.getTarget().equals(target.getRawPlayer()) && target.getIsAlive())
                        break TARGET;
                    }
                    bug.setTarget(null);
                            target = null;



                    for (BattlePlayer player : players) {
                        if (!player.getIsAlive())
                            continue;

                        if (player.getTeam().equals(team))
                            continue;


                        if (player.getRawPlayer().getLocation().distanceSquared(bug.getLocation()) <= 576) {
                            bug.setTarget(player.getRawPlayer());
                            target = player;
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(arena.getPlugin(),0,20);
    }


    @Override
    public void remove()
    {
        bug.remove();
    }


    public UUID getUUID()
    {
        return bug==null? null: bug.getUniqueId();
    }

    public BattlePlayer getOwner()
    {
        return owner;
    }

    @Override
    public boolean isAlive() {
        return bug != null && !bug.isDead();
    }

    @Override
    public double getHealth() {
        return bug==null?0:bug.getHealth();
    }

    @Override
    public BattleTeam getTeam() {
        return team;
    }

    @Override
    public Cause getCauseType() {
        return Cause.NORMAL;
    }

    @Override
    public String getName() {
        return bug == null ? null: bug.getCustomName();
    }

    @Override
    public void register() {
       listener.addEntity(this);
    }

    @Override
    public void unregister() {
     listener.removeEntity(this.getUUID());
    }
}
