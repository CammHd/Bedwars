package me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy;

import org.bukkit.entity.Entity;

/**
 * @author CAMM
 * Interface for modelling autonomous entities (Entities with their own AI)
 */
public interface IGameAutonomous extends ITracked
{
    String getName();
    void spawn();
    void remove();
    boolean isAlive();
    void handleEntityTarget(Entity entity);

}

