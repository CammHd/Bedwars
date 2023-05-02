package me.camm.productions.bedwars.Items.ItemDatabases;


import org.bukkit.ChatColor;
import org.bukkit.Material;

import static me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory.*;

/**
 * @author CAMM
 * This enum represents all of the buyable items in the shops (quick buy shops)
 */
public enum ShopItem
{

    WOOL(4, 16, Material.WOOL, Material.IRON_INGOT, ChatColor.GRAY + "Wool", BLOCK, false, false, 4),
    LADDER(4, 16, Material.LADDER, Material.IRON_INGOT, ChatColor.GRAY + "Ladder", BLOCK, false, false, 4),
    STAINED_GLASS(12, 4, Material.STAINED_GLASS, Material.IRON_INGOT, ChatColor.GRAY + "Blast Resistant Glass", BLOCK, true, false, 12),
    HARDENED_CLAY(16, 24, Material.STAINED_CLAY, Material.IRON_INGOT, ChatColor.GRAY + "Hardened Clay", BLOCK, false, false, 16),
    ENDER_STONE(24, 12, Material.ENDER_STONE, Material.IRON_INGOT, ChatColor.GRAY + "End Stone", BLOCK, false, false, 24),
    POPUP_TOWER(24, 1, Material.CHEST, Material.IRON_INGOT, ChatColor.AQUA + "Compact Popup Tower", UTILITY, true, false, 24),
    FIREBALL(40, 1, Material.FIREBALL, Material.IRON_INGOT, ChatColor.GOLD + "Fireball", UTILITY, true, false, 40),

    BEDBUG(30, 1, Material.SNOW_BALL, Material.IRON_INGOT, ChatColor.AQUA + "BedBug", UTILITY, true, false, 30),
    DREAM_DEFENDER(120, 1, Material.MONSTER_EGG, Material.IRON_INGOT, ChatColor.GOLD + "Dream Defender", UTILITY, true, false, 120),

    GOLDEN_APPLE(3, 1, Material.GOLDEN_APPLE, Material.GOLD_INGOT, ChatColor.GRAY + "Golden Apple", UTILITY, false, false, 3),
    WATER(3, 1, Material.WATER_BUCKET, Material.GOLD_INGOT, ChatColor.GRAY + "Water Bucket", UTILITY, false, false, 6),
    SPONGE(3, 6, Material.SPONGE, Material.GOLD_INGOT, ChatColor.GRAY + "Sponge", UTILITY, false, false, 6),
    MILK(4, 1, Material.MILK_BUCKET, Material.GOLD_INGOT, ChatColor.AQUA + "Magic Milk", UTILITY, true, false, 4),
    TNT(4, 1, Material.TNT, Material.GOLD_INGOT, ChatColor.RED + "TNT", UTILITY, false, false, 8),
    PLANKS(4, 16, Material.WOOD, Material.GOLD_INGOT, ChatColor.GRAY + "Planks", BLOCK, false, false, 4),
    STICK(5, 1, Material.STICK, Material.GOLD_INGOT, ChatColor.GOLD + "Knockback I stick", MELEE, false, false, 5),


    SHEARS(20, 1, Material.SHEARS, Material.IRON_INGOT, ChatColor.GRAY + "Permanent Shears", TOOLS, false, true, 20),


    WOODEN_PICKAXE(10, 1, Material.WOOD_PICKAXE, Material.IRON_INGOT, ChatColor.GRAY + "Wooden Pickaxe (Efficiency I)", TOOLS, false, true, 10),
    IRON_PICKAXE(3, 1, Material.IRON_PICKAXE, Material.IRON_INGOT, ChatColor.AQUA + "Iron Pickaxe (Efficiency II)", TOOLS, false, false, 10),
    GOLD_PICKAXE(3, 1, Material.GOLD_PICKAXE, Material.GOLD_INGOT, ChatColor.AQUA + "Golden Pickaxe (Efficiency III, Sharpness II)", TOOLS, false, false, 3),
    DIAMOND_PICKAXE(6, 1, Material.DIAMOND_PICKAXE, Material.GOLD_INGOT, ChatColor.AQUA + "Diamond Pickaxe (Efficiency III)", TOOLS, false, false, 6),

    WOOD_AXE(10, 1, Material.WOOD_AXE, Material.IRON_INGOT, ChatColor.GRAY + "Wood Axe (Efficiency I)", TOOLS, false, true, 10),
    STONE_AXE(10, 1, Material.STONE_AXE, Material.IRON_INGOT, ChatColor.GRAY + "Stone Axe (Efficiency I)", TOOLS, false, false, 10),
    IRON_AXE(3, 1, Material.IRON_AXE, Material.GOLD_INGOT, ChatColor.GRAY + "Iron Axe (Efficiency II)", TOOLS, false, false, 3),
    DIAMOND_AXE(6, 1, Material.DIAMOND_AXE, Material.GOLD_INGOT, ChatColor.GRAY + "Diamond Axe (Efficiency III)", TOOLS, false, false, 6),

    WOODEN_SWORD(0, 1, Material.WOOD_SWORD, Material.AIR, ChatColor.GRAY + "Wooden Sword", MELEE, false, true, 0),
    STONE_SWORD(10, 1, Material.STONE_SWORD, Material.IRON_INGOT, ChatColor.GRAY + "Stone Sword", MELEE, true, false, 10),
    IRON_SWORD(7, 1, Material.IRON_SWORD, Material.GOLD_INGOT, ChatColor.GOLD + "Iron Sword", MELEE, false, false, 7),
    DIAMOND_SWORD(4, 1, Material.DIAMOND_SWORD, Material.EMERALD, ChatColor.AQUA + "Diamond Sword", MELEE, false, false, 3),

