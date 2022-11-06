package me.camm.productions.bedwars.Arena.GameRunning;

import me.camm.productions.bedwars.Arena.GameRunning.Events.ActionEvent;
import me.camm.productions.bedwars.Arena.GameRunning.Events.GameEndAction;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.Managers.PlayerTrackerManager;
import me.camm.productions.bedwars.Arena.Players.Scoreboards.PlayerBoard;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.InventoryOptionable;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Listeners.PacketHandler;
import me.camm.productions.bedwars.Entities.ShopKeeper;
import me.camm.productions.bedwars.Generators.Generator;
import me.camm.productions.bedwars.Listeners.*;
import me.camm.productions.bedwars.Arena.GameRunning.Events.EventTime;
import me.camm.productions.bedwars.Util.Locations.Boundaries.ExecutableBoundaryLoader;
import me.camm.productions.bedwars.Util.Helpers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.camm.productions.bedwars.Arena.Players.Scoreboards.ScoreBoardHeader.TIME;
import static me.camm.productions.bedwars.Arena.GameRunning.Events.EventTime.*;


/*
 * @author CAMM
 * This class is used to run the game. It takes care of the generators, and the player scoreboard updating as
 * well as registering other event handlers.
 */
public class GameRunner
{
    private final Plugin plugin;
    private final Arena arena;
    private final List<Generator> generators;

    private final int totalGameTime;
    private final int runnableFraction;


    private String playerHeader;
    private volatile int headerTime;
    private int currentGameTime;
    private double generatorSpinTime;
    private volatile boolean isRunning;

    private PacketHandler packetHandler;
    private final ArrayList<ShopKeeper> keepers;

    private boolean isInflated;


    /*
    These are the classes with the listeners.
     */
    private BlockInteractListener blockListener;
    private ItemUseListener itemListener;
    private ExplosionHandler explosionListener;
    private ItemListener droppedListener;
    private EntityActionListener.LocationManager npcManager;
    private MobSpawnListener mobSpawnListener;
    private EntityActionListener damageListener;
    private LogListener playerLogListener;
    private ProjectileListener projectileListener;


    private InventoryListener invListener;


    private ExecutableBoundaryLoader boundaryLoader;
    private PlayerTrackerManager trackerManager;



    private Collection<BattlePlayer> registered;
    private  final ArrayList<ActionEvent> eventList;
    private int nextActivationTime;

    private final ChatSender sender;


    //constructor
    public GameRunner(Plugin plugin, Arena arena)
    {
        this.plugin = plugin;
        this.arena = arena;
        sender = ChatSender.getInstance();
        registered = null;

        this.keepers = new ArrayList<>();


      this.isInflated = false;
      this.isRunning = false;

      //the period to run the generators and the timer
    this.runnableFraction = EventTime.RUNNABLE_PERIOD.getTime()/TICKS.getTime();


    this.currentGameTime = 0;
    this.generatorSpinTime = 0;


    this.totalGameTime = TOTAL_GAME_TIME.getTime();
    eventList = EventBuilder.build(this);
    this.playerHeader = null;
    int index = 0;



    while (playerHeader == null && index < eventList.size())
    {
        playerHeader = eventList.get(index).getHeader();
        index ++;
    }

    //although these are the same values initially, they are not always during the game.
    headerTime = eventList.get(0).getActivationTime();
    nextActivationTime = eventList.get(0).getActivationTime();



     generators = arena.getGenerators();

     //we add the log listener and inventory listeners here so the players can join the game.
        invListener = new InventoryListener(this);
        plugin.getServer().getPluginManager().registerEvents(invListener,plugin);

        playerLogListener = new LogListener(arena,this,keepers);
        plugin.getServer().getPluginManager().registerEvents(playerLogListener,plugin);


        addPermissions();


    }//constructor.


    /*
    @author CAMM
    This method prepares the game to start by performing post-constructor operations before
    the game actually starts.
     */
    public void prepareAndStart()
    {

        //the registered players won't change during the game since you cannot unregister/register,
        //so doing .values() to transfer the info to a collection is fine.

        registered = arena.getPlayers().values();
        this.isRunning = true;

        int maxPlayers = 0;

        Collection<BattleTeam> teams = arena.getTeams().values();


        //we use the loader to detect if the players are in areas like trap areas, heal pools, etc
        boundaryLoader = new ExecutableBoundaryLoader(arena);
        boundaryLoader.start();

        trackerManager = new PlayerTrackerManager();
        trackerManager.start();

        //        //init the npcs for buying, etc
        for (BattleTeam team: arena.getTeams().values()) {
            maxPlayers = Math.max(maxPlayers, team.getPlayers().size());
            team.initializeNPCs();
            team.showNPCs();
            keepers.add(team.getTeamQuickBuy());
            keepers.add(team.getTeamGroupBuy());

            team.initTrackingEntries(teams);
            team.startForge();
            team.setLoader(boundaryLoader);

            //if there are no players on there, then eliminate the team.
            if (team.getRemainingPlayers()==0) {
                team.eliminate();
                continue;
            }

            team.setTrackerManager(trackerManager);
        }


        // bedwars has different prices depending on game mode. We just use the player number on the teams to determine that.
        if (maxPlayers>2)
            isInflated = true;



        //adding the packet handler for the invisibility, etc
        this.packetHandler = new PacketHandler(keepers, arena);
        playerLogListener.initPacketHandler(packetHandler);




        //spawning the generators.
        for (Generator generator: generators) {
            generator.spawnIntoWorld();
            generator.setPlayerNumber(maxPlayers);
        }

        //updating the team statuses for the players after we decide which ones are eliminated, etc
        TeamHelper.updateTeamBoardStatus(registered);



        //Initiating the listeners
        initListeners();


        start();


    }

