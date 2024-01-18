package me.camm.productions.bedwars.Game.Scoreboard;


import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;


import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

/**
 * @author CAMM
 * Class for the scoreboard of the players
 */
public class PlayerBoard
{
    private final BattlePlayer player;
    private final Arena arena;
    private final HashMap<String, ScoreEntry> scores;
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

        this.primary = board.registerObjective(ScoreBoardHeader.PRIMARY_OBJECTIVE.getPhrase()+player.getNumber(),new ScoreboardBaseCriteria(ScoreBoardHeader.DUMMY.getPhrase()));
        this.primary.setDisplayName(ScoreBoardHeader.TITLE.getPhrase());

       this.buffer = board.registerObjective(ScoreBoardHeader.BUFFER_OBJECTIVE.getPhrase()+player.getNumber(), new ScoreboardBaseCriteria(ScoreBoardHeader.DUMMY.getPhrase()));
       this.buffer.setDisplayName(ScoreBoardHeader.TITLE.getPhrase());


         //0 means create
        send(new PacketPlayOutScoreboardObjective(primary, 0));

        send(new PacketPlayOutScoreboardDisplayObjective(1,primary));  //show the primary

        Collection<BattleTeam> teams = arena.getTeams().values();
        int selectedScore = teams.size()+7; // 7 to account for the other scores.


        scores.put(ScoreBoardHeader.TIME.getPhrase(), new ScoreEntry(board,selectedScore, ScoreBoardHeader.TIME.getPhrase(), ScoreBoardHeader.DIAMOND_TWO_HEADER.getPhrase(), player, primary, buffer));
        scores.get(ScoreBoardHeader.TIME.getPhrase()).sendPrimary();  //time
        selectedScore--;

        scores.put(ScoreBoardHeader.B_ONE.getPhrase(), new ScoreEntry(board,selectedScore, ScoreBoardHeader.TIME.getPhrase(), ScoreBoardHeader.B_ONE.getPhrase(), player, primary, buffer));  //blank space
        scores.get(ScoreBoardHeader.B_ONE.getPhrase()).sendPrimary();
        selectedScore--;


        //for the amount of teams in the arena
        for (BattleTeam team : teams) {
            String currentTeamScore = getTeamStatus(team);
            String identifier;

            if (player.getTeam().getTeamColor().getName().equalsIgnoreCase(team.getTeamColor().getName()))  //if the player is on the same team
            {
                identifier = ScoreBoardHeader.CURRENT_TEAM.getPhrase();
                currentTeamScore += ScoreBoardHeader.CURRENT_TEAM.getPhrase();
            } else {
                identifier = team.getTeamColor().getName();
            }

            ScoreEntry current = new ScoreEntry(board, selectedScore, identifier, currentTeamScore, player, primary, buffer);
            current.sendPrimary();
            scores.put(current.getIdentifier(), current);
            selectedScore--;
        }

        //blank score for formatting
        scores.put(ScoreBoardHeader.B_TWO.getPhrase(), new ScoreEntry(board, selectedScore, ScoreBoardHeader.B_TWO.getPhrase(), ScoreBoardHeader.B_TWO.getPhrase(),player,primary,buffer));  //blank space
        scores.get(ScoreBoardHeader.B_TWO.getPhrase()).sendPrimary();
        selectedScore--;


        ////kills, finals, beds
        scores.put(ScoreBoardHeader.KILLS.getPhrase(), new ScoreEntry(board, selectedScore, ScoreBoardHeader.KILLS.getPhrase(), ScoreBoardHeader.KILLS.getPhrase()+player.getKills(), player, primary, buffer));
        scores.get(ScoreBoardHeader.KILLS.getPhrase()).sendPrimary();
        selectedScore--;

        scores.put(ScoreBoardHeader.FINALS.getPhrase(), new ScoreEntry(board, selectedScore, ScoreBoardHeader.FINALS.getPhrase(), ScoreBoardHeader.FINALS.getPhrase()+player.getFinals(), player, primary, buffer));
        scores.get(ScoreBoardHeader.FINALS.getPhrase()).sendPrimary();
        selectedScore--;

        scores.put(ScoreBoardHeader.BEDS.getPhrase(), new ScoreEntry(board, selectedScore, ScoreBoardHeader.BEDS.getPhrase(), ScoreBoardHeader.BEDS.getPhrase()+player.getBeds(), player, primary, buffer));
        scores.get(ScoreBoardHeader.BEDS.getPhrase()).sendPrimary();
        selectedScore--;

        scores.put(ScoreBoardHeader.B_THREE.getPhrase(), new ScoreEntry(board, selectedScore, ScoreBoardHeader.B_THREE.getPhrase(), ScoreBoardHeader.B_THREE.getPhrase(),player, primary, buffer));
        scores.get(ScoreBoardHeader.B_THREE.getPhrase()).sendPrimary();
        selectedScore--;

        //easter egg
        scores.put(ScoreBoardHeader.SPACE_CREDIT.getPhrase(), new ScoreEntry(board,selectedScore, ScoreBoardHeader.SPACE_CREDIT.getPhrase(),easterEgg,player,primary,buffer));
        scores.get(ScoreBoardHeader.SPACE_CREDIT.getPhrase()).sendPrimary();

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
              setScoreName(ScoreBoardHeader.CURRENT_TEAM.getPhrase(),getTeamStatus(team)+ ScoreBoardHeader.CURRENT_TEAM.getPhrase());
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


            scores.forEach((String, scoreEntry) ->
            {
              scoreEntry.recalculate();
                scoreEntry.sendBuffer();
            }
        );
            board.unregisterObjective(primary);



            if (isInitialized) {
                primary = board.registerObjective(ScoreBoardHeader.PRIMARY_OBJECTIVE.getPhrase() + player.getNumber(), new ScoreboardBaseCriteria(ScoreBoardHeader.DUMMY.getPhrase()));
                primary.setDisplayName(ScoreBoardHeader.TITLE.getPhrase());
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

            scores.forEach((String, scoreEntry) ->
                      {
                        scoreEntry.recalculate();
                        scoreEntry.sendPrimary();
                      }
                    );

              buffer = board.registerObjective(ScoreBoardHeader.BUFFER_OBJECTIVE.getPhrase() + player.getNumber(), new ScoreboardBaseCriteria(ScoreBoardHeader.DUMMY.getPhrase()));
              buffer.setDisplayName(ScoreBoardHeader.TITLE.getPhrase());
        }
        scores.forEach((String, scoreEntry) ->
                scoreEntry.recalculate()

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
           ScoreEntry oldChange = scores.get(oldSetIdentifier);
           ScoreEntry newChange = scores.get(setToTakeOld);

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
            ScoreEntry set = scores.get(identifier);
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
                previousName = team.getDisplayScoreboardEntry() + " " + ScoreBoardHeader.TEAM_DEAD.getPhrase();
            else if (!team.getBedExists())
                previousName = team.getDisplayScoreboardEntry() + " " + team.getRemainingPlayers();
            else
                previousName = team.getDisplayScoreboardEntry() + " " + ScoreBoardHeader.TEAM_ALIVE.getPhrase();

            return previousName;
        }
        catch (NullPointerException e)
        {
            return "Team DNE";
        }
    }

}
