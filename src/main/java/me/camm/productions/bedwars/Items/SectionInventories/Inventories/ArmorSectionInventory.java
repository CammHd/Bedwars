package me.camm.productions.bedwars.Items.SectionInventories.Inventories;


import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.ArmorConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import org.bukkit.inventory.Inventory;


/**
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
//1 for each player
//this is an instance of Inventory
public class ArmorSectionInventory extends ShopInventory {

    //(InventoryHolder owner, int size, String title)
    public ArmorSectionInventory(boolean isInflated, Arena arena) {
        super(null, InventoryProperty.SHOP_SIZE.getValue(),InventoryName.ARMOR.getTitle(),isInflated,arena);
        setTemplate(isInflated,false);
        setInventoryItems();

    }

    @Override
    public void setInventoryItems()
    {
        for (ArmorConfig config: ArmorConfig.values())
            super.setItem(config.getSlot(),config.getItem(),isInflated);
        //method in InventorySetter

    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }


}
