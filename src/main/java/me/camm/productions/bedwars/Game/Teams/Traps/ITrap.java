package me.camm.productions.bedwars.Game.Teams.Traps;

import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Teams.TeamTitle;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamInventoryConfig;

/**
 * @author CAMM
 * This interface models a generic trap
 */
public interface ITrap
{
    /* Activates the trap*/
    void activate();

    BattleTeam getTeam();

    /* get the configuration item for the trap */
    TeamInventoryConfig getTrapConfig();

    TeamTitle getTrapTitle();

    String name();
}
