package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_START;


/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum ToolsConfig
{
    SHEARS(QUICK_INV_BORDER_START.getValue(), ShopItem.SHEARS),
    PICKAXE(QUICK_INV_BORDER_START.getValue()+1, ShopItem.WOODEN_PICKAXE),
    AXE(QUICK_INV_BORDER_START.getValue()+2, ShopItem.WOOD_AXE);

    private final int slot;
    private final ShopItem item;

    ToolsConfig(int slot, ShopItem item) {
        this.slot = slot;
        this.item = item;
    }

    public int getSlot() {
        return slot;
    }

    public ShopItem getItem() {
        return item;
    }

    //this enum is to hold the slots and the inventory items that should go in the tools section


    //note: both this inventory and the quickbuy inventory of the player must be synced
    //up to match what they buy in the shop in terms of tools.

    //also, perm items also need to be synced (so like armor and tools that are tiered)
}
