package me.camm.productions.bedwars.Arena.Players;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.Managers.HotbarManager;
import me.camm.productions.bedwars.Arena.Players.Managers.PlayerInventoryManager;
import me.camm.productions.bedwars.Arena.Players.Scoreboards.PlayerBoard;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.TeamTitle;
import me.camm.productions.bedwars.Files.FileManager;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.ChatOptionSelectionInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.QuickBuyEditorInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Listeners.PacketHandler;
import me.camm.productions.bedwars.Entities.ShopKeeper;
import me.camm.productions.bedwars.Files.PlayerFileReader;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Items.ItemDatabases.TieredItem;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import me.camm.productions.bedwars.Util.Helpers.PlayerHelper;
import me.camm.productions.bedwars.Util.PacketSound;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static me.camm.productions.bedwars.Arena.Players.Scoreboards.ScoreBoardHeader.*;
import static me.camm.productions.bedwars.Items.ItemDatabases.ShopItem.TRACKER_ITEM;


/*

 @author CAMM
 This is a wrapper class for a player that provides added information about
 a registered player
 */
public class BattlePlayer
{

    private final Arena arena;
    private volatile Player player;




    private BattleTeam tracking;

    //system time of when they last drank milk
    private volatile long lastMilk;

    //boolean values to determine if the player is eliminated or alive.
    //The alive value is for when they are in spectator mode and about to respawn.
    private boolean isEliminated;
    private volatile boolean isAlive;


    //The time the player has until next respawn.
    //Used by a player death counter and also for when bed destroyed while counting down.
    //we use the int for a significant subtitle.
    //E.g bed destroyed while respawn message is being played.
    //The significant title is only used for the bed and respawn messages, not for other messages  (E.g dragon spawn message)
    //we use it since if we don't, there will be a cutoff of what is being shown (nothing will be shown instead)


    //countdown time for when they respawn
    private volatile int timeTillRespawn;



    //not a battle team. This team is for their name color and visibility
    //to other players
    private Team playerTeam;


    //Managers for their hotbar and their quickbuy
    private HotbarManager barManager;
    private PlayerInventoryManager shopManager;
    private ChatOptionSelectionInventory chatOptionInv;


    //Shopkeepers to send to them.
    private final HashMap<Integer, ShopKeeper> toResend;

    //Board object for their scores.
    private PlayerBoard board;

    //The team they belong to.
    private BattleTeam team;

    //Int values for their kills, finals, and beds broken.
    private volatile int finals;
    private volatile int kills;
    private volatile int beds;



    //The possible persistent items a player can have.
    private boolean hasShears;

    private TieredItem pick;
    private TieredItem axe;
    private TieredItem armor;


    //editor for the quickbuy
    private QuickBuyEditorInventory quickEditor;
    private static final int eliminationTime;


    private final Map<Integer, IGameInventory> accessibleInventories;


    private static int scoreboardId = 0;
    private final int playerScoreboardId;






    static {
        eliminationTime = 5;
    }




    //constructor
    public BattlePlayer(Player player, BattleTeam team, Arena arena) throws IOException
    {

        this.arena = arena;
        this.team = team;
        this.tracking = null;

        this.playerScoreboardId = scoreboardId ++;
        this.player = player;

        this.isEliminated = false;
        this.timeTillRespawn = 0;
        this.isAlive = true;

        this.finals = 0;
        this.kills = 0;
        this.beds = 0;

        this.barManager = null;
        this.shopManager = null;


        pick = null;
        axe = null;
        hasShears = false;
        armor = TieredItem.LEATHER_ARMOR;

        this.toResend = new HashMap<>();
        this.accessibleInventories = new HashMap<>();

        //create the scoreboard
        createBoard();

        //create the config files if they don't exist
        FileManager.getPlayerFiles(this.player);

        this.lastMilk = 0;

    }


    /*
    Registers a player onto the healthboard and their team (not battleteam)
     */
    @SuppressWarnings("deprecation")
    public void register()
    {
        Scoreboard healthBoard = arena.getHealthBoard();

        //unregister them first in the case that they've been registered beforehand
        unregister(healthBoard);

        //registering a separate team for them.
        playerTeam = healthBoard.registerNewTeam(team.getTeamColor().getName()+ playerScoreboardId);
        playerTeam.setPrefix(team.getTeamPrefix());
        playerTeam.setSuffix(team.getTeamPostfix());
        player.setScoreboard(healthBoard);
        playerTeam.addPlayer(player);
    }

