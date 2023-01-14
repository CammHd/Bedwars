package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Util.DataSets.ItemStackSet;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


/*
 * @author CAMM
 * This inventory models the quick buy editor for the player
 */
public class QuickBuyEditorInventory extends CraftInventoryCustom implements IGameInventory
{
    private final BattlePlayer owner;
    private ItemStack currentAdding;
    private final Arena arena;

    public QuickBuyEditorInventory(BattlePlayer owner) {
        super(null, InventoryProperty.SHOP_SIZE.getValue(), InventoryName.EDIT_QUICKBUY.getTitle());
        this.owner = owner;
        this.arena = owner.getTeam().getArena();
        currentAdding = new ItemStack(Material.AIR);
        updateConfiguration();
    }

    public void updateConfiguration(){
        QuickBuyInventory section = owner.getShopManager().getQuickBuy();
         for (ItemStackSet set: section.getItems())
             setItem(set.getSlot(), set.getStack());
         setCurrentAdding(currentAdding);
    }


    public void setCurrentAdding(ItemStack stack){
        currentAdding = stack;

        // ensures that we get 1/2 of the hotbar length. Integer division.
        setItem(InventoryProperty.HOT_BAR_END.getValue()/2,currentAdding);
    }


    public void display() {
        owner.getRawPlayer().openInventory(this);
    }

    public void applyConfigChange(int slot){
        QuickBuyInventory section = owner.getShopManager().getQuickBuy();

        section.setItem(slot, new ItemStack(Material.AIR));
        section.setItem(slot, currentAdding);

        setItem(slot, new ItemStack(Material.AIR));
        setItem(slot, currentAdding);
    }

    @Override
    public boolean equals(Inventory other) {
        return super.equals(other);
    }

    @Override
    public void operate(InventoryClickEvent event) {
        InventoryOperationHelper.operateInventoryEdit(event, arena);
    }

    @Override
    public void operate(InventoryDragEvent event) {

        if (InventoryOperationHelper.didTryToDragIn(event, this)) {
            event.setCancelled(true);

            return;
        }
        InventoryOperationHelper.handleDefaultRestrictions(event, arena);
    }

    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
    }


}
