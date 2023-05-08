package me.camm.productions.bedwars.Items.SectionInventories.Templates;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;

/**
 * @author CAMM
 * This is an interface for functionality that section inventories should have
 */
public interface ISectionInventory extends IGameInventory
{
    void setTemplate(boolean isInflated, boolean includeEmpties);
    void setInventoryItems();
    void setItem(int index, ShopItem item, boolean isInflated);
   // ArrayList<ShopItemSet> packageInventory(Inventory inv);



}
