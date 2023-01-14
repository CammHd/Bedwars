package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.BlockConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import org.bukkit.inventory.Inventory;


import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.SHOP_SIZE;
import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName.BLOCKS;


/**
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
//Universal for all players
public class BlockSectionInventory extends ShopInventory {

    public BlockSectionInventory(boolean isInflated, Arena arena)
    {
        super(null,SHOP_SIZE.getValue(),BLOCKS.getTitle(),isInflated,arena);
        setTemplate(isInflated,false);
        setInventoryItems();
    }


    @Override
    public void setInventoryItems() {
        for (BlockConfig config: BlockConfig.values())
            setItem(config.getSlot(),config.getItem(), isInflated);
    }


    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }


}

