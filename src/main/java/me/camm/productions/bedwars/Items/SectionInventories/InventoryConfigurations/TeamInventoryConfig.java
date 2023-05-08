package me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations;

import me.camm.productions.bedwars.Items.ItemProperties.TeamItem;
import static me.camm.productions.bedwars.Items.ItemProperties.TeamItem.*;


/**
 * @author CAMM
 * This enum is used as a template for placing items into section inventories
 */
public enum TeamInventoryConfig
{
    ALARM_TRAP(new int[]{16},TRAP_ALARM),
    OFFENSE_TRAP(new int[]{15},TRAP_OFFENSIVE),
    SIMPLE_TRAP(new int[]{14},TRAP_SIMPLE),
    MINER_TRAP(new int[]{23},TRAP_MINER_SLOW),

    HASTE_BUFF(new int[]{12},BUFF_HASTE),
    REGEN_FIELD(new int[]{20},BUFF_BASE_REGEN),
    DRAGON(new int[]{21},BUFF_DRAGONS),

    PROTECTION(new int[]{11},UPGRADE_PROTECTION),
    SWORDS(new int[]{10},UPGRADE_SWORDS),
    FORGE(new int[]{19},UPGRADE_FORGE),

    BARRIER(new int[]{27,28,29,30,31,32,33,34,35},SLOT_BARRIER),

    TRAP_ONE(new int[]{39},SLOT_TRAP_ONE),
    TRAP_TWO(new int[]{40},SLOT_TRAP_TWO),
    TRAP_THREE(new int[]{41},SLOT_TRAP_THREE);


TeamInventoryConfig(int[] slots, TeamItem items)
{
    this.slots = slots;
    this.items = items;
}

private final int[] slots;
private final TeamItem items;

    public int[] getSlots() {
        return slots;
    }

    public TeamItem getItems() {
        return items;
    }
}