    public void setTracking(BattleTeam tracking) {
        this.tracking = tracking;
    }

    public BattleTeam getTracking() {
        return tracking;
    }

    public Map<Integer, IGameInventory> getAccessibleInventories(){
        return accessibleInventories;
    }


    public void addAccessibleInventory(IGameInventory inventory){
        accessibleInventories.put(inventory.hashCode(), inventory);
    }


    //Tries to unregister them from the health board. removePlayer() is deprecated. Can't really
    //do anything about that.
    @SuppressWarnings("deprecation")
    public void unregister(Scoreboard healthBoard)
    {
        try {
            playerTeam.removePlayer(player);
            healthBoard.getTeam(team.getTeamColor().getName() + playerScoreboardId).unregister();
        }
        catch (NullPointerException | IllegalStateException | IllegalArgumentException ignored)
        {

        }
    }

    //removes the invis effect from the player
    public void removeInvisibilityEffect() {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }



    /*

    @param newPlayer: Takes in a new player object to replace the old one.

    Used to replace the player object and reset the scoreboard when the player rejoins into the game
    if they were previously registered.

    Every time a player leaves and joins, it's not the same player object that was previously referenced.
    Also the reason why every listener here must get the player list from the arena (centralized info area)
    and not keep a copy of raw players.

     */
    public synchronized void refactorPlayer(Player newPlayer)
    {
        this.player = newPlayer;
        board.unregisterRegardless();
        createBoard();
    }

    /*

    Gets information on the player's configuration for their hotbar and quickbuy files.
     */
    public void instantiateConfig(boolean isInflated)
    {
        PlayerFileReader reader = new PlayerFileReader(arena.getPlugin(),this,isInflated, arena);
        this.barManager = reader.readBarFile();
        this.shopManager = reader.readInvFile();


        Arrays.stream(shopManager.getShopInventories()).forEach(inv -> accessibleInventories.put(inv.hashCode(), inv));

         quickEditor = new QuickBuyEditorInventory(this);
         chatOptionInv = new ChatOptionSelectionInventory(arena, this);

         addAccessibleInventory(quickEditor);
         addAccessibleInventory(barManager.getEditor());
         addAccessibleInventory(team.getTeamInventory());
         addAccessibleInventory(team.getTrackerInventory());
         addAccessibleInventory(arena.getChatInv());
         addAccessibleInventory(arena.getSelectionInv());
         addAccessibleInventory(chatOptionInv);

    }

    /*

   Toggles whether the player should be invisible to other teams. Used when
    the player drinks an invisibility potion.
    does not add or remove the invis effect
     */
    public synchronized void togglePotionInvisibility(boolean isInvisible, PacketHandler handler)
    {
        if (isInvisible)
        {
            hideArmor();
            handler.addInvisiblePlayer(this.player);
            if (playerTeam!=null)
             playerTeam.setNameTagVisibility(NameTagVisibility.NEVER);
        }
        else
        {
            handler.removeInvisiblePlayer(this.player);
            if (playerTeam!=null)
                playerTeam.setNameTagVisibility(NameTagVisibility.ALWAYS);
          sendArmorUpdate();
        }
    }

    /*

    Updates the scoreboard and armor stuff on the player's side, but does not account for the invisibility through
    the packet handler.

     */
    public synchronized void removeUnprocessedInvisibility()
    {
            if (playerTeam!=null)
                playerTeam.setNameTagVisibility(NameTagVisibility.ALWAYS);

            sendArmorUpdate();
    }


    /*

    Gets the player's armor as nms items and returns them in an Item stack array.
     */
    public synchronized net.minecraft.server.v1_8_R3.ItemStack[] getNMSArmor()
    {
        net.minecraft.server.v1_8_R3.ItemStack[] items = new ItemStack[4];
        PlayerInventory inv = player.getInventory();
        items[3] = ItemHelper.toNMSItem(inv.getHelmet());
        items[2] = ItemHelper.toNMSItem(inv.getChestplate());
        items[1] = ItemHelper.toNMSItem(inv.getLeggings());
        items[0] = ItemHelper.toNMSItem(inv.getBoots());
        return items;
    }


   //Equips the current armor registered onto the player.
    public void equipArmor()
    {
      ItemHelper.setArmor(ItemHelper.inventoryItemToArmor(getArmor().getItem(),this),getRawPlayer());
    }


