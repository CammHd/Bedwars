package me.camm.productions.bedwars.Game.Teams.Traps;

import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Teams.TeamTitle;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamInventoryConfig;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

/**
 * @author CAMM
 * This class models a counter-offensive trap
 */
public class OffensiveTrap extends GameTrap
{

    private static final String NAME = "Counter-offensive trap";
    public OffensiveTrap(BattleTeam team, GameBoundary bounds) {
        this.team = team;
        this.bounds = bounds;
    }

    @Override
    public void activate()
    {

        Map<UUID, BattlePlayer> teamPlayers = team.getPlayers();

        new BukkitRunnable(){
            @Override
            public void run() {

                //Giving all team players speed and jump for 15 seconds.
                   for (BattlePlayer player: teamPlayers.values()) {

                       Player current = player.getRawPlayer();
                       current.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1, false,false));
                       current.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 300, 1, false,false));
                   }

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
        return TeamInventoryConfig.OFFENSE_TRAP;
    }


    public String name() {
        return NAME;
    }

    @Override
    public TeamTitle getTrapTitle() {
        return TeamTitle.TRIGGERED;
    }
}
