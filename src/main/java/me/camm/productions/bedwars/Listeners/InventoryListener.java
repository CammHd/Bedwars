package me.camm.productions.bedwars.Listeners;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.GameRunner;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.TeamJoinInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final Arena arena;
    private final GameRunner runner;
    private final HashMap<String, InventoryName> titles;
    private final Map<UUID, BattlePlayer> registeredPlayers;
    private final IGameInventory joinInventory;
    private final ChatSender sender;


    public InventoryListener(GameRunner runner) {
        sender = ChatSender.getInstance();
        this.arena = runner.getArena();
        this.runner = runner;
        registeredPlayers = arena.getPlayers();
        titles = new HashMap<>();
        InventoryName[] names = InventoryName.values();
        for (InventoryName name : names)
            titles.put(name.getTitle(), name);
        this.joinInventory = new TeamJoinInventory(arena, runner);
    }

    public IGameInventory getJoinInventory() {
        return joinInventory;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)  //for joining teams / other things
    {



        if (event instanceof InventoryCreativeEvent) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        HumanEntity player = event.getWhoClicked();
        BattlePlayer battlePlayer = registeredPlayers.getOrDefault(player.getUniqueId(), null);
        Inventory inv = event.getClickedInventory();

        if (battlePlayer == null || inv.equals(joinInventory)) {
            joinInventory.operate(event);
            return;
        }


        if (!runner.isRunning()) {
            return;
        }

        Map<Integer, IGameInventory> monitors = battlePlayer.getAccessibleInventories();

        Inventory clicked = event.getClickedInventory();
        IGameInventory gameInventory = monitors.getOrDefault(clicked.hashCode(), null);


        if (gameInventory == null) {
         InventoryOperationHelper.handleDefaultRestrictions(event, arena);
        }
        else {
            gameInventory.operate(event);
        }


        /////////////////////


    }//method




        /*
    @Author CAMM
    This method handles the case of a player clicking on one of the inventories present in the game.
     */


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        UUID id = event.getWhoClicked().getUniqueId();

        BattlePlayer battlePlayer = registeredPlayers.getOrDefault(id, null);
        if (battlePlayer == null) {

            if (InventoryOperationHelper.didTryToDragIn(event, (Inventory)joinInventory)) {
                event.setCancelled(true);
            }
            return;
        }


        Map<Integer, IGameInventory> inventories = battlePlayer.getAccessibleInventories();

        IGameInventory dragged = inventories.getOrDefault(event.getInventory().hashCode(), null);

        if (dragged == null) {
            InventoryOperationHelper.handleDefaultRestrictions(event, arena);
            return;
        }

        dragged.operate(event);
    }





}
