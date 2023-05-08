package me.camm.productions.bedwars.Entities.ActiveEntities;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.DeathMessages.Cause;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.TeamColor;
import me.camm.productions.bedwars.Entities.ActiveEntities.Hierarchy.ILifeTimed;
import me.camm.productions.bedwars.Listeners.EntityActionListener;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.UUID;


/**
 * @author CAMM
 * Models a dream defender.
 */
public class DreamDefender implements ILifeTimed
{
    private final BattleTeam team;
    private final BattlePlayer owner;
    private final Arena arena;
    private IronGolem golem;
    private final EntityActionListener listener;

    private static final int MAX_TIME;
    private static final int AGGRO_DISTANCE_SQUARED;

    private int timeLeft;
    private final Location toSpawn;

    static {

        AGGRO_DISTANCE_SQUARED = 576;

        MAX_TIME = 120;
    }

    public DreamDefender(BattleTeam team, BattlePlayer owner, Arena arena, Location toSpawn, EntityActionListener listener) {
        this.team = team;
        this.owner = owner;
        this.arena = arena;
        this.timeLeft = MAX_TIME;
        this.toSpawn = toSpawn;
        this.listener = listener;
    }

    @Override
    public Cause getCauseType() {
        return Cause.NORMAL;
    }

    @Override
    public void handleLifeTime()
    {
        Collection<BattlePlayer> players = arena.getPlayers().values();



        new BukkitRunnable()
        {
            BattlePlayer target = null;
            @Override
            public void run() {

                if (timeLeft <=0 || golem.isDead())
                {
                    unregister();
                    golem.remove();
                    cancel();
                    return;
                }


                //golems are not aggressive normally, so we need to target for them.
                TARGET:
                {

                    if (golem.getTarget()!=null && target != null) {
                        if (golem.getTarget().equals(target.getRawPlayer()) && target.isAlive())
                            break TARGET;
                    }
                    target = null;
                    golem.setTarget(null);




                    for (BattlePlayer player : players) {
                        if (!player.isAlive())
                            continue;

                        if (player.getTeam().equals(team))
                            continue;



                        //sqrt(576) = 24, which is usually the aggro distance
                        if (player.getRawPlayer().getLocation().distanceSquared(golem.getLocation()) <= AGGRO_DISTANCE_SQUARED) {
                          golem.setTarget(player.getRawPlayer());
                          target = player;
                          break;
                        }
                    }
                }



                golem.setCustomName(team.getTeamColor().getChatColor()+""+team.getTeamColor().getName()+" "+getType()+" ["+timeLeft+"]");
                timeLeft --;
            }
        }.runTaskTimer(arena.getPlugin(),0,20);

    }

    @Override
    public void handleEntityTarget(Entity toTarget) {
        if (toTarget instanceof LivingEntity)
         golem.setTarget((LivingEntity) toTarget);

    }

    @Override
    public String getType(){
        return "Dream Defender";
    }

    @Override
    public void remove() {
      golem.remove();
    }

    @Override
    public boolean isAlive() {
        return golem != null && !golem.isDead();
    }

    @Override
    public double getHealth() {
        return golem==null?0:golem.getHealth();
    }

    @Override
    public BattlePlayer getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return golem == null? null: golem.getCustomName();
    }

    @Override
    public void spawn() {

            new BukkitRunnable()
            {
                @Override
                public void run() {

                    //so it doesn't spawn in the ground.
                    Location spawning = toSpawn.add(owner.getRawPlayer().getEyeLocation().getDirection().multiply(-1.5).add(new Vector(0,1,0)));

                    golem = arena.getWorld().spawn(spawning,IronGolem.class);
                    golem.setPlayerCreated(false);
                    TeamColor color = team.getTeamColor();
                    golem.setCustomName(color.getChatColor()+color.getName()+" "+getType());
                    golem.setHealth(20);
                    register();
                    cancel();
                }
            }.runTask(arena.getPlugin());
            handleLifeTime();


    }

    @Override
    public UUID getUUID() {
        return golem==null ? null : golem.getUniqueId();
    }

    @Override
    public BattleTeam getTeam() {
        return team;
    }


    @Override
    public void register()
    {
     listener.addEntity(this);
    }

    @Override
    public void unregister() {
        if (golem!=null)
      listener.removeEntity(golem.getUniqueId());
    }
}