    BOW(12, 1, Material.BOW, Material.GOLD_INGOT, ChatColor.GRAY + "Bow", RANGED, false, false, 12),
    POW_BOW(24, 1, Material.BOW, Material.GOLD_INGOT, ChatColor.GOLD + "Power I Bow", RANGED, false, false, 24),
    ARROW(2, 8, Material.ARROW, Material.GOLD_INGOT, ChatColor.GRAY + "Arrow", RANGED, false, false, 2),
    PUNCH_BOW(6, 1, Material.BOW, Material.EMERALD, ChatColor.GRAY + "Punch I Power 1 Bow", RANGED, false, false, 6),

    LEATHER_ARMOR(0, 1, Material.LEATHER_BOOTS, Material.AIR, ChatColor.GRAY + "Leather armor", ARMOR, false, true, 0),
    CHAIN_MAIL(30, 1, Material.CHAINMAIL_BOOTS, Material.IRON_INGOT, ChatColor.GOLD + "Permanent Chain mail Armor", ARMOR, false, true, 30),
    IRON_ARMOR(12, 1, Material.IRON_BOOTS, Material.GOLD_INGOT, ChatColor.GRAY + "Permanent Iron Armor", ARMOR, false, true, 12),
    DIAMOND_ARMOR(4, 1, Material.DIAMOND_BOOTS, Material.EMERALD, ChatColor.GRAY + "Permanent Diamond Armor", ARMOR, true, true, 4),

    BRIDGE_EGG(2, 1, Material.EGG, Material.EMERALD, ChatColor.GOLD + "Bridge Egg", UTILITY, true, false, 1),
    SPEED_POT(1, 1, Material.POTION, Material.EMERALD, ChatColor.DARK_AQUA + "Speed II Potion (45 seconds) ", POTION, true, false, 1),
    JUMP_POT(1, 1, Material.POTION, Material.EMERALD, ChatColor.DARK_AQUA + "Jump V Potion (45 seconds)", POTION, true, false, 1),
    INVIS_POT(1, 1, Material.POTION, Material.EMERALD, ChatColor.DARK_AQUA + "Invisibility Potion (30 Seconds)", POTION, true, false, 1),
    ENDER_PEARL(4, 1, Material.ENDER_PEARL, Material.EMERALD, ChatColor.GRAY + "Ender Pearl", UTILITY, false, false, 4),
    OBSIDIAN(4, 4, Material.OBSIDIAN, Material.EMERALD, ChatColor.GRAY + "Obsidian", BLOCK, false, false, 4),

    //team items

    TRACKER_ITEM(2,1, Material.COMPASS, Material.EMERALD, ChatColor.AQUA+"Tracker and Communications",TRACKER, true, false, 2),

    //Navigational and utility items
    SEPARATOR(1, 1, Material.STAINED_GLASS_PANE, Material.AIR, ChatColor.DARK_GRAY+"\u21e7 Categories "+ChatColor.GRAY+"\u21e9 Items", ItemCategory.SEPARATOR, false, false, 0),
    EMPTY_SLOT(1, 1, Material.STAINED_GLASS_PANE, Material.STAINED_GLASS_PANE, ChatColor.GOLD + "Empty Slot", NONE, true, false, 0),
    TRACKER_NAV(1,1,Material.COMPASS,Material.AIR,ChatColor.GOLD+"Tracker",NAV,false, false, 0),
    BLOCKS_NAV(1, 1, Material.HARD_CLAY, Material.AIR, ChatColor.DARK_GREEN+"Blocks", NAV, false, false, 0),
    MELEE_NAV(1, 1, Material.GOLD_SWORD, Material.AIR, ChatColor.DARK_AQUA+"Melee", NAV, false, false, 0),
    ARMOR_NAV(1, 1, Material.CHAINMAIL_BOOTS, Material.AIR, ChatColor.YELLOW+"Armor", NAV, false, false, 0),
    TOOLS_NAV(1, 1, Material.STONE_PICKAXE, Material.AIR, ChatColor.GRAY+"Tools", NAV, false, false, 0),
    RANGED_NAV(1, 1, Material.BOW, Material.AIR, ChatColor.GOLD+"Ranged", NAV, false, false, 0),
    POTIONS_NAV(1, 1, Material.BREWING_STAND_ITEM, Material.AIR, ChatColor.DARK_AQUA+"Potions", NAV, false, false, 0),
    UTILITY_NAV(1, 1, Material.TNT, Material.AIR, ChatColor.RED+"Utility", NAV, false, false, 0),
    HOTBAR_NAV(1, 1, Material.BLAZE_POWDER, Material.AIR, ChatColor.GOLD+"Hotbar Manager", NAV, false, false, 0),
    HOME_NAV(1, 1, Material.NETHER_STAR, Material.AIR, ChatColor.GREEN + "Home", NAV, false, false, 0),
   ;

    public final int cost;
    public final int sellAmount;
    public final Material sellMaterial;
    public final Material costMaterial;
    public final String name;
    public final ItemCategory category;
    public final boolean keepName;

    public final boolean isPermanent;
    public final int inflatedPrice;


    ShopItem(int cost, int sellAmount, Material sellMaterial, Material costMaterial, String name, ItemCategory category, boolean keepName, boolean isPermanent, int inflatedPrice) {
        this.cost = cost;
        this.sellAmount = sellAmount;
        this.sellMaterial = sellMaterial;
        this.costMaterial = costMaterial;
        this.name = name;
        this.category = category;
        this.keepName = keepName;
        this.isPermanent = isPermanent;
        this.inflatedPrice = inflatedPrice;
    }

    public String format(){
        return ChatColor.GOLD+(ChatColor.RESET+name);
    }
}