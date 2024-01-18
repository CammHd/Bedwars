package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.ItemProperties.ItemCategory;
import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;


/*
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum HotBarConfig
{
    RETURN(Material.ARROW, ChatColor.AQUA+"Return to Quick Buy", ItemCategory.OPERATOR, new int[]{3}),
    RESET(Material.BARRIER, ChatColor.GOLD+"Reset Configuration", ItemCategory.OPERATOR, new int[]{5}),
    SEPARATOR(ShopItem.SEPARATOR.sellMaterial, ChatColor.DARK_GRAY+"\u21e7 Categories "+ChatColor.GRAY+"\u21e9 Configuration",null, new int[]{27,28,29,30,31,32,33,34,35}),
    BLOCK(ShopItem.BLOCKS_NAV.sellMaterial,ShopItem.BLOCKS_NAV.name,ItemCategory.BLOCK, new int[]{InventoryProperty.LARGE_ROW_THREE_START.getValue()+1}),
    MELEE(ShopItem.MELEE_NAV.sellMaterial, ShopItem.MELEE_NAV.name, ItemCategory.MELEE, new int[]{InventoryProperty.LARGE_ROW_THREE_START.getValue()+2}),
    TOOLS(ShopItem.TOOLS_NAV.sellMaterial,ShopItem.TOOLS_NAV.name, ItemCategory.TOOLS, new int[]{InventoryProperty.LARGE_ROW_THREE_START.getValue()+3}),
    RANGED(ShopItem.RANGED_NAV.sellMaterial,ShopItem.RANGED_NAV.name, ItemCategory.RANGED, new int[]{InventoryProperty.LARGE_ROW_THREE_START.getValue()+4}),
    POTION(ShopItem.POTIONS_NAV.sellMaterial,ShopItem.POTIONS_NAV.name, ItemCategory.POTION, new int[]{InventoryProperty.LARGE_ROW_THREE_START.getValue()+5}),
    UTILITY(ShopItem.UTILITY_NAV.sellMaterial,ShopItem.UTILITY_NAV.name, ItemCategory.UTILITY, new int[]{InventoryProperty.LARGE_ROW_THREE_START.getValue()+6}),
    TRACKER(ShopItem.TRACKER_NAV.sellMaterial, ShopItem.TRACKER_NAV.name, ItemCategory.TRACKER,new int[]{InventoryProperty.LARGE_ROW_THREE_START.getValue()+7})
    ;

    private final Material mat;
    private final String name;
    private final ItemCategory category;
    final int[] slots;

    HotBarConfig(Material mat, String name, ItemCategory category, int[] slots) {
        this.mat = mat;
        this.name = name;
        this.category = category;
        this.slots = slots;
    }

    public Material getMat() {
        return mat;
    }

    public String getName() {
        return name;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public int[] getSlots() {
        return slots;
    }

    //return                      reset
    //arrow: return to quick buy, barrier: reset to default
}
