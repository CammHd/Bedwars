package me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy;

import me.camm.productions.bedwars.Game.DeathMessages.DeathCause;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;

import java.util.UUID;

/**
 * @author CAMM
 * Interface for modelling entities on a team
 */
public interface IGameTeamable
{
    UUID getUUID();
    BattleTeam getTeam();
    String getName();
    String getType();
    DeathCause getCauseType();
}


/*
                IOwnable ------------------------------|
                getOwner()                             |
                ^                                      |
                |                                      --------> ILifeTimed
IGameTeamable --|                                      |         handleLifeTime()
getUUID()       v                                      |
getTeam()      ITrackable  ------> IAutonomous --------|
getName()       register()           isAlive()
                unregister()         spawn()
                                     remove()

the register and unregister methods are to track the entities when they are spawned in the
game, so we can refer to them in some way to their owners and attribute kills.

golems and silverfish do not stay in the game forever.
golems: 120 seconds, silverfish: 15 seconds



 */