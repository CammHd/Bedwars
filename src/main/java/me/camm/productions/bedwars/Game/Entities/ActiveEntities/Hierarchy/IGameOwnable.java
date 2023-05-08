package me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy;

import me.camm.productions.bedwars.Game.BattlePlayer;

/**
 * @author CAMM
 * Interface for modelling entities that have an owner
 */
public interface IGameOwnable extends IGameTeamable
{
    BattlePlayer getOwner();
    String getName();
}
