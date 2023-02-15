package me.camm.productions.bedwars.Listeners;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.BedMessage;
import me.camm.productions.bedwars.Arena.Teams.TeamTitle;
import me.camm.productions.bedwars.Entities.ActiveEntities.GameTNT;
import me.camm.productions.bedwars.Structures.SoakerSponge;
import me.camm.productions.bedwars.Structures.Tower;
import me.camm.productions.bedwars.Util.BlockTag;
import me.camm.productions.bedwars.Util.Helpers.BlockTagManager;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import me.camm.productions.bedwars.Util.PacketSound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

//import static me.camm.productions.bedwars.Util.Locations.BlockRegisterType.*;



/*
This class listens for and handles interactions which involve blocks
@author CAMM
@author bipi
 */
public class BlockInteractListener implements Listener {
    private final Plugin plugin;
    private final Arena arena;
    private final HashSet<String> activeSponges;
    private final static BedMessage[] messages;
    private final static Random rand;
    private ChatSender sender;

    //Messages for when the bed is broken
    //random for picking a random message
    static {
        messages = BedMessage.values();
        rand = new Random();
    }

    public BlockInteractListener(Plugin plugin, Arena arena)  //constructor
    {
        this.plugin = plugin;
        this.arena = arena;
        this.activeSponges = new HashSet<>();
        sender = ChatSender.getInstance();

    }


    @EventHandler(priority = EventPriority.HIGHEST)   //determines the state of the block
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {

        //get the hashmap of the players and the event info
        Map<UUID, BattlePlayer> players = arena.getPlayers();
        Block block = event.getBlockPlaced();
        Player placer = event.getPlayer();


        //if the player isn't registered, return and cancel.
        if (!players.containsKey(placer.getUniqueId())) {
            event.setCancelled(true);
            placer.sendMessage(ChatColor.RED + "You're not registered!");
            return;
        }

        //if the player is dead, stop it
        BattlePlayer player = players.get(placer.getUniqueId());
        if (player.getIsEliminated() || (!player.getIsAlive())) {
            event.setCancelled(true);
            return;
        }

        BlockTagManager manager = BlockTagManager.get();

        if (!manager.isInbounds(block)) {
            placer.sendMessage(ChatColor.RED + "You cannot place blocks here!");
            event.setCancelled(true);
            return;
        }

        if (manager.hasTag(block)) {

            byte tag = manager.getTag(block);
            if (!(tag == BlockTag.ALL.getTag()) || (tag == BlockTag.NONE.getTag())) {
                placer.sendMessage(ChatColor.RED + "You cannot place blocks here!");
                event.setCancelled(true);
                return;
            }


        }

        Material type = event.getBlockPlaced().getType();
        switch (type)       //test if tnt, sponge, chest
        {
            case TNT:
                summonTNT(event, player);

                //summon tnt

                break;

            case SPONGE:
                new SoakerSponge(plugin, block, this).soak();

                //make a sponge water soaker-upper

                break;

            case CHEST:
                new Tower(event, plugin, (byte) players.get(event.getPlayer().getUniqueId()).getTeam().getTeamColor().getValue());
                //get the player team and create a popup tower

                break;
        }

    }

    //method to handle when players try to place blocks in spectators. We just allow the event in this case.
    @EventHandler
    public void onBlockCanPlaceEvent(@NotNull BlockCanBuildEvent event) {
        event.setBuildable(true);
    }

