package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_START;


/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum MeleeConfig
{
    STONE_SWORD(QUICK_INV_BORDER_START.getValue(), ShopItem.STONE_SWORD),
    IRON_SWORD(QUICK_INV_BORDER_START.getValue()+1, ShopItem.IRON_SWORD),
    DIAMOND_SWORD(QUICK_INV_BORDER_START.getValue()+2, ShopItem.DIAMOND_SWORD),
    KB_STICK(QUICK_INV_BORDER_START.getValue()+3, ShopItem.STICK);

    private final int slot;
    private final ShopItem item;

    MeleeConfig(int slot, ShopItem item)
    {
        this.item = item;
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public ShopItem getItem() {
        return item;
    }
}
