package me.camm.productions.bedwars.Arena.Teams.TeamTraps;

import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.TeamTitle;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamInventoryConfig;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;
import me.camm.productions.bedwars.Util.PacketSound;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


/**
 * @author CAMM
 * Models an alarm trap object
 */
public class AlarmTrap extends GameTrap
{
    private static final int MILK_TIME_MILLIS = 30000;
    private static final int ALARM_TIME_TICKS = 120;
    private static final String NAME = "Alarm Trap";

    public AlarmTrap(BattleTeam team, GameBoundary effectiveRange) {
        this.team = team;
        this.bounds = effectiveRange;
    }

    @Override
    public void activate() {
        World world = team.getArena().getWorld();
        Map<UUID, BattlePlayer> arenaPlayers = team.getArena().getPlayers();
        Map<UUID, BattlePlayer> teamPlayers = team.getPlayers();


        new BukkitRunnable() {
            public void run() {

                final Set<BattlePlayer> activators = new HashSet<>();

                //getting the close entities which may have activated them
                for (Entity entity: bounds.getCloseEntities(world))
                {
                    if (!teamPlayers.containsKey(entity.getUniqueId()) &&
                            entity instanceof Player &&
                            arenaPlayers.containsKey(entity.getUniqueId())) {

                        BattlePlayer current = arenaPlayers.get(entity.getUniqueId());

                        if ( (System.currentTimeMillis() - current.getLastMilk()) > MILK_TIME_MILLIS) {
                            activators.add(current);
                            current.getRawPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
                        }
                        //this should send a packet remove effect to the packet handler, so it should be fine if we
                        //don't update it here
                    }
                }


                //playing an alarm for all players that activated the trap
                new BukkitRunnable()
                {
                    int iterations = 0;
                    boolean note;
                    @Override
                    public void run()
                    {
                        Iterator<BattlePlayer> iter = activators.iterator();

                        while (iter.hasNext()) {
                            BattlePlayer trigger = iter.next();
                            if (trigger != null && trigger.isAlive())
                            {


                                Location loc = trigger.getRawPlayer().getLocation();
                                if (!bounds.containsCoordinate(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                                   continue;


                                if (note)
                                        team.sendTeamSoundPacket(PacketSound.ALARM);
                                    else
                                        team.sendTeamSoundPacket(PacketSound.ALARM_TWO);
                                    note = !note;

                            }
                            else
                                iter.remove();
                        }

                        if (activators.isEmpty()) {
                            cancel();
                        }

                        //just an arbitrary amount for the alarm.
                        if (iterations > ALARM_TIME_TICKS)
                            cancel();

                        iterations ++;



                    }
                }.runTaskTimer(team.getArena().getPlugin(),0,2);

                cancel();
            }
        }.runTask(team.getArena().getPlugin());
    }

    @Override
    public BattleTeam getTeam() {
        return team;
    }

    @Override
    public TeamInventoryConfig getTrapConfig() {
        return TeamInventoryConfig.ALARM_TRAP;
    }

    public String name() {
        return NAME;
    }

    @Override
    public TeamTitle getTrapTitle() {
        return TeamTitle.ALARM;
    }
}


