package me.camm.productions.bedwars.Items.SectionInventories.Inventories;


import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.UtilityConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import org.bukkit.inventory.Inventory;

/*
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
public class UtilitySectionInventory extends ShopInventory {


    public UtilitySectionInventory(boolean isInflated, Arena arena) {
        super(null, InventoryProperty.SHOP_SIZE.getValue(), InventoryName.UTILITY.getTitle(), isInflated, arena);
        setTemplate(isInflated,false);
        setInventoryItems();
    }


    @Override
    public void setInventoryItems()
    {
        for (UtilityConfig config: UtilityConfig.values())
            setItem(config.getSlot(),config.getItem(),isInflated);
    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }


}
