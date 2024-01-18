package me.camm.productions.bedwars.Items.SectionInventories.Templates;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Items.ItemProperties.DefaultTemplateNavigation;
import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import static me.camm.productions.bedwars.Items.ItemProperties.DefaultTemplateNavigation.EMPTY;


/**
 * @author CAMM
 * This abstract class is used for inventory util
 */
//this is a subclass of inventory.
public abstract class ShopInventory extends CraftInventoryCustom implements ISectionInventory
{
    protected final boolean isInflated;
    protected final Arena arena;


    public ShopInventory(InventoryHolder owner, int size, String title, boolean isInflated, Arena arena) {
        super(owner, size, title);
        this.isInflated = isInflated;
        this.arena = arena;
    }

    @Override
    public void setItem(int index, ItemStack item) {
        super.setItem(index, item);
    }

    @Override
    public void setItem(int index, ShopItem item, boolean isInflated) {
        try
        {
           super.setItem(index, ItemHelper.toDisplayItem(item, isInflated));
        }
        catch (IndexOutOfBoundsException | NullPointerException ignored)
        {

        }
    }

    @Override
    public void setInventoryItems() {

    }

    /*
        Setting the items at the top of an inv for navigation
        @param inv
        @param isInflated
        @param includeEmpties
         */
    @Override
    public void setTemplate(boolean isInflated, boolean includeEmpties)
    {

        for (DefaultTemplateNavigation template: DefaultTemplateNavigation.values())
        {
            if (template == EMPTY)
                continue;

            int[] range = template.getRange();
            for (int slot: range)
                setItem(slot,template.getItem(),isInflated);
        }
        if (includeEmpties)
        {
            fillEmpties();
        }


    }

    private void fillEmpties()
    {
        int[] range = EMPTY.getRange();
        for (int slot: range) {
            if (getItem(slot)==null || getItem(slot).getItemMeta()==null)
             setItem(slot, EMPTY.getItem(), false);
        }


    }


    @Override
    public void operate(InventoryClickEvent event) {


        HumanEntity player = event.getWhoClicked();
        Inventory playerInv = player.getInventory();
        Inventory clicked = event.getClickedInventory();

        BattlePlayer battlePlayer = arena.getPlayers().getOrDefault(player.getUniqueId(), null);
        if (battlePlayer == null)
        {

            return;
        }

        InventoryOperationHelper.handleDefaultRestrictions(event, arena);

        boolean cancel = InventoryOperationHelper.triedToPlaceIn(event, this);
           event.setCancelled(cancel);


        //if the player's inventory is the clicked inv
        if (playerInv.equals(clicked))
            return;

        //if the enderchest is the clicked inv, then return.
        if (player.getEnderChest().equals(clicked))
            return;

        InventoryOperationHelper.sellQuickbuy(event,arena, isInflated);
    }

    @Override
    public void operate(InventoryDragEvent event) {

        if (InventoryOperationHelper.didTryToDragIn(event,this))
            event.setCancelled(true);
        else
            InventoryOperationHelper.handleDefaultRestrictions(event,arena);
    }


}
