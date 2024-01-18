package me.camm.productions.bedwars.Util.Helpers;

import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Teams.TeamTitle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static me.camm.productions.bedwars.Game.Scoreboard.ScoreBoardHeader.TEAM_ALIVE;
import static me.camm.productions.bedwars.Game.Scoreboard.ScoreBoardHeader.TEAM_DEAD;

public class PlayerHelper {

    public static String getTeamStatus(BattleTeam team)
    {
        try {
            String previousName;
            String entry = team.getDisplayScoreboardEntry();
            if (team.isEliminated())
                previousName = entry + " " + TEAM_DEAD.getPhrase();
            else if (!team.getBedExists())
                previousName = entry + " " + team.getRemainingPlayers();
            else
                previousName = entry + " " + TEAM_ALIVE.getPhrase();

            return previousName;
        }
        catch (NullPointerException e)
        {
            return "Team DNE";
        }
    }

    public static String addRespawnNumber(TeamTitle title, int seconds)
    {
        if (title == TeamTitle.RESPAWN_AFTER)
        {
            return title.getMessage() + seconds +"!\"}";
        }
        return title.getMessage();
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{null,null,null,null});
    }
}
