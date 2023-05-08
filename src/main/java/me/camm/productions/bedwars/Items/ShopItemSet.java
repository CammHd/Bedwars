package me.camm.productions.bedwars.Items;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;

/*
This class is a package for configuration of items, preserving slot and the item.
For a list of items that should not appear as the gameItem, refer to the
ItemToolBox class, method isFileRestricted(GameItem item)
 */
public class ShopItemSet
{
    private final ShopItem item;
    private final int slot;

  public ShopItemSet(ShopItem item, int slot)
  {
      this.item = ItemHelper.isFileRestricted(item) ? ShopItem.EMPTY_SLOT:item;
      this.slot = slot;
  }

  public int getSlot()
  {
      return slot;
  }

  public ShopItem getItem()
  {
      return item;
  }

  @Override
  public String toString()
  {
      return item.name() +" "+ slot;
  }
}