    /*

    Sends a packet to all players on opposing teams.
     */
    public void sendOppositionPackets(Packet<?> packet)
    {
        arena.getPlayers().forEach((uuid, battlePlayer) -> {
            if (!battlePlayer.getTeam().equals(this.team))
                battlePlayer.sendPacket(packet);
        });
    }

    //Send packets to all players which are not this player
    public void sendPacketsAllNonEqual(Packet<?> packet) {
        arena.getPlayers().forEach((uuid,battlePlayer) -> {
            if (!battlePlayer.equals(this)) {
                battlePlayer.sendPacket(packet);
            }
        });
    }


    /*

    Hides the player's armor by sending packets to the opposition.
     */
    public void hideArmor()
    {
        int id = this.player.getEntityId();
        int slot = 1;

        while (slot<5)
        {
            PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(id,slot,null);
            sendOppositionPackets(equipment);
            slot++;
        }
    }



    /*

    Sends packets to all players except this one regarding this player's armor (Updating their information)
     */
    public void sendArmorUpdate()
    {
        //getting this player's armor
       final net.minecraft.server.v1_8_R3.ItemStack[] items = getNMSArmor();
       final int id = this.player.getEntityId();

        arena.getPlayers().forEach((uuid, battlePlayer) -> {
            if (!battlePlayer.equals(this))
            {
                //slot goes from boots to helmet (1-4)
                int slot = 0;

                for (ItemStack stack: items)
                    battlePlayer.sendPacket(new PacketPlayOutEntityEquipment(id,++slot,stack));

            }
        });
    }




    /*

    Puts the player onto a different score on the packet scoreboard and refreshes their board.
    Problems with player numbers not showing up correctly should not be an issue here.
    Make sure to reflect the change on the scoreboards of the other players after changing teams since
    this method does not update scoreboards for other players.
     */
    public boolean changeTeam(BattleTeam newTeam)
    {

       if (!this.team.equals(newTeam)&&!newTeam.isEliminated())
       {
           board.setScoreName(CURRENT_TEAM.getPhrase(), PlayerHelper.getTeamStatus(this.team));
           //sets the score with the "you" to the default score name regarding a team.

           board.setScoreName(newTeam.getTeamColor().getName(), PlayerHelper.getTeamStatus(newTeam) + CURRENT_TEAM.getPhrase());
           //Sets the default score to one with the "you"

           board.interchangeIdentifiers(CURRENT_TEAM.getPhrase(), this.team.getTeamColor().getName(), newTeam.getTeamColor().getName());
            //Interchanging identifiers. This makes it so that the scores keep the same positions.

           //removing the player from their previous team.
           this.team.removePlayer(this.player);

           //setting their team to the team they switched to.
           this.team = newTeam;

           //Adding the player to the new team.
           this.team.addPlayer(this);

           player.setScoreboard(arena.getHealthBoard());
           board.switchPrimaryBuffer();
           return true;
       }

       else
       {
           player.sendMessage(ChatColor.RED+"That Team is Invalid!");
           return false;
       }
    }