    public void initListeners(){

        droppedListener = new ItemListener(arena);
        mobSpawnListener = new MobSpawnListener();
        damageListener = new EntityActionListener(arena,plugin,this);
        blockListener = new BlockInteractListener(plugin, arena);
        explosionListener = new ExplosionHandler(plugin, arena,damageListener);
        itemListener = new ItemUseListener(plugin,arena,packetHandler,damageListener);
        projectileListener = new ProjectileListener(plugin, arena, damageListener);


        PluginManager manager = plugin.getServer().getPluginManager();
        Listener[] handlers = new Listener[]{droppedListener, mobSpawnListener, damageListener, blockListener, explosionListener, itemListener, projectileListener};
        for (Listener listener: handlers) {
            manager.registerEvents(listener, plugin);
        }


        for (BattlePlayer player: registered)
        {
            player.instantiateConfig(isInflated);
            if (!packetHandler.contains(player.getRawPlayer()))
                packetHandler.addPlayer(player.getRawPlayer());
        }


        npcManager = new EntityActionListener.LocationManager(plugin,arena,keepers,packetHandler,this);
        Thread thread = new Thread(npcManager);
        thread.start();
    }


    public void unregisterPlayer(Player p) {

        if (arena!=null){
            arena.getTeams().values().forEach((team) -> team.removePlayer(p));


            BattlePlayer unregistered = arena.getPlayers().getOrDefault(p.getUniqueId(),null);
            if (unregistered != null) {
                arena.unregisterPlayer(p.getUniqueId());
            }
        }

        PacketHandler handler = getPacketHandler();
        if (handler != null) {
            handler.removePlayer(p);
        }
    }




    /*
    @Author CAMM
    This method starts and runs the game for the game's duration, or until a team wins.
     */
    private void start()
    {

        sender.sendMessage("The game is starting!");

        for (BattleTeam team: arena.getTeams().values())
            team.readyPlayers();

        //bukkit runnable to run the game at a constant pace
        new BukkitRunnable()
        {
            int fraction = 0;

            public void run()
            {
                if (!isRunning) {
                    cancel();
                    return;
                }
                generatorSpinTime += 0.1;
                for (Generator generator: generators)
                {
                    generator.setTimeCount(generator.getTimeCount()+1);
                    generator.setRotation(generatorSpinTime);

                    //The time count is a count in a fraction of a second. (n/1 second).
                    //We use this time to see if we should update the timer title armorstand on the generator,
                    //and see if we should spawn the product.
                    if (!(generator.getTimeCount()>=runnableFraction))
                        continue;

                    if (generator.updateSpawnTime()) //if the generator should spawn an item
                        generator.spawnItem();

                    generator.setTimeCount(0);
                    generator.setTimeTitle(generator.getNextSpawnTime());
                }//for

                // We increase the fraction. (Fraction of n/1 second) When it is equal to the whole, we increase the
                //player timer. (The fraction is for the generators since they don't rotate once per second.)
                fraction++;
                if (fraction>=runnableFraction)
                {
                    fraction = 0;
                    advancePlayerTimer();  //advancing the player scoreboard
                }
            }
        }.runTaskTimer(plugin,0,TICKS.getTime());  //1 second = 20 ticks. Bukkit runnables run the
                                                        //task in ticks.
    }



    /*
    @author CAMM
    Advances the player's timer on their scoreboards, and checks for if a game
    event should be triggered.
    This method also refreshes the player's scoreboards.
     */
    private synchronized void advancePlayerTimer()
    {
        if (this.currentGameTime<totalGameTime)
        {
            currentGameTime++;
           checkForEvents(currentGameTime);


         registered.forEach((player) -> {
             PlayerBoard board = player.getBoard();

             //pretty sure we need this check since they may relog, in which case the board may be null
             if (board!=null) {
                 board.setScoreName(TIME.getPhrase(), getTimeFormatted());
                 board.switchPrimaryBuffer();
                 player.updatePlayerStatistics();

                 ///might cause a bit of packet lag.
                 player.sendHealthUpdatePackets();
             }

         });
        }
    }

