package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;


/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum DefaultQuickItemConfig
{

    //the default configuration of the items in the quick buy inventory.
    // See defaultQuickTemplate enum items for separators, empty, and nav.
    WOOL(ShopItem.WOOL,new int[]{19}),
    STONE_SWORD(ShopItem.STONE_SWORD,new int[] {20}),
    CHAIN(ShopItem.CHAIN_MAIL,new int[] {21}),
    ARROW(ShopItem.ARROW,new int[] {23}),
    SPEED_POT(ShopItem.SPEED_POT,new int[] {24}),
    TNT(ShopItem.TNT,new int[] {25}),
    PLANKS(ShopItem.PLANKS,new int[] {28}),
    IRON_SWORD(ShopItem.IRON_SWORD,new int[] {29}),
    IRON_ARMOR(ShopItem.IRON_ARMOR,new int[] {30}),
    SHEARS(ShopItem.SHEARS,new int[] {31}),
    BOW(ShopItem.BOW,new int[] {32}),
    INVIS_POT(ShopItem.INVIS_POT,new int[] {33}),
    //TRACKER(ShopItem.TRACKER_NAV,new int[]{45}),
    WATER(ShopItem.WATER,new int[] {34});


    private final ShopItem item;
    private final int[] slots;

    DefaultQuickItemConfig(ShopItem item, int[] slot)
    {
        this.item = item;
        this.slots = slot;
    }

    public int[] getSlots()
    {
        return slots;
    }

    public ShopItem getItem()
    {
        return item;
    }
}
