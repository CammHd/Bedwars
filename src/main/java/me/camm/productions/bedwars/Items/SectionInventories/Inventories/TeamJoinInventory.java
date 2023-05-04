package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Game.GameRunner;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import me.camm.productions.bedwars.Util.Helpers.TeamHelper;
import me.camm.productions.bedwars.Validation.RegistrationException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName.TEAM_JOIN;


/*
 * @author CAMM
 * This inventory models the inventory for joining teams
 */
//This class is not part of the other shop inventories. Not an instance of Inventory.
public class TeamJoinInventory extends CraftInventoryCustom implements IGameInventory   //class to make inv for players to join teams
{

    private final Collection<BattleTeam> teams;
    private final Arena arena;
    private final GameRunner runner;
    private final ChatSender sender;


    public TeamJoinInventory(Arena arena, GameRunner runner)
    {
        super(null,(((arena.getTeams().size()/9))+1)*9,TEAM_JOIN.getTitle());
        teams = arena.getTeams().values();
        this.arena = arena;
        this.runner = runner;
        this.sender = ChatSender.getInstance();
        init();
    }

    @SuppressWarnings("deprecation")
    private void init(){
        int slot = 0;
        for (BattleTeam currentTeam: teams)
        {
            ItemStack wool = new ItemStack(Material.WOOL,1,(short)0,(byte)(currentTeam.getTeamColor().getValue()));
            ItemMeta woolMeta = wool.getItemMeta();

            woolMeta.setDisplayName(currentTeam.getTeamColor().getName());
            wool.setItemMeta(woolMeta);
            setItem(slot,wool);
            slot++;
        }
    }



    @Override
    public boolean equals(Inventory other)
    {
        return super.equals(other);
    }

    @Override
    public void operate(InventoryDragEvent event) {
        InventoryOperationHelper.handleDefaultRestrictions(event, arena);
    }

    public void operate(InventoryClickEvent event) {
       boolean cancel = InventoryOperationHelper.triedToPlaceIn(event, this);

        if (cancel)
            event.setCancelled(true);

        try {
            registerToTeam(event);
        }
        catch (IOException e) {
            sender.sendConsoleMessage(e.getMessage(), Level.WARNING);
        }
    }



    /*
 @Author CAMM
 Adds a player to a team, or changes their team if they are already on one.
  */
    private void registerToTeam(InventoryClickEvent event) throws IOException
    {

        System.out.println("tjInv adding player to team uuid="+arena.getDebugUUID());




        Inventory inv = event.getClickedInventory();
        HumanEntity player = event.getWhoClicked();
        Map<UUID, BattlePlayer> registeredPlayers = arena.getPlayers();

        if (!inv.equals(this)|| ItemHelper.isItemInvalid(event.getCurrentItem()))
            return;

        if (runner.isRunning())
        {
            player.sendMessage(ChatColor.YELLOW+"Wait for the current game to finish!");
            player.closeInventory();
            return;
        }

        if (event.getCurrentItem().getType() != Material.WOOL)
            return;

        ItemStack stack = event.getCurrentItem();

        String name = stack.getItemMeta().getDisplayName();
        event.setCancelled(true);

        Map<String, BattleTeam> arenaRegistered = arena.getTeams();
        BattleTeam picked = arenaRegistered.getOrDefault(name, null);
        player.closeInventory();
        if (picked == null)
            throw new RegistrationException("Could not find the team "+name+" even though the option was given!");



        BattlePlayer currentPlayer;
        HumanEntity whoClicked = event.getWhoClicked();

        if (registeredPlayers.containsKey(whoClicked.getUniqueId()))  //check if the player is registered
        {

            currentPlayer = registeredPlayers.get(whoClicked.getUniqueId());

            try {
                //this may throw an exception
                boolean isChanged = registeredPlayers.getOrDefault(whoClicked.getUniqueId(),null)
                        .changeTeam(arena.getTeams().get(name));

                if (isChanged)
                {
                    sender.sendMessage(currentPlayer.getRawPlayer().getName() + " changed their Team to " + currentPlayer.getTeam().getTeamColor() + "!");
                    runner.initializeTimeBoardHead(currentPlayer);
                    TeamHelper.updateTeamBoardStatus(registeredPlayers.values());
                }

            }
            catch (NullPointerException e)
            {
                player.sendMessage(ChatColor.RED+"Could not change teams!");
            }

        }
        else  // If they were not in the team before.
        {
            BattleTeam team = arena.getTeams().get(name);
            currentPlayer = new BattlePlayer((Player) event.getWhoClicked(), team, arena, arena.assignPlayerNumber());
            //Since the player board is initialized before the player joins, we get the incorrect amount of players on the team initially.

            boolean isAdded = team.addPlayer(currentPlayer);

            if (isAdded)
            {
                arena.addPlayer(whoClicked.getUniqueId(), currentPlayer);
                sender.sendMessage(ChatColor.GOLD + whoClicked.getName() + " Joined Team " + team.getTeamColor());
                runner.initializeTimeBoardHead(currentPlayer);
                TeamHelper.updateTeamBoardStatus(registeredPlayers.values());
            } else
                whoClicked.sendMessage(ChatColor.RED + "Could not join the team!");
        }

    }


}

