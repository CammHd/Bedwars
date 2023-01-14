package me.camm.productions.bedwars.Arena.Players.Scoreboards;


import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Util.DataSets.ScoreSet;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;


import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import static me.camm.productions.bedwars.Arena.Players.Scoreboards.ScoreBoardHeader.*;

/**
 * @author CAMM
 * Class for the scoreboard of the players
 */
public class PlayerBoard
{
    private final BattlePlayer player;
    private final Arena arena;
    private final HashMap<String, ScoreSet> scores;
    private Scoreboard board;

    //we use 2 objectives: a primary and a buffer to make the update transitions smoothly without flickering.
    private ScoreboardObjective primary;
    private ScoreboardObjective buffer;

    private boolean isShowingPrimary;
    private boolean isInitialized;
    private final String easterEgg;

    private volatile static boolean isBoardSet;

    static {
        isBoardSet = false;
    }

    public PlayerBoard(BattlePlayer player, Arena arena)
    {
        this.player = player;
        this.arena = arena;
        scores = new HashMap<>();
        this.isInitialized = false;

        BoardEasterEgg[] credit = BoardEasterEgg.values();
       easterEgg = credit[new Random().nextInt(credit.length)].getPhrase();

       if (!isBoardSet)
       {
           isBoardSet = true;
           //We unregister them in case they have been registered before in a previous game that has taken place in the world.

               World world = player.getRawPlayer().getWorld();
               Scoreboard initial = ((CraftWorld)world).getHandle().getScoreboard();
               Collection<ScoreboardObjective> objectives = initial.getObjectives();
               for (ScoreboardObjective objective: objectives)
               {
                   try {
                       initial.unregisterObjective(objective);
                       initial.handleObjectiveRemoved(objective);
                   }
                   catch (IllegalArgumentException | IllegalStateException ignored)
                   {

                   }
               }
       }
        createDefault();
    }


    private void createDefault()
    {
        this.isShowingPrimary = true;
        board = new Scoreboard();

        this.primary = board.registerObjective(OBJECTIVE_ONE.getPhrase()+player.getNumber(),new ScoreboardBaseCriteria(DUMMY.getPhrase()));
        this.primary.setDisplayName(TITLE.getPhrase());

      this.buffer = board.registerObjective(OBJECTIVE_TWO.getPhrase()+player.getNumber(), new ScoreboardBaseCriteria(DUMMY.getPhrase()));
       this.buffer.setDisplayName(TITLE.getPhrase());


         //0 means create
        send(new PacketPlayOutScoreboardObjective(primary, 0));

        send(new PacketPlayOutScoreboardDisplayObjective(1,primary));  //show the primary

        Collection<BattleTeam> teams = arena.getTeams().values();
        int selectedScore = teams.size()+7; // 7 to account for the other scores.


        scores.put(TIME.getPhrase(), new ScoreSet(board,selectedScore, TIME.getPhrase(), DIAMOND_TWO_HEADER.getPhrase(), player, primary, buffer));
        scores.get(TIME.getPhrase()).sendPrimary();  //time
        selectedScore--;

        scores.put(B_ONE.getPhrase(), new ScoreSet(board,selectedScore,TIME.getPhrase(), B_ONE.getPhrase(), player, primary, buffer));  //blank space
        scores.get(B_ONE.getPhrase()).sendPrimary();
        selectedScore--;


        //for the amount of teams in the arena
        for (BattleTeam team : teams) {
            String currentTeamScore = getTeamStatus(team);
            String identifier;

            if (player.getTeam().getTeamColor().getName().equalsIgnoreCase(team.getTeamColor().getName()))  //if the player is on the same team
            {
                identifier = CURRENT_TEAM.getPhrase();
                currentTeamScore += CURRENT_TEAM.getPhrase();
            } else {
                identifier = team.getTeamColor().getName();
            }

            ScoreSet current = new ScoreSet(board, selectedScore, identifier, currentTeamScore, player, primary, buffer);
            current.sendPrimary();
            scores.put(current.getIdentifier(), current);
            selectedScore--;
        }

        //blank score for formatting
        scores.put(B_TWO.getPhrase(), new ScoreSet(board, selectedScore, B_TWO.getPhrase(), B_TWO.getPhrase(),player,primary,buffer));  //blank space
        scores.get(B_TWO.getPhrase()).sendPrimary();
        selectedScore--;


        ////kills, finals, beds
        scores.put(KILLS.getPhrase(), new ScoreSet(board, selectedScore, KILLS.getPhrase(), KILLS.getPhrase()+player.getKills(), player, primary, buffer));
        scores.get(KILLS.getPhrase()).sendPrimary();
        selectedScore--;

        scores.put(FINALS.getPhrase(), new ScoreSet(board, selectedScore, FINALS.getPhrase(), FINALS.getPhrase()+player.getFinals(), player, primary, buffer));
        scores.get(FINALS.getPhrase()).sendPrimary();
        selectedScore--;

        scores.put(BEDS.getPhrase(), new ScoreSet(board, selectedScore, BEDS.getPhrase(),BEDS.getPhrase()+player.getBeds(), player, primary, buffer));
        scores.get(BEDS.getPhrase()).sendPrimary();
        selectedScore--;

        scores.put(B_THREE.getPhrase(), new ScoreSet(board, selectedScore, B_THREE.getPhrase(),B_THREE.getPhrase(),player, primary, buffer));
        scores.get(B_THREE.getPhrase()).sendPrimary();
        selectedScore--;

        //easter egg
        scores.put(SPACE_CREDIT.getPhrase(), new ScoreSet(board,selectedScore,SPACE_CREDIT.getPhrase(),easterEgg,player,primary,buffer));
        scores.get(SPACE_CREDIT.getPhrase()).sendPrimary();

    }

