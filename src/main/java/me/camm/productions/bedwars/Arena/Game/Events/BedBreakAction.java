package me.camm.productions.bedwars.Arena.Game.Events;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Game.GameRunner;
import me.camm.productions.bedwars.Arena.Players.Scoreboards.ScoreBoardHeader;


import static me.camm.productions.bedwars.Arena.Teams.TeamTitle.ALL_BEDS_DESTROYED;


/**
 * @author CAMM
 * This class models the action of breaking the bed at a given time
 */
public class BedBreakAction extends GameActionPhysical {

    private final GameRunner runner;

    //constructor
    public BedBreakAction(GameRunner runner) {
        super();
        this.runner = runner;
        header = ScoreBoardHeader.BED_GONE_HEADER.getPhrase();
        spent = false;
    }


    /*
     * Activates the event if it is not spent.
     */
    @Override
    public void activate() {
        if (spent)
            return;
        spent = true;

        Arena arena = runner.getArena();

        sender.sendMessage(GameEventText.DESTROY_BEDS.getText());
        arena.getTeams().forEach((string, battleTeam) -> {

            //if the team's bed does not exist, then send an additional message that says that all beds are destroyed

            if (!battleTeam.getBedExists())
                battleTeam.sendTeamTitle(ALL_BEDS_DESTROYED.getMessage(), "",10,40,10);
                //10, 40, and 10 are just arbitrary values. We could have them as any positive int. It's just that these values
            //work out well (The numbers are in values of ticks. 20 ticks = 1 second)

            //maybe put them into final variables?


            //we attempt to put them on their last stand (break their bed). The method already handles messaging etc.
            battleTeam.putOnLastStand();
        });

    }


    //getting the header
    @Override
    String getHeader() {
        return header;
    }
}
