package me.camm.productions.bedwars.Items.SectionInventories.Inventories;


import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.RangedConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import org.bukkit.inventory.Inventory;

/**
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
public class RangedSectionInventory extends ShopInventory {

    public RangedSectionInventory(boolean isInflated, Arena arena)
    {
        super(null, InventoryProperty.SHOP_SIZE.getValue(), InventoryName.RANGED.getTitle(),isInflated, arena);
        setTemplate(isInflated,false);
        setInventoryItems();
    }

    @Override
    public void setInventoryItems()
    {
        for (RangedConfig config: RangedConfig.values())
            setItem(config.getSlot(),config.getItem(),isInflated);
    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }


}