    /*
    @author CAMM
    This method updates the board of the player in respect to the situations of
    both their team and the other teams.
     */
    public synchronized void updateTeamStatuses()
    {
      arena.getTeams().forEach((string, team) -> {

          if (team.equals(player.getTeam()))
              setScoreName(CURRENT_TEAM.getPhrase(),getTeamStatus(team)+CURRENT_TEAM.getPhrase());
          else
              setScoreName(team.getTeamColor().getName(),getTeamStatus(team));
      });
    }



    /*
    @author CAMM
    Refreshes the information displayed on the player scoreboards according to the scores
    stored in the "scores" hashmap. This method does NOT update the scores of the teams, time, or
    player kills, finals, and beds. Those updates should be controlled by other methods.
     */
    public void switchPrimaryBuffer()
    {
      //If we are showing the primary board...
        if (this.isShowingPrimary)
        {

            //Unregister and destroy the primary scoreboard.
            this.isShowingPrimary = false;
            board.unregisterObjective(primary);
            board.handleObjectiveRemoved(primary);

            send(new PacketPlayOutScoreboardObjective(primary, 1));  // 1 means destroy
            send(new PacketPlayOutScoreboardObjective(buffer, 0)); // 0 means create
            send(new PacketPlayOutScoreboardDisplayObjective(1,buffer)); //1 means side display


            scores.forEach((String, ScoreSet) ->
            {
              ScoreSet.recalculate();
                ScoreSet.sendBuffer();
            }
        );
            board.unregisterObjective(primary);



            if (isInitialized) {
                primary = board.registerObjective(OBJECTIVE_ONE.getPhrase() + player.getNumber(), new ScoreboardBaseCriteria(DUMMY.getPhrase()));
                primary.setDisplayName(TITLE.getPhrase());
            }
            else
                isInitialized = true;


        }
        else//we were showing buffer, now we show prim
        {

            this.isShowingPrimary = true;

            send(new PacketPlayOutScoreboardObjective(buffer,1));  //destroy the buffer

            board.unregisterObjective(buffer);
            board.handleObjectiveRemoved(buffer);

            send(new PacketPlayOutScoreboardObjective(primary,0)); //create
            send(new PacketPlayOutScoreboardDisplayObjective(1,primary));

            scores.forEach((String, ScoreSet) ->
                      {
                        ScoreSet.recalculate();
                        ScoreSet.sendPrimary();
                      }
                    );

              buffer = board.registerObjective(OBJECTIVE_TWO.getPhrase() + player.getNumber(), new ScoreboardBaseCriteria(DUMMY.getPhrase()));
              buffer.setDisplayName(TITLE.getPhrase());
        }
        scores.forEach((String, ScoreSet) ->
                ScoreSet.recalculate()

    );

    }


