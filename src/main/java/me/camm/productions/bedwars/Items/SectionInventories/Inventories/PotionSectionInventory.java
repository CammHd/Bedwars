package me.camm.productions.bedwars.Items.SectionInventories.Inventories;


import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.PotionConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import org.bukkit.inventory.Inventory;

/**
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */

public class PotionSectionInventory extends ShopInventory {


    public PotionSectionInventory(boolean isInflated, Arena arena) {
        super(null, InventoryProperty.SHOP_SIZE.getValue(), InventoryName.POTION.getTitle(),isInflated, arena);
        super.setTemplate(isInflated,false);
        setInventoryItems();
    }

    @Override
    public void setInventoryItems()
    {
        for (PotionConfig config: PotionConfig.values())
            setItem(config.getSlot(),config.getItem(),isInflated);
    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }



}
