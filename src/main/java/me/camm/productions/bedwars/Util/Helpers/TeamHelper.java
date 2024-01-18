package me.camm.productions.bedwars.Util.Helpers;

import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Scoreboard.PlayerBoard;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;

import java.util.Collection;

public class TeamHelper
{
    public static BattleTeam isVictorFound(Collection<BattleTeam> teams)
    {
        int teamNumber = teams.size();

        BattleTeam candidate= null;
        for (BattleTeam currentTeam: teams)
        {
            if (currentTeam.isEliminated())
                teamNumber --;
            else candidate = currentTeam;
        }
        //If the remaining team number is 1 and the candidate winner is not null,
        //then return the candidate

        return teamNumber==1&&candidate!=null? candidate:null;


    }

    public static synchronized void updateTeamBoardStatus(Collection<BattlePlayer> players)
    {
        for (BattlePlayer arenaPlayer: players) {
            PlayerBoard board = arenaPlayer.getBoard();
            board.updateTeamStatuses();
            board.switchPrimaryBuffer();
        }
    }

    public static boolean allTeamsEliminated(Collection<BattleTeam> teams){
        for (BattleTeam team: teams) {
            if (!team.isEliminated())
                return false;
        }
        return true;
    }
}