    //unregister the board for the player
    public void unregister()
    {

        if (isShowingPrimary) {
            try {
                board.unregisterObjective(primary);
                board.handleObjectiveRemoved(primary);
                send(new PacketPlayOutScoreboardObjective(primary, 1));
            } catch (IllegalArgumentException | IllegalStateException ignored) {

            }
        }
        else {

            try {
                board.unregisterObjective(buffer);
                board.handleObjectiveRemoved(buffer);
                send(new PacketPlayOutScoreboardObjective(buffer, 1));
            } catch (IllegalArgumentException | IllegalStateException ignored) {

            }
        }
    }

    //Unregisters everything regardless of if they exist or not.
    // this may throw an exception on the player side.
    public void unregisterRegardless()
    {
        try {
            board.unregisterObjective(primary);
            board.handleObjectiveRemoved(primary);
            send(new PacketPlayOutScoreboardObjective(primary,1));

        }
        catch (IllegalArgumentException | IllegalStateException ignored)
        {

        }

        try {
            board.unregisterObjective(buffer);
            board.handleObjectiveRemoved(buffer);
            send(new PacketPlayOutScoreboardObjective(buffer,1));
        }
        catch (IllegalArgumentException | IllegalStateException ignored)
        {

        }
    }


    //Changes the ids of 2 scoresets.
    //oldIdentifier --> Original Scoreset 1 id.
    //oldIdentifierChange --> New scoreset 1 id.


    //Change the old id to the oldIdChange
    public synchronized void interchangeIdentifiers(String oldSetIdentifier, String oldSetNewIdentifier, String setToTakeOld)
    {
       if (scores.containsKey(oldSetIdentifier)&&scores.containsKey(setToTakeOld))
       {
           //getting the scores relevant to the old scores.
           ScoreSet oldChange = scores.get(oldSetIdentifier);
           ScoreSet newChange = scores.get(setToTakeOld);

           //removing them from the hashmap.
           scores.remove(oldSetIdentifier);
           scores.remove(setToTakeOld);

           //changing the identifiers to different ones.
           oldChange.setIdentifier(oldSetNewIdentifier);
           newChange.setIdentifier(oldSetIdentifier);

           //putting them back into the hashmap.
           scores.put(oldChange.getIdentifier(),oldChange);
           scores.put(newChange.getIdentifier(),newChange);
       }
    }


    //Setting the score name of a score
    public synchronized void setScoreName(String identifier, String newName)
    {
        if (scores.containsKey(identifier))
        {
            ScoreSet set = scores.get(identifier);
            set.setName(newName);
        }
    }


    //helper to send packets to the player. Packets are a generic type, so that's why there's a <?>
    private void send(Packet<?> packet)
    {
        ((CraftPlayer)player.getRawPlayer()).getHandle().playerConnection.sendPacket(packet);
    }

    //Helper to get the team status of a team
    private static String getTeamStatus(BattleTeam team)
    {
        try {
            String previousName;
            if (team.isEliminated())
                previousName = team.getDisplayScoreboardEntry() + " " + TEAM_DEAD.getPhrase();
            else if (!team.doesBedExist())
                previousName = team.getDisplayScoreboardEntry() + " " + team.getRemainingPlayers();
            else
                previousName = team.getDisplayScoreboardEntry() + " " + TEAM_ALIVE.getPhrase();

            return previousName;
        }
        catch (NullPointerException e)
        {
            return "Team DNE";
        }
    }

}
