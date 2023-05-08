package me.camm.productions.bedwars.Listeners;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Game.Commands.CommandKeyword;
import me.camm.productions.bedwars.Arena.Game.GameRunner;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Entities.ShopKeeper;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Helpers.TeamHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogListener implements Listener//, IArenaChatHelper, IArenaWorldHelper, IPlayerUtil
{
    private PacketHandler packetHandler;
    private final Arena arena;
    private final Plugin plugin;
    private final GameRunner runner;

    private final ArrayList<ShopKeeper> keepers;
    private final HashMap<UUID, PermissionAttachment[]> perms;
    private final ChatSender sender;

    public LogListener(Arena arena, GameRunner runner,ArrayList<ShopKeeper> keepers) {
        this.packetHandler = null;
        this.arena = arena;
        this.plugin = arena.getPlugin();
        this.runner = runner;
        this.keepers = keepers;
        perms = new HashMap<>();
        sender = ChatSender.getInstance();

    }


    public void initPacketHandler(PacketHandler handler){
        if (handler == null)
            throw new IllegalArgumentException("packet handler is null");
        this.packetHandler = handler;
    }


    /*
@author CAMM
 This method listens for a player to log out, and deals with both their spot on their team,
 and the scoreboards of other players.
 */

    @EventHandler
    public void onPlayerLogOut(PlayerQuitEvent event)
    {

        if (!runner.isRunning())
            return;

        Map<UUID,BattlePlayer> registeredPlayers = arena.getPlayers();

        if (packetHandler!=null) {
            if (packetHandler.channelsContains(event.getPlayer()))
                packetHandler.removePlayer(event.getPlayer());
        }

        if (!registeredPlayers.containsKey(event.getPlayer().getUniqueId()))
            return;

        //setting the quit message
        BattlePlayer current = registeredPlayers.get(event.getPlayer().getUniqueId());

        if (current.isEliminated())
            return;


        event.setQuitMessage(current.getTeam().getTeamColor().getChatColor()+current.getRawPlayer().getName()+ ChatColor.YELLOW+" has Quit!");
        current.dropInventory(current.getRawPlayer().getLocation().clone(),null);

        if (!runner.isRunning()) {
            TeamHelper.updateTeamBoardStatus(registeredPlayers.values());
            return;
        }
        BattleTeam team = current.getTeam();




        //if is on last life
        if (!team.getBedExists())
        {
            //eliminate them
            sender.sendMessage(current.getTeam().getTeamColor().getChatColor()+current.getRawPlayer().getName()+ChatColor.YELLOW+" was on their last life! They have been eliminated!");
            current.setEliminated(true);
            TeamHelper.updateTeamBoardStatus(registeredPlayers.values());
            //check for the team stuff to see if there's a win here.
        }

        int remaining = team.getRemainingPlayers();

        //since the method considers the player logging
        //out to still be online, then we -1.
        if (remaining -1 <= 0)
        {
            team.eliminate();
            TeamHelper.updateTeamBoardStatus(registeredPlayers.values());
            runner.attemptEndGame();
        }
    }


    /*
 @Author CAMM
 This method listens for a player to join the game.
 It handles if a player was previously registered in the game, updating scoreboards,
 replacing the previous player object, and resetting their spot on the packet handler.
  */
    public void addPerms(Player player){
        if (!runner.isRunning()) {

            if (!perms.containsKey(player.getUniqueId())) {
                CommandKeyword[] words = CommandKeyword.values();
                PermissionAttachment[] attachments = new PermissionAttachment[words.length];

                for (int slot=0;slot< attachments.length;slot++) {
                    attachments[slot] = player.addAttachment(plugin);
                    attachments[slot].setPermission(words[slot].getPerm(),true);
                }
                perms.put(player.getUniqueId(),attachments);
                player.recalculatePermissions();

            }

        }

    }



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

       addPerms(player);


        BattlePlayer current;
        BattleTeam team;
        boolean isBedExists;

        Map<UUID,BattlePlayer> registeredPlayers = arena.getPlayers();
        UUID id = player.getUniqueId();

        if (!registeredPlayers.containsKey(id)) //if the uuids are the same
            return;


        current = registeredPlayers.get(id);
        team = current.getTeam();



        current.refactorPlayer(player);

        isBedExists = team.getBedExists();

        if (runner.isRunning() && packetHandler != null)
        {
            if (!isBedExists)
                current.handlePlayerIntoSpectator(packetHandler,true);
            else
                current.handlePlayerRespawn(packetHandler);
        }

        current.getRawPlayer().setScoreboard(arena.getHealthBoard());  //refreshing the board.
        runner.initializeTimeBoardHead(current);
        TeamHelper.updateTeamBoardStatus(registeredPlayers.values());


        if (!runner.isRunning())
            return;


        if (packetHandler.channelsContains(player)) {
            packetHandler.removePlayer(player);
        }
        packetHandler.addPlayer(player);


        for (ShopKeeper keeper: keepers) {
            keeper.sendNPC(player);
            keeper.setRotation(player);
        }

        current.handlePlayerIntoSpectator(packetHandler,!team.getBedExists(),null);

    }
}
