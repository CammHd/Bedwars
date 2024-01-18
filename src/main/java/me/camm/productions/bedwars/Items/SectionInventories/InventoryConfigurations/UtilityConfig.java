package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_ROW2_START;
import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_START;


/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum UtilityConfig
{
    APPLE(QUICK_INV_BORDER_START.getValue(), ShopItem.GOLDEN_APPLE),
    BEDBUG(QUICK_INV_BORDER_START.getValue()+1, ShopItem.BEDBUG),
    WATER(QUICK_INV_BORDER_START.getValue()+2, ShopItem.WATER),
    DREAM_DEFENDER(QUICK_INV_BORDER_START.getValue()+3, ShopItem.DREAM_DEFENDER),
    FIREBALL(QUICK_INV_BORDER_START.getValue()+4, ShopItem.FIREBALL),
    TNT(QUICK_INV_BORDER_START.getValue()+5, ShopItem.TNT),
    ENDER_PEARL(QUICK_INV_BORDER_START.getValue()+6, ShopItem.ENDER_PEARL),


    //2ND ROW
    BRIDGE_EGG(QUICK_INV_BORDER_ROW2_START.getValue(), ShopItem.BRIDGE_EGG),
    MILK(QUICK_INV_BORDER_ROW2_START.getValue()+1, ShopItem.MILK),
    SPONGE(QUICK_INV_BORDER_ROW2_START.getValue()+2, ShopItem.SPONGE),
    TOWER(QUICK_INV_BORDER_ROW2_START.getValue()+3, ShopItem.POPUP_TOWER);




    private final int slot;
    private final ShopItem item;

    UtilityConfig(int slot, ShopItem item) {
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
