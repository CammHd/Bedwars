package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_START;


/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum RangedConfig
{
    ARROW(QUICK_INV_BORDER_START.getValue(), ShopItem.ARROW),
    BOW(QUICK_INV_BORDER_START.getValue()+1, ShopItem.BOW),
    POWER(QUICK_INV_BORDER_START.getValue()+2, ShopItem.POW_BOW),
    PUNCH(QUICK_INV_BORDER_START.getValue()+3, ShopItem.PUNCH_BOW);
    //start from slot 19

    private final int slot;
    private final ShopItem item;

    RangedConfig(int slot, ShopItem item) {
        this.slot = slot;
        this.item = item;
    }

    public int getSlot() {
        return slot;
    }

    public ShopItem getItem() {
        return item;
    }
}
