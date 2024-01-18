package me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy;


/**
 * @author CAMM
 * Interface for modelling autonomous entities that have a timer before they despawn
 */
public interface ILifeTimed extends IGameOwnable,IGameAutonomous
{
  void handleLifeTime();
  double getHealth();

}
