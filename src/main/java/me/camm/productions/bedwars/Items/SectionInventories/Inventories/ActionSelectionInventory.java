package me.camm.productions.bedwars.Items.SectionInventories.Inventories;


import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.ActionSelectionConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

/*
 * Unfinished.
 */
public class ActionSelectionInventory extends CraftInventoryCustom implements IGameInventory {

    private final Arena arena;

    public ActionSelectionInventory(Arena arena) {
        super(null, InventoryProperty.SMALL_SHOP_SIZE.getValue(), InventoryName.TRACKER_COMMS.getTitle());
        this.arena = arena;
        init();
    }

    public void init(){
        //set items here

        for (ActionSelectionConfig config: ActionSelectionConfig.values()) {
            setItem(config.getSlot(), ItemHelper.toSimpleItem(config.getMat(), config.getName()));
        }
    }

    @Override
    public boolean equals(Inventory other) {
        return super.equals(other);
    }

    @Override
    public void operate(InventoryClickEvent event) {



        event.setCancelled(InventoryOperationHelper.triedToPlaceIn(event, this));
        Map<UUID, BattlePlayer> players = arena.getPlayers();

        UUID id = event.getWhoClicked().getUniqueId();
        BattlePlayer clicked = players.getOrDefault(id, null);
        if (clicked == null)
            return;

        Player raw = clicked.getRawPlayer();

       Inventory tracker = clicked.getTeam().getTrackerInventory();
        Inventory chat = (Inventory)arena.getChatInv();

        int slot = event.getSlot();

        ActionSelectionConfig current = null;
        for (ActionSelectionConfig config: ActionSelectionConfig.values()) {
            if (config.getSlot() == slot) {
                current = config;
                break;
            }
        }

        if (current == null)
            return;



        ///there is a bug related to InventoryClickEvents where if you click on an item, and that makes the plugin
        //open another inventory for the player, if the event is NOT cancelled, then inventory click events
        //will not fire for the player until they relog.
        //see: https://bukkit.org/threads/inventoryclickevent-not-being-called.447616/
        event.setCancelled(true);

        switch (current) {
            case CHAT:
                raw.openInventory(chat);
                break;
            case TRACKER:
                raw.openInventory(tracker);
        }



    }

    @Override
    public void operate(InventoryDragEvent event) {

        InventoryOperationHelper.didTryToDragIn(event,this);
    }
}
