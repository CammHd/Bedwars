package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.MeleeConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import org.bukkit.inventory.Inventory;


/**
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
//Universal for all players
public class MeleeSectionInventory extends ShopInventory {

    public MeleeSectionInventory(boolean isInflated, Arena arena) {
        super(null, InventoryProperty.SHOP_SIZE.getValue(), InventoryName.MELEE.getTitle(),isInflated,arena);
        super.setTemplate(isInflated,false);
       setInventoryItems();
    }

    @Override
    public void setInventoryItems() {
        for (MeleeConfig config: MeleeConfig.values())
            setItem(config.getSlot(),config.getItem(),isInflated);
    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }


}
