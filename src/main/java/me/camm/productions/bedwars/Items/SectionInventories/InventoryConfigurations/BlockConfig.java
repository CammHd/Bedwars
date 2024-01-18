package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_START;

/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum BlockConfig
{
    WOOL(QUICK_INV_BORDER_START.getValue(), ShopItem.WOOL),
    CLAY(QUICK_INV_BORDER_START.getValue()+1, ShopItem.HARDENED_CLAY),
    GLASS(QUICK_INV_BORDER_START.getValue()+2, ShopItem.STAINED_GLASS),
    END_STONE(QUICK_INV_BORDER_START.getValue()+3, ShopItem.ENDER_STONE),
    LADDER(QUICK_INV_BORDER_START.getValue()+4, ShopItem.LADDER),
    WOOD(QUICK_INV_BORDER_START.getValue()+5, ShopItem.PLANKS),
    OBSIDIAN(QUICK_INV_BORDER_START.getValue()+6, ShopItem.OBSIDIAN);


    private final int slot;
    private final ShopItem item;

    BlockConfig(int slot, ShopItem item)
    {
        this.slot = slot;
        this.item = item;
    }

    public int getSlot()
    {
        return slot;
    }

    public ShopItem getItem() {
        return item;
    }
}