    /*
@Author CAMM
Initializes the time amount on the player scoreboards to a number, instead of being blank.
E.g) "Diamond II in 1:00" instead of "Diamond II in"
*/
    public void initializeTimeBoardHead(BattlePlayer player)
    {
        player.getBoard().setScoreName(TIME.getPhrase(), getTimeFormatted());
    }


/*
@author CAMM
@return A string displaying the header and time for the players. E.g "Diamond II in 1:00"
This method gets the time and header to be displayed on the player's scoreboards
as a string.
 */
    private synchronized String getTimeFormatted()
    {
        //Current event time is the time period before the next event.
        //The current event time should always be greater or equal to the current game time.
        int remainingTime = headerTime-currentGameTime;


        //If the time remainder is less than 10, then add a 0 to the start.
        String adder = remainingTime% TIME_IN_MINUTE.getTime()<10 ? "0": "";


        //Formatting the time for the player scoreboard.
        String time = (remainingTime/TIME_IN_MINUTE.getTime())+":"+adder+(remainingTime%TIME_IN_MINUTE.getTime());
        return playerHeader+time;
    }


    /*
     * @author CAMM
     * Used to add permissions to the players.
     */
    private void addPermissions(){

        for (Player player: Bukkit.getOnlinePlayers()) {
            playerLogListener.addPerms(player);
        }
    }



   /*
   @author CAMM
   @param time The value to check against when determining if an event should be executed. Positive int.
   This method checks for if an event should be executed by comparing a given time with the current
   event to launch.
    */
    private synchronized void checkForEvents(int time) {

        //if the thing is empty, then end the game
        if (eventList.size() == 0)
        {
            ActionEvent ending = new ActionEvent(0, new GameEndAction(this));
            ending.activateEvent();
            return;
        }

            ActionEvent nextEvent = eventList.get(0);

        //check the time
            if (time >= nextActivationTime)
            {
               nextEvent.activateEvent();

               if (eventList.size()>1) {
                   eventList.remove(0);
               }
               else
                   return;

               this.playerHeader = null;

                nextActivationTime = eventList.get(0).getActivationTime();

                //getting the next header
               int index = 0;
               while (playerHeader == null && index < eventList.size())
               {
                   playerHeader = eventList.get(index).getHeader();
                   headerTime = eventList.get(index).getActivationTime();
                   index ++;
               }

               //updating the board status
               TeamHelper.updateTeamBoardStatus(registered);
            }
    }


    /*
    Attempts to end the game by seeing if there is a team remaining
    does not end the game if there is a tie condition
     */
    public void attemptEndGame(){



        if (!isRunning)
            return;

        BattleTeam candidate = TeamHelper.isVictorFound(arena.getTeams().values());
                if (candidate!=null)
                    this.endGame(candidate);
        TeamHelper.updateTeamBoardStatus(registered);
    }

    /*
    @author CAMM
    @param candidate If the candidate is null, invokes a tie sequence. If not, the given team is the
    winner.
    Ends the game, with a different outcome depending on the candidate given.
     */
    public synchronized void endGame(@Nullable BattleTeam candidate)
    {

        Collection<BattleTeam> teams = arena.getTeams().values();

        for (Entity entity: arena.getWorld().getEntities()) {
            if ((entity.getType() != EntityType.PLAYER))
                entity.remove();

        }

        setRunning(false);
        npcManager.setRunning(false);
        boundaryLoader.stop();
        trackerManager.setRunning(false);

        for (BattleTeam team : teams) {
            team.getForge().disableForge();
            team.getTeamQuickBuy().removeNPC();
            team.getTeamGroupBuy().removeNPC();

        }


        if (candidate!=null) {
            sender.sendMessage(ChatColor.GOLD + "All other teams have been eliminated!");
            sender.sendMessage(candidate.getTeamColor().getChatColor() + candidate.getCapitalizedColor() + " team has won the game!");
        }
        else
        {
            sender.sendMessage(ChatColor.YELLOW+"Tie detected between the following teams:");
            for (BattleTeam team : teams) {
                if (!team.isEliminated())
                    sender.sendMessage(team.getTeamColor().getChatColor()+"- "+team.getCapitalizedColor());
            }
        }

        HandlerList.unregisterAll(plugin);
        arena.unregisterMap();

        for (BattlePlayer player: arena.getPlayers().values())
        {

            player.teleport(arena.getSpecSpawn());
            Player raw = player.getRawPlayer();
            raw.setAllowFlight(true);
            raw.setFlying(true);
            packetHandler.removePlayer(raw);
            player.removeInvisibilityEffect();




            for (Player possiblyHidden: Bukkit.getOnlinePlayers()) {
                player.getRawPlayer().showPlayer(possiblyHidden);
            }
        }


    }



    //Getter and Setters
    ///////////////////////////////////////

    public void setRunning(boolean isRunning)
    {
        this.isRunning = isRunning;
    }

    public PacketHandler getPacketHandler()
    {
        return packetHandler;
    }

    public boolean isRunning()
    {
        return this.isRunning;
    }


    public Arena getArena(){
        return arena;
    }

    public ExecutableBoundaryLoader getLoader(){
        return boundaryLoader;
    }

    public EntityActionListener getDamageListener(){
        return damageListener;
    }

    public boolean isInflated() {
        return isInflated;
    }


    public IGameInventory getJoinInventory(){
        return invListener.getJoinInventory();
    }


}