   /*

    Sends a title and a subtitle to a player, with specified time for fading and stay.
    The string given must be in Minecraft's chat component format used like in command blocks.
    E.g  "{\"text\":\"abc\"}"


    */
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut)
    {
        try {
            IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(title);
            PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, component);
            PacketPlayOutTitle subPacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(subTitle));
            PacketPlayOutTitle titleLength = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
            sendPacket(titlePacket);
            sendPacket(titleLength);
            sendPacket(subPacket);
        }
        catch (Exception ignored)
        {

        }
    }


    public void sendActionbarTitle(String title) {
        try {
            IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(title);
            PacketPlayOutChat chat = new PacketPlayOutChat(component, (byte)2);
            sendPacket(chat);

        }
        catch (Exception ignored) {

        }
    }


    /*
    Use this method to send titles to the player involving respawn messages and time left before they respawn.
     */
    public void sendRespawnTitle(TeamTitle title, TeamTitle subTitle, int secondsRemaining, int fadeIn, int stay, int fadeOut)
    {
        String respawn = PlayerHelper.addRespawnNumber(subTitle,secondsRemaining);
        sendTitle(title.getMessage(), respawn, fadeIn, stay, fadeOut);
    }




    /*
    Unfinished. Need to account for the packet handler.
    Also, when they come out of spec, hide all spec players from them, and allow all alive players to see them.
    Also remember to account for potion effects and items, as well as nametag visibility.

    Maybe use:
    public void toggleSpectator(boolean isSpectator, boolean isFinal, PacketHandler handler)

    Refactor to be private.

    Update: did the above.


    Toggles a player's spectator mode.
    Does not teleport the player anywhere
    Does not account for armor or persistent items.
     */
    private void toggleSpectator(boolean isSpectator, PacketHandler handler)
    {
        togglePotionInvisibility(false,handler);
        player.setFallDistance(0);
        player.setVelocity(new Vector(0,0,0));

        if (isSpectator)
        {



            for (BattlePlayer current: arena.getPlayers().values())
            {
               if (current.equals(this))
                  continue;

               if (current.isAlive())
                   current.getRawPlayer().hidePlayer(player);
               else
                   current.getRawPlayer().showPlayer(player);
            }
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setCanPickupItems(false);
            setAlive(false);
            ((CraftPlayer)player).getHandle().collidesWithEntities = false;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,0,false,false));


            tracking = null;



        }
        else
        {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setCanPickupItems(true);
            setAlive(true);
            ((CraftPlayer)player).getHandle().collidesWithEntities = true;
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            for (BattlePlayer current: arena.getPlayers().values())
            {
                if (current.equals(this))
                    continue;

                //Show the current battleplayer to all other players.
                current.getRawPlayer().showPlayer(player);

                //If the other player is currently in spectator, hide the spectator from them.

                if (!current.isAlive)
                    player.hidePlayer(current.getRawPlayer());
            }
        }
    }

    /*
    Puts a player into spectator mode
    if is final, will not make a countdown timer for respawning

     */
    public void handlePlayerIntoSpectator(@NotNull PacketHandler handler, boolean isFinal, @Nullable Player killer)
    {



        dropInventory(player.getLocation().clone(),killer);

        if (!isAlive || isEliminated) {
            PlayerHelper.clearInventory(this.player);
            sendPacketsAllNonEqual(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,((CraftPlayer)this.player).getHandle()));
            return;
        }

        toggleSpectator(true, handler);
        heal();


        if (isFinal)
        {
            setEliminated(true);
           boolean sendMessage = emptyEnderChest();
            sendPacketsAllNonEqual(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,((CraftPlayer)this.player).getHandle()));

           int remaining = team.getRemainingPlayers();
           if (remaining == 0)
               team.eliminate();

            if (killer !=null && sendMessage)
                killer.sendMessage(ChatColor.YELLOW+"Items dropped from "+player.getName()+"'s enderchest have been put into their team's forge.");

            return;
        }


        teleport(arena.getSpecSpawn());

        new BukkitRunnable()
        {
            int seconds = 0;
            public void run()
            {
                if (seconds < eliminationTime)
                {
                    setTimeTillRespawn(eliminationTime-seconds);

                    if (team.getBedExists())
                        sendRespawnTitle(TeamTitle.YOU_DIED,TeamTitle.RESPAWN_AFTER,getTimeTillRespawn(),0,60,10);
                    else
                        sendRespawnTitle(TeamTitle.BED_DESTROYED,TeamTitle.RESPAWN_AFTER,getTimeTillRespawn(),0,60,10);
                    seconds ++;
                }
                else if (player.isOnline())
                {
                    setTimeTillRespawn(0);
                    handlePlayerRespawn(handler);
                    cancel();
                }
                else {
                    setTimeTillRespawn(0);
                    cancel();
                }
            }
        }.runTaskTimer(arena.getPlugin(),0,20);
    }

    public void handlePlayerIntoSpectator(PacketHandler handler, boolean isFinal)
    {
        handlePlayerIntoSpectator(handler,isFinal, null);
    }




    /*
    This method handles the player respawning.
    This should not be used for their first spawn in. see handlePlayerFirstSpawn()
     */
    public void handlePlayerRespawn(PacketHandler handler)
    {
        setSurvival();
        setEliminated(false);
        team.teleportToBase(player);
        toggleSpectator(false, handler);


        player.getInventory().clear();

        if (hasShears)
            barManager.set(ItemHelper.toSoldItem(ShopItem.SHEARS,this),ShopItem.SHEARS,player);

        TieredItem degraded;

        if (getPick() != null) {
            degraded = handlePersistentItemDegradation(getPick());
            setPickDownwards(degraded);

            barManager.set(ItemHelper.toSoldItem(pick.getItem(), this), getPick().getItem(), player);
        }
        if (getAxe() != null) {
            degraded = handlePersistentItemDegradation(getAxe());
            setAxeDownwards(degraded);
            barManager.set(ItemHelper.toSoldItem(axe.getItem(), this), getAxe().getItem(), player);
        }
        barManager.set(ItemHelper.toSoldItem(ShopItem.WOODEN_SWORD,this), ShopItem.WOODEN_SWORD,player);
        barManager.set(ItemHelper.toSimpleItem(TRACKER_ITEM.sellMaterial, TRACKER_ITEM.name), TRACKER_ITEM,player);

        heal();
        equipArmor();

        sendTitle(TeamTitle.RESPAWNED.getMessage(), null,2,40,10);
        team.applyPlayerModifiersToPlayer(this);


    }


    /*
     Handles degradation for tools that are permanent by getting the previous tier (if any) of the current item
     DOES NOT set the degradated tool for the player. You need to do that separately.
     */
    private TieredItem handlePersistentItemDegradation(TieredItem current)
    {
        if (current == null)
            return null;

        TieredItem previousTier = ItemHelper.getPreviousTier(current);
        return previousTier == null ? current: previousTier;

    }

    //Handles the player's first spawn in.
    public void handlePlayerFirstSpawn()
    {
        player.getInventory().clear();
        player.setFlying(false);
        player.setAllowFlight(false);
        setSurvival();
        setEliminated(false);
        team.teleportToBase(player);
        heal();
        equipArmor();
        barManager.set(ItemHelper.toSoldItem(ShopItem.WOODEN_SWORD,this), ShopItem.WOODEN_SWORD,player);
        barManager.set(ItemHelper.toSimpleItem(TRACKER_ITEM.sellMaterial, TRACKER_ITEM.name), TRACKER_ITEM,player);
    }


    //Convenience Helper methods
    /////////////////////////////////////////////

    /*

    Convenience method for sending packets to the current player.
     */
    public void sendPacket(Packet<?> packet)
    {
        ( (CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    /*

    Initializing the scoreboard for the player.
     */
    private void createBoard()
    {
        board = new PlayerBoard(this,arena);
    }

    /*

    Convenience method for teleporting the player to a location.
     */
    public void teleport(Location loc)
    {
        player.teleport(loc);
    }

    private void heal()
    {
        player.setHealth(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setRemainingAir(300);
        player.setFoodLevel(20);

        ((CraftPlayer)player).getHandle().removeAllEffects();

    }

    /*
    Empties and clears the player's inventory.
    Currency items (gold, iron, diamonds, emeralds) are dropped, everything else is cleared.
     */
    public void dropInventory(final Location deathLocation, final Player killer)
    {
        Inventory inv = player.getInventory();

        org.bukkit.inventory.ItemStack stack  = player.getItemOnCursor();
        player.setItemOnCursor(null);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                World w = player.getWorld();
                final Location dropLocation = (killer == null ? deathLocation : killer.getLocation()).clone();

                if (stack != null && stack.getItemMeta() != null) {
                    org.bukkit.entity.Item drop = w.dropItem(dropLocation,stack);
                    drop.setPickupDelay(0);
                }

                Arrays.stream(inv.getContents()).filter(item -> Objects.nonNull(item)&&ItemHelper.isCurrencyItem(item)).forEach(item -> {
                    org.bukkit.entity.Item drop = w.dropItem(dropLocation,item);
                    drop.setPickupDelay(0);

                });

                PlayerHelper.clearInventory(player);
                cancel();
            }
        }.runTask(arena.getPlugin());

    }

      /*
        Empties and clears the player's enderchest.
        Currency items are dropped in their team's forge.

        If the enderchest was not empty, then returns true.
        Else returns false.
         */
    private boolean emptyEnderChest()
    {
        ArrayList<org.bukkit.inventory.ItemStack> toDrop = new ArrayList<>();
        Inventory chest = player.getEnderChest();
        Arrays.stream(chest.getContents()).filter(ItemHelper::isCurrencyItem).forEach(toDrop::add);
        chest.clear();

        new BukkitRunnable()
        {
            public void run()
            {
                Location loc = team.getForge().getForgeLocation();
                World world = loc.getWorld();
                for (org.bukkit.inventory.ItemStack item: toDrop)
                    world.dropItem(loc,item);

                cancel();
            }
        }.runTask(arena.getPlugin());

        return toDrop.size() > 0;
    }


    //applies an update to the statistics of the player for their scoreboard scoresets.
    //Does not apply the change to the scoreboard. That is handled in the gamerunner.
    public void updatePlayerStatistics()
    {
        board.setScoreName(FINALS.getPhrase(), FINALS.getPhrase()+getFinals());
        board.setScoreName(KILLS.getPhrase(), KILLS.getPhrase()+getKills());
        board.setScoreName(BEDS.getPhrase(), BEDS.getPhrase()+getBeds());
    }



    /*
    Convenience method for sending a message to the player. Only that player can see the message.
     */
    public void sendMessage(String message)
    {
        this.player.sendMessage(message);
    }


    /*
    Plays a sound from a packet for a player.
     */
    public void playSound(PacketSound sound)
    {
        Location loc = player.getLocation();
        sendPacket(new PacketPlayOutNamedSoundEffect(sound.getSoundName(),loc.getX(),loc.getY(),loc.getZ(),1,sound.getPitch()));
    }

    /*
    Returns a boolean whether the given id is part of the hashmap for the npcs
    Who need to be refreshed in terms of packets.
     */
    public synchronized boolean containsNPC(int id)
    {
        return toResend.containsKey(id);
    }

    /*
    Removes a npc to be resent to the player.
     */
    public synchronized void removeResender(int id)
    {
        toResend.remove(id);
    }


    /*
    Adds an npc to be resent to the player
     */
    public synchronized void addResender(ShopKeeper keeper)
    {
        toResend.putIfAbsent(keeper.getId(), keeper);
    }



    /*
    This is the code to make it so that the scoreboard is accurate in the tab and below the name. :D
     */
    public void sendHealthUpdatePackets()
    {
        PacketPlayOutScoreboardScore nameHealth =
        modify(new PacketPlayOutScoreboardScore(),player.getName(),HEALTH_CATEGORY.getPhrase(),Math.round((float)player.getHealth()));

        PacketPlayOutScoreboardScore tabHealth =
        modify(new PacketPlayOutScoreboardScore(),player.getName(),HEALTH_CATEGORY_TWO.getPhrase(),Math.round((float)player.getHealth()));


        arena.getPlayers().values().forEach(player -> {
            if (nameHealth != null)
                player.sendPacket(nameHealth);

            if (tabHealth != null)
                player.sendPacket(tabHealth);
        });
    }

    /*
    Modifies a packet with reflection for updating health values.
     */
    private PacketPlayOutScoreboardScore modify(PacketPlayOutScoreboardScore packet, String playerName, String objectiveName, int score)
    {
        try
        {
            Field fieldA = PacketPlayOutScoreboardScore.class.getDeclaredField("a");
            Field fieldB = PacketPlayOutScoreboardScore.class.getDeclaredField("b");
            Field fieldC = PacketPlayOutScoreboardScore.class.getDeclaredField("c");
            Field fieldD = PacketPlayOutScoreboardScore.class.getDeclaredField("d");

            fieldA.setAccessible(true);
            fieldB.setAccessible(true);
            fieldC.setAccessible(true);
            fieldD.setAccessible(true);

            fieldA.set(packet, playerName);
            fieldB.set(packet,objectiveName);
            fieldC.set(packet, score);
            fieldD.set(packet,PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE);

            fieldA.setAccessible(false);
            fieldB.setAccessible(false);
            fieldC.setAccessible(false);
            fieldD.setAccessible(false);

            return packet;
        }
        catch (Exception e)
        {
            return null;
        }
    }


    /*
    Hides the eliminated players from this player
     */
    public void hideEliminatedPlayers(){
        Collection<BattlePlayer> players = arena.getPlayers().values();

        for (BattlePlayer player: players)
        {
            if (player.isEliminated && !player.equals(this))
                sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,((CraftPlayer)player.getRawPlayer()).getHandle()));
        }
    }


    //Setter methods
    ////////////////////////////////////////////////////



    public void setSurvival()
    {
        this.player.setGameMode(GameMode.SURVIVAL);
    }

    public synchronized void setAlive(boolean isAlive)
    {
        this.isAlive = isAlive;
    }

    public synchronized void setTimeTillRespawn(int seconds)
    {
        this.timeTillRespawn = seconds;
    }

    public synchronized void setLastMilkTime(long time){
        this.lastMilk = time;
    }

    public long getLastMilk(){
        return lastMilk;
    }


    public synchronized void setFinals(int newFinals)
    {
        this.finals = newFinals;
    }

    public synchronized void setShears()
    {
        hasShears = true;
    }

    public synchronized void setKills(int newKills)
    {
        this.kills = newKills;
    }



    //////////////////////////////////////////

    //upgrades the pickaxe of the player.
    //pick is the item to upgrade the player's pick to.
    //updates the shop to display the next upgrade after the pick.
    public synchronized void setPickUpwards(TieredItem pick) {
        this.pick = pick;
        TieredItem upgrade = ItemHelper.getNextTier(pick);
           shopManager.replaceItem(pick.getItem(),upgrade == null? pick.getItem() : upgrade.getItem());

    }


    //downgrades the pickaxe of the player.
    //pick is the item to downgrade the player's pick to.
    //updates the shop to display the next upgrade after the pick.
    public synchronized void setPickDownwards(TieredItem pick) {
        TieredItem pickCloned = this.pick;
        this.pick = pick;

        TieredItem prevUpgrade = ItemHelper.getNextTier(pickCloned);
        TieredItem currentUpgrade = ItemHelper.getNextTier(pick);

        shopManager.replaceItem(prevUpgrade == null ? pick.getItem(): prevUpgrade.getItem(), currentUpgrade == null ? pick.getItem() : currentUpgrade.getItem());

    }


    //upgrades the axe of the player.
    //axe is the item to upgrade the player's axe to.
    //updates the shop to display the next upgrade after the axe.
    public synchronized void setAxeUpwards(TieredItem axe) {


        this.axe = axe;
        TieredItem upgrade = ItemHelper.getNextTier(axe);
        shopManager.replaceItem(axe.getItem(),upgrade == null? axe.getItem() : upgrade.getItem());
    }




    //downgrades the axe of the player.
    //pick is the item to downgrade the player's axe to.
    //updates the shop to display the next upgrade after the axe.
    public synchronized void setAxeDownwards(TieredItem axe) {
        TieredItem axeCloned = this.axe;
        this.axe = axe;

        TieredItem prevUpgrade = ItemHelper.getNextTier(axeCloned);
        TieredItem currentUpgrade = ItemHelper.getNextTier(axe);

        shopManager.replaceItem(prevUpgrade == null ? axe.getItem() : prevUpgrade.getItem(), currentUpgrade == null ? axe.getItem() : currentUpgrade.getItem());

    }

    /*

    Setter method to set the number of beds the player has broken.
    This value is later shown in their scoreboard, which is controlled externally.
     */
    public synchronized void setBeds(int bedNumber)
    {
        this.beds = bedNumber;
    }

    /*

    Setter method to set whether the player should be eliminated.
    This value is later shown in their scoreboard, which is controlled externally.
     */
    public synchronized void setEliminated(boolean isEliminated)
    {
        this.isEliminated = isEliminated;
    }

    /*
    Set the current armor of the player
     */
    public synchronized void setPurchasedArmor(TieredItem armor) {
        this.armor = armor;
    }





    //Getter methods
