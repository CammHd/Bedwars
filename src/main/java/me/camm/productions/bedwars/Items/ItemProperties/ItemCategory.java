package me.camm.productions.bedwars.Items.ItemProperties;

import org.bukkit.ChatColor;

/*
 * @author CAMM
 * This enum is used for keeping track of item categories in the game
 *
 */
public enum ItemCategory
{
    NONE(ChatColor.AQUA+"Empty Slot", ShopItem.EMPTY_SLOT),
    BLOCK(ChatColor.AQUA+"Blocks Section", ShopItem.BLOCKS_NAV),
    MELEE(ChatColor.AQUA+"Melee Section", ShopItem.MELEE_NAV),
    ARMOR(ChatColor.AQUA+"Armor Section",ShopItem.ARMOR_NAV),
    TOOLS(ChatColor.AQUA+"Tools Section",ShopItem.TOOLS_NAV),
    RANGED(ChatColor.AQUA+"Ranged Section",ShopItem.RANGED_NAV),
    POTION(ChatColor.AQUA+"Potion",ShopItem.POTIONS_NAV),
    UTILITY(ChatColor.AQUA+"Utility",ShopItem.UTILITY_NAV),
    NAV(ChatColor.GOLD+"Navigation",null),
    OPERATOR(ChatColor.GOLD+"Operator",null),
    TRACKER("Tracker",ShopItem.TRACKER_ITEM),
    SEPARATOR(ChatColor.GRAY+"Separator",null);

    public final String type;
    public final ShopItem associate;
   ItemCategory(String type, ShopItem associate)
    {
        this.type = type;
        this.associate = associate;
    }

    @Override
    public String toString()
    {
        return this.name();
    }



}

    /*
    BLOCK
MELEE
ARMOR
TOOLS
RANGED
POTION
UTILITY
     */

