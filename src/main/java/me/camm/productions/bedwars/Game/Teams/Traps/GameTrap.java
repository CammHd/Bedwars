package me.camm.productions.bedwars.Game.Teams.Traps;

import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;

/**
 * @author CAMM
 * Abstract class for traps
 */
public abstract class GameTrap implements ITrap {
    protected BattleTeam team;
    protected GameBoundary bounds;

}
