package me.camm.productions.bedwars.Game.Teams.Traps;

import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Teams.TeamTitle;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamInventoryConfig;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CAMM
 * This class models a miner-fatigue trap
 */
public class MiningTrap extends GameTrap
{

    private static final String NAME = "Mining-Fatigue trap";


    public MiningTrap(BattleTeam team, GameBoundary bounds) {
        this.team = team;
        this.bounds = bounds;
    }

    //activate the trap
    @Override
    public void activate()
    {
        World world = team.getArena().getWorld();
        ConcurrentHashMap<UUID, BattlePlayer> teamPlayers = team.getPlayers();

        //get all of the close entities, and add mining fatigue to them. Gives mining- fatigue
        //for 10 seconds.
        new BukkitRunnable() {
            public void run() {

                bounds.getCloseEntities(world).forEach(entity -> {
                    if (!teamPlayers.containsKey(entity.getUniqueId()) && entity instanceof Player)
                    {
                        ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,200, 1,false));
                    }
                });

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
        return TeamInventoryConfig.MINER_TRAP;
    }


    public String name() {
        return NAME;
    }

    @Override
    public TeamTitle getTrapTitle() {
        return TeamTitle.TRIGGERED;
    }
}

