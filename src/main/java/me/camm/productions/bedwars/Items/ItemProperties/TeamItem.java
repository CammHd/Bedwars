package me.camm.productions.bedwars.Items.ItemProperties;

import org.bukkit.ChatColor;
import org.bukkit.Material;


/*
 * @author CAMM
 * This enum is for all of the team items in the team buy inventories
 */
public enum TeamItem {
    TRAP_ALARM(Material.REDSTONE_TORCH_ON, new String[]{ChatColor.WHITE+"Alarm trap"}, new String[]{"Reveals invisible players.","Costs 1, 2, or 3 diamonds depending on trap slots active"}, new int[]{1,2,3}, Material.DIAMOND,true),
    TRAP_OFFENSIVE(Material.FEATHER, new String[]{ChatColor.WHITE+"Counter-Offensive trap"}, new String[]{"Give speed 2 and jump 2 to allied players near the base.","Costs 1, 2, or 3 diamonds depending on trap slots active"},new int[]{1,2,3}, Material.DIAMOND,true),
    TRAP_SIMPLE(Material.TRIPWIRE_HOOK, new String[]{ChatColor.WHITE+"It's a trap!"}, new String[]{"Inflict blindness and slowness for 8 secs.","Costs 1, 2, or 3 diamonds depending on trap slots active"}, new int[]{1,2,3},Material.DIAMOND,true),
    TRAP_MINER_SLOW(Material.IRON_PICKAXE, new String[]{ChatColor.WHITE+"Mining-Fatigue trap"}, new String[]{"Gives mining fatigue to enemies when triggered","Costs 1, 2, or 3 diamonds depending on trap slots active"}, new int[]{1,2,3},Material.DIAMOND,true),

    BUFF_HASTE(Material.GOLD_PICKAXE, new String[]{ChatColor.WHITE+"Maniac Miner"}, new String[]{"Gives haste to team members","Haste 1: 2 diamonds","Haste 2: 4 diamonds"}, new int[]{2,4},Material.DIAMOND,false),
    BUFF_BASE_REGEN(Material.BEACON, new String[]{ChatColor.WHITE+"Heal pool"}, new String[]{"Create a regeneration field around the base","Cost 1 diamonds"},  new int[]{1},Material.DIAMOND,false),
    BUFF_DRAGONS(Material.DRAGON_EGG, new String[]{ChatColor.WHITE+"Dragon Buff"}, new String[]{"Have 2 dragons instead of 1 at sudden death.","Cost 5 diamonds"},  new int[]{5},Material.DIAMOND,false),

    UPGRADE_PROTECTION(Material.IRON_CHESTPLATE, new String[]{ChatColor.WHITE+"Reinforced Armor"}, new String[]{"Adds protection to your armor","Prot 1 - 2 diamonds","Prot 2 - 4 diamonds","Prot 3 - 8 diamonds","Prot 4 - 16 diamonds" }, new int[]{2,4,8,16},Material.DIAMOND,false),

    UPGRADE_SWORDS(Material.IRON_SWORD, new String[]{ChatColor.WHITE+"Sharpened Swords"}, new String[]{"Adds sharpness to your swords","Cost 4 diamonds"}, new int[]{4},Material.DIAMOND,false),

    UPGRADE_FORGE(Material.FURNACE, new String[]{ChatColor.WHITE+"Forge Upgrade"}, new String[]{"Upgrade resource creation on your island.",
            "T1: +50% resources - 2 diamonds", "T2: +100% resources - 4 diamonds", "T3: Spawn emeralds - 6 diamonds",
            "T4: 200% resources - 8 diamonds"},new int[]{2,4,6,8}, Material.DIAMOND,false),

    SLOT_BARRIER(Material.STAINED_GLASS_PANE, new String[]{ChatColor.GRAY+"⇧ Upgrades"+ChatColor.DARK_GRAY+" ⇩ Trap Slots"}, new String[]{}, new int[]{-2},Material.AIR,false),

    SLOT_TRAP_ONE(Material.STAINED_GLASS, new String[]{ChatColor.WHITE+"Trap slot 1"}, new String[]{"This is a slot for a trap."}, new int[]{-1}, Material.AIR,false),
    SLOT_TRAP_TWO(Material.STAINED_GLASS, new String[]{ChatColor.WHITE+"Trap slot 2"}, new String[]{"This is a slot for a trap."}, new int[]{-1}, Material.AIR,false),
    SLOT_TRAP_THREE(Material.STAINED_GLASS, new String[]{ChatColor.WHITE+"Trap slot 3"}, new String[]{"This is a slot for a trap."},new int[]{-1}, Material.AIR,false);

    /*


==============
Iron/Gold/Emerald/Molten forge - Upgrade resource spawing on your island.
T1: +50% resources - 2 diamonds
T2: +100% resources - 4 diamonds
T3: Spawn emeralds - 6 diamonds
T4: 200% resources - 8 diamonds
furnace

Reinforced armor I-->IV Your team permanently gains protection on all armor pieces
T1: Prot 1: 2 d
T2: prot 2: 4 d
T3: Prot 3: 8 d
T4: prot 4: 16 d
Iron chestplate

Maniac Miner I/II  - All players permanentl gain haste.
T1: haste 1: 2 d
T2: haste 2: 4 d
Gold pick

     */

    TeamItem(Material mat, String[] names, String[] lore, int[] costs, Material costMat, boolean renewable) {
        this.mat = mat;
        this.names = names;
        this.lore = lore;
        this.costMat = costMat;
        this.cost = costs;
        this.renewable = renewable;


    }

    private final Material mat;
    private final String[] names;
    private final String[] lore;
    private final Material costMat;
    private final int[] cost;
    private final boolean renewable;


    public Material getMat() {
        return mat;
    }

    public String[] getNames() {
        return names;
    }

    public String[] getLore() {
        return lore;
    }

    public Material getCostMat() {
        return costMat;
    }

    public int[] getCost() {
        return cost;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public String format(){
        return names[0];
    }

    @Override
    public String toString(){
        return this.name();
    }
}
