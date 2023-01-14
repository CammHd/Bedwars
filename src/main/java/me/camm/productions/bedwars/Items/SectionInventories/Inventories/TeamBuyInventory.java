package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamInventoryConfig;

import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName.TEAM_BUY;

/*
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
public class TeamBuyInventory extends CraftInventoryCustom implements IGameInventory
{

    private final Arena arena;
    public TeamBuyInventory(Arena arena) {
        super(null,54,TEAM_BUY.getTitle());
        this.arena = arena;
        setTemplateItems();

    }

    private void setTemplateItems()
    {
        TeamInventoryConfig[] config = TeamInventoryConfig.values();
        for (TeamInventoryConfig item: config)
        {
           ItemStack display = ItemHelper.toTeamDisplayItem(item.getItems(),1); //1 is put there for the initial description to be orange.
            for (int slot: item.getSlots())
                setItem(slot, display);
        }
    }


    public void setItem(@NotNull TeamInventoryConfig config, int index){
        ItemStack display = ItemHelper.toTeamDisplayItem(config.getItems(),index);
        super.setItem(config.getSlots()[0],display);
    }

    public void setItem(TeamInventoryConfig config, int arrayDisplayIndex, int inventorySlotIndex)
    {
        try {
        ItemStack display = ItemHelper.toTeamDisplayItem(config.getItems(), arrayDisplayIndex);

        if (display != null) {
            display.getItemMeta().getLore().forEach(string -> {
            });
        }

        super.setItem(inventorySlotIndex,display);
          }
    catch(Exception e)
     {
         e.printStackTrace();
     }

    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals(other);
    }

    @Override
    public void operate(InventoryClickEvent event) {

        Map<UUID, BattlePlayer> players = arena.getPlayers();
        UUID id = event.getWhoClicked().getUniqueId();

        BattlePlayer battlePlayer = players.getOrDefault(id, null);

        if (battlePlayer == null) {
            event.setCancelled(true);
            return;
        }

        InventoryOperationHelper.handleDefaultRestrictions(event, arena);
        InventoryOperationHelper.sellTeamBuy(event,arena);


    }

    @Override
    public void operate(InventoryDragEvent event) {

        if (InventoryOperationHelper.didTryToDragIn(event, this)) {
            event.setCancelled(true);

            return;
        }
        InventoryOperationHelper.handleDefaultRestrictions(event, arena);
    }


}
