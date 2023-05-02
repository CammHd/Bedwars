package me.camm.productions.bedwars.Arena.Game;

import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Generators.Generator;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.ActionSelectionInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.QuickChatInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;

import me.camm.productions.bedwars.Util.PacketSound;
import me.camm.productions.bedwars.Validation.BedWarsException;
import me.camm.productions.bedwars.Validation.CommandException;
import me.camm.productions.bedwars.Validation.RegistrationException;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.camm.productions.bedwars.Arena.Players.Scoreboards.ScoreBoardHeader.*;


/**
 * @author CAMM
 * Models an arena for the game to run in
 */
public class Arena
{
  private final GameBoundary bounds;
  //the bounds of the arena

  private final int voidLevel;

  private final ConcurrentHashMap<String, BattleTeam> teams;
  //the string is the team color

  private List<Generator> generators;
  private final World world;

  private final Plugin plugin;

  //location of the spectator spawn
  private final Location specSpawn;

  //we're using this to keep track of players
  private static int playerAssignment;


  private volatile boolean settingUp;
  boolean spent;



  //objectives for the health above the name tags, and in the tablist
  private final Scoreboard healthBoard;
  private final Objective nameHealth;
  private final Objective tabHealth;


  private final ChatSender sender;

    //we can't have a single scoreboard for each player because each board is different for each player, but we can have one
    //single board for displaying the health.


    //registered players
    private final Map<UUID, BattlePlayer> players;


    private IGameInventory chatInv;
    private IGameInventory selectionInv;



    /*
     *
     * Constructor.
     *
     * @param bounds bounds of the map
     * @param spectatorSpawn where the spectators spawn
     * @param voidLevel y level for the void
     * @param world world the match will take place in
     * @param plugin owning plugin
     */
  public Arena(GameBoundary bounds, Coordinate spectatorSpawn, int voidLevel, World world, Plugin plugin)
  {


      chatInv = new QuickChatInventory(this);
      selectionInv = new ActionSelectionInventory(this);

      sender = ChatSender.getInstance();
      settingUp = false;
      spent = false;
      playerAssignment = 0;
      this.bounds = bounds;
      this.voidLevel = voidLevel;

      this.world = world;
      this.plugin = plugin;

      this.players = new ConcurrentHashMap<>();
      this.teams = new ConcurrentHashMap<>();

      //converting to a location
          specSpawn = spectatorSpawn.getAsLocation(world);


          //initializing the scoreboards
          healthBoard = Bukkit.getScoreboardManager().getNewScoreboard();

         nameHealth = healthBoard.registerNewObjective(HEALTH_CATEGORY.getPhrase(),HEALTH_CRITERIA.getPhrase());
         nameHealth.setDisplayName(HEART.getPhrase());
         nameHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);

      tabHealth = healthBoard.registerNewObjective(HEALTH_CATEGORY_TWO.getPhrase(),HEALTH_CRITERIA.getPhrase());
      tabHealth.setDisplayName(HEALTH_CATEGORY.getPhrase());
      tabHealth.setDisplaySlot(DisplaySlot.PLAYER_LIST);

//boards must have at least 1 score to display properly, so we put in a placeholder
     Score initOne = nameHealth.getScore(INITIALIZER_ONE.getPhrase());
      initOne.setScore(1);
      Score initTwo = tabHealth.getScore(INITIALIZER_TWO.getPhrase());
      initTwo.setScore(1);

  }


  //removes the player from the game (includes the scoreboard and the teams).
    //You don't need to do anything else to remove the player.
    public void unregisterPlayer(UUID uuid)
    {
        if (players.containsKey(uuid))
        {
            BattlePlayer player = players.get(uuid);
            player.getBoard().unregister();
            player.getTeam().removePlayer(player.getRawPlayer());
            players.remove(uuid);

            player.unregister(healthBoard);

        }
    }

    public IGameInventory getChatInv(){
      return chatInv;
    }

    public IGameInventory getSelectionInv(){
      return selectionInv;
    }

    //adds a player to the arena. DOES NOT register them. You need to do that separately.
    public synchronized void addPlayer(UUID uuid, BattlePlayer player)
    {
        players.put(uuid, player);
    }

    //adds teams to the arena. They are added separately since the config info is in separate files.
  public void addTeams(@NotNull ArrayList<BattleTeam> list)
  {
      for (BattleTeam team: list)
      {
          if (team!=null)
              this.teams.put(team.getTeamColor().getName(),team);
      }
  }


  //gets the teams as an arraylist.
    //use  new ArrayList<>(arena.getTeams().values()) instead or
    //simply getTeams().values() for a collection if order doesn't matter.
  @Deprecated
  public ArrayList<BattleTeam> getTeamList()
  {
      ArrayList<BattleTeam> list = new ArrayList<>();
      teams.forEach((String, BattleTeam)->
              list.add(BattleTeam));
      return list;
  }


  //gets a player number for a player
  public synchronized int assignPlayerNumber()
  {
      playerAssignment++;
      return playerAssignment;
  }

  //registers the map
  public void registerMap() throws BedWarsException
  {
      if (isRegistering())
          throw new CommandException("Concurrent request for set up. The arena is already doing so!");

      final long time = System.currentTimeMillis();

      setRegistering(true);

      for (Generator generator: generators)
          generator.registerBox();

      setRegistering(false);


      sender.sendMessage("Registered map zones! ("+(System.currentTimeMillis() - time)+" ms)");



  }


  public synchronized boolean isRegistering(){
      return settingUp;
  }

  public synchronized void setRegistering(boolean setting){
      settingUp = setting;
  }

  public void unregisterMap(){

     for (BattleTeam team:  teams.values()) {
         team.unregisterBase();
         team.empty();
     }
     spent = true;

  }

  public boolean isSpent(){
      return spent;
  }

  //registers the zones for the teams
  public void registerTeamZones()
  {
      long time = System.currentTimeMillis();
      teams.forEach((String,team) -> team.registerBase());
      sender.sendMessage("Registered Team zones! ("+(System.currentTimeMillis() - time)+" ms)");

  }

  public int[] getTeamColorsAsInt()
  {
      ArrayList<Integer> values = new ArrayList<>();

      for (BattleTeam battleTeam : teams.values()) {
          values.add(battleTeam.getTeamColor().getValue());
      }

      int[] arrayValues = new int[values.size()];  //transferring to int array

      for (int slot=0;slot<values.size();slot++)
          arrayValues[slot] = values.get(slot);

      return arrayValues;
  }


    /*
       sends a sound to players in a given area around an origin
       we square the distance to save computation time (sqrt() is somewhat of an inefficient operation)
     */
  public void sendLocalizedSound(PacketSound sound,Location origin, double distance){

      distance *= distance;
      for (BattlePlayer player: players.values())
      {
          if (origin.distanceSquared(player.getRawPlayer().getLocation()) < distance)
            player.playSound(sound);
      }
  }




  //getters
    public Scoreboard getHealthBoard()
    {
        return healthBoard;
    }

    public Map<String, BattleTeam> getTeams()
    {
        return teams;
    }

    public List<Generator> getGenerators()
    {
        return generators;
    }

    public void setGenerators(List<Generator> generators)
    {
        this.generators = generators;
    }

    public World getWorld()
    {
        return world;
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public Map<UUID, BattlePlayer> getPlayers()
    {
        return players;
    }

    public int getVoidLevel()
    {
        return this.voidLevel;
    }

    public Location getSpecSpawn()
    {
        return specSpawn;
    }

    public GameBoundary getBounds(){
      return bounds;
    }
}
