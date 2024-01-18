package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_START;


/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum PotionConfig
{
    SPEED(QUICK_INV_BORDER_START.getValue(), ShopItem.SPEED_POT),
    JUMP(QUICK_INV_BORDER_START.getValue()+1, ShopItem.JUMP_POT),
    INVIS(QUICK_INV_BORDER_START.getValue()+2, ShopItem.INVIS_POT);


    PotionConfig(int slot, ShopItem item) {
        this.slot = slot;
        this.item = item;
    }

    private final int slot;
    private final ShopItem item;

    public int getSlot() {
        return slot;
    }

    public ShopItem getItem() {
        return item;
    }
}