    /*
    EntityExplodeEvent
      -> event.getBlocks

      1:
     */

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {

        Block block = event.getBlock();
        BlockTagManager blocks = BlockTagManager.get();
        if (!blocks.hasTag(block)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You can't break blocks here!");
            return;
        }

        if (block.getType() == Material.SPONGE || block.getType() == Material.CHEST) {
            event.setCancelled(true);
            return;
        }

        Player whoBroke = event.getPlayer();
        if (!arena.getPlayers().containsKey(whoBroke.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        BattlePlayer broke = arena.getPlayers().get(whoBroke.getUniqueId());

        if (broke.getIsEliminated() || (!broke.getIsAlive())) {
            event.setCancelled(true);
            return;
        }


        if (block.getType() == Material.BED) {
            if (isBedBroken(block, broke)) {
                return;
            }
        }


        if (BlockTag.ALL.getTag() != blocks.getTag(block)) {
            event.setCancelled(true);
            whoBroke.sendMessage(ChatColor.RED + "You can't break blocks here!");
            return;
        }



        BattleTeam comparison = null;
        for (BattleTeam team : arena.getTeams().values()) {
            Coordinate chest = team.getChest();
            if (chest.isBlock(arena.getWorld(), block)) {
                comparison = team;
                break;
            }
        }

        if (comparison == null)
            return;

        if (comparison.isEliminated())
            return;

        if (!broke.getTeam().equals(comparison)) {
            event.setCancelled(true);
            broke.sendMessage(ChatColor.RED + "You cannot open that chest while " + comparison.getTeamColor().getName() + "is not eliminated!");
        }
    }



    public boolean isBedBroken(Block block, BattlePlayer broke)
    {
        //A bed has been broken. Get the team and send message and modify the variables.
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();


        //Attempt to find the team that the bed belonged to.
        BattleTeam broken = null;


        for (BattleTeam team: arena.getTeams().values())
        {
            if (team.getBed().containsCoordinate(x,y,z))
            {
                broken = team;
                break;
            }
        }


        //If we can't find a team that the bed belonged to, send an error message.



        if (broken == null) {
            sender.sendConsoleMessage(" A bed was broken at "+x+", "+y+", "+z+" ." +
                    "It was registered, but couldn't find a team it belonged to!",Level.WARNING);
            return false;
        }



        //we need to cancel the event regardless so that putOnLastStand() can work,
        // or if the player isn't on a team, or if the player isn't alive.



        //If the player tried to break their own bed.

        if ((!broke.getIsAlive())||broke.getIsEliminated()) {
            return false;
        }

        if (broken.equals(broke.getTeam()))
        {
            broke.sendMessage(ChatColor.RED+"You can't break your own bed!");
            return false;
        }


        broken.putOnLastStand();
        broke.setBeds(broke.getBeds()+1);


            /*
            Playing sounds for all players on the team who's bed was broken.
            if the player on the team is alive, then we send a subtitle about them being on their last life.
            If the player is dead but not eliminated, then we send them a packet about their bed destroyed with the respawn.
             */

        broken.getPlayers().values().forEach(battlePlayer ->
        {
            battlePlayer.playSound(PacketSound.WITHER);

            if (battlePlayer.getIsAlive())
                battlePlayer.sendTitle(TeamTitle.BED_DESTROYED.getMessage(), TeamTitle.LAST_LIFE_WARNING.getMessage(), 10,40,10);
            else
                battlePlayer.sendRespawnTitle(TeamTitle.BED_DESTROYED,TeamTitle.RESPAWN_AFTER, battlePlayer.getTimeTillRespawn(), 10,40,10);
        });


        for (BattlePlayer player: arena.getPlayers().values())
            player.getBoard().updateTeamStatuses();

        String lead = messages[rand.nextInt(messages.length)].getMessage();
        sender.sendMessage(ChatColor.WHITE+""+ChatColor.BOLD+"BED DESTRUCTION >"+broken.getTeamColor().getName()+ChatColor.RESET+" was "+lead+" by "+
                broke.getTeam().getTeamColor().getChatColor()+broke.getRawPlayer().getName()+"!");
        return true;
    }

    @EventHandler
    public void onWaterFlow(@NotNull BlockFromToEvent event)
    {
        //If a block has any metadata about the sponge, then we cancel the event and return.
        Block to = event.getToBlock();
        BlockTagManager manager = BlockTagManager.get();

        if (!manager.isInbounds(to)) {
            event.setCancelled(true);
            return;
        }

        byte tag = manager.getTag(to);
        if (tag != BlockTag.ALL.getTag()) {
            event.setCancelled(true);
            return;
        }

        for (String id: activeSponges)
        {
            if (event.getToBlock().hasMetadata(id)) {
                event.setCancelled(true);
                return;
            }

        }
    }

    @EventHandler
    public void onFireExtinguishNatural(BlockFadeEvent event){
        if (event.getBlock()==null)
            return;

        Block block = event.getBlock();
        Block below = block.getLocation().add(0,-1,0).getBlock();

        if (below != null && below.getType()==Material.AIR)
            return;

        if (block.getType()==Material.FIRE)
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(@NotNull BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFireSpread(@NotNull BlockSpreadEvent event){

        if (event.getBlock()==null)
            return;

        Block source = event.getSource();

        if (source==null)
            return;

        if (source.getType()==Material.FIRE)
            event.setCancelled(true);
    }


    //Summoning tnt.
    private void summonTNT(BlockPlaceEvent event, BattlePlayer player)
    {
      new GameTNT(event,player);
    }

    public synchronized void addActiveSponge(@NotNull UUID id)
    {
        activeSponges.add(id.toString());
    }

    public synchronized void removeActiveSponge(@NotNull UUID id)
    {
        activeSponges.remove(id.toString());
    }



}
