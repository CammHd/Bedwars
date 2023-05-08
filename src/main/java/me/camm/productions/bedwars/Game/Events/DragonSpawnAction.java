package me.camm.productions.bedwars.Game.Events;

import me.camm.productions.bedwars.Game.GameRunner;
import me.camm.productions.bedwars.Game.Scoreboard.ScoreBoardHeader;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.GameDragon;
import me.camm.productions.bedwars.Listeners.EntityActionListener;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;


import java.util.Collection;


/**
 * @author CAMM
 * This class models a dragon spawning action.
 */
public class DragonSpawnAction extends GameActionPhysical {
    private final GameRunner runner;

    //constructor
    public DragonSpawnAction(GameRunner runner) {
        this.runner = runner;
        spent = false;
        header = ScoreBoardHeader.SUDDEN_DEATH_HEADER.getPhrase();
    }

    @Override
    public void activate() {

        //if the event has already been activated, we do not want to activate it again
        if (spent)
            return;
        spent = true;

        EntityActionListener listener = runner.getDamageListener();
        World world = runner.getArena().getWorld();

       final Coordinate centre = runner.getArena().getBounds().getCoordinateAverage();
       final double y = centre.getY();

        Collection<BattleTeam> teams = runner.getArena().getTeams().values();

        teams.forEach(battleTeam ->
        {

            //BLOCK with label
            CURRENT_TEAM:
            {
                if (battleTeam.isEliminated())
                    break CURRENT_TEAM;

                //getting the location to spawn the dragon (right above the bed.)
                Coordinate current = battleTeam.getBed().getCoordinateAverage();
                double delta = Math.abs(current.getY() - y);

                //we use this in the case that they bought dragon buff.
                int iterations = battleTeam.getDragonSpawnNumber();
                sender.sendMessage(battleTeam.getTeamColor().getChatColor() + " +" + iterations + " " + battleTeam.getTeamColor().getName() + " dragon");


                //spawning the dragons
                while (iterations > 0) {

                    Location spawn = new Location(world, current.getX(), current.getY() + (delta * iterations), current.getZ());
                   //we don't want to spawn the dragons in the same area, so we put them above or below the original location

                    GameDragon dragon = new GameDragon(((CraftWorld) world).getHandle(), spawn, runner.getArena(), battleTeam, new Location(world, centre.getX(), centre.getY(), centre.getZ()), listener);
                    dragon.spawn();
                    dragon.register();
                    iterations--;
                }
            }
        });


    }

    @Override
    String getHeader() {
        return header;
    }
}