////////////////////////////////////////////////////


    public synchronized int getTimeTillRespawn() {
        return timeTillRespawn;
    }

    public synchronized boolean isAlive()
    {
        return this.isAlive;
    }

    public synchronized PlayerBoard getBoard()
    {
        return board;
    }

   public Player getRawPlayer()
   {
       return player;
   }

    public synchronized int getKills()
    {
        return kills;
    }

    public synchronized int getFinals()
    {
        return finals;
    }

    public synchronized int getBeds()
    {
        return beds;
    }

    public synchronized TieredItem getArmor() {
        return armor;
    }

    public synchronized boolean hasShears() {
        return hasShears;
    }

    public synchronized TieredItem getPick() {
        return pick;
    }

    public synchronized TieredItem getAxe() {
        return axe;
    }

    public UUID getUUID()
    {
        return player.getUniqueId();
    }

    public BattleTeam getTeam()
    {
        return team;
    }

    public HotbarManager getBarManager()
    {
        return barManager;
    }

    public PlayerInventoryManager getShopManager()
    {
        return shopManager;
    }

    public int getNumber(){
        return playerScoreboardId;
    }

    public ChatOptionSelectionInventory getChatOptionInv() {
        return chatOptionInv;
    }

    public synchronized boolean isEliminated()
    {
        return this.isEliminated;
    }

    public QuickBuyEditorInventory getQuickEditor() {
        return quickEditor;
    }




}


