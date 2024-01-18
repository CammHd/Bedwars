package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.ResourceConfig;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamOptionConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/*
This inventory gives players options for the quick chat inventory.

 */
public class ChatOptionSelectionInventory extends InventoryOptionable {


    private final BattlePlayer owner;
    private boolean showingTeams;

    private static final int END = InventoryProperty.LARGE_ROW_TWO_END.getValue();
    private final Map<String, BattleTeam> teams;
    private final Map<String, ResourceConfig> config;





    public ChatOptionSelectionInventory(Arena arena, BattlePlayer owner) {
        super(arena, InventoryName.SELECT_OPTION.getTitle());
        this.owner = owner;
        teams = new HashMap<>();
        config = new HashMap<>();
        for (BattleTeam team: arena.getTeams().values()) {
            teams.put(team.getTeamColor().format(), team);
        }

        for (ResourceConfig resources: ResourceConfig.values()) {
            config.put(resources.getName(), resources);
        }

        loadTeams();

    }


    public void loadTeams(){


        showingTeams = true;
        BattleTeam friendly = owner.getTeam();
        for (BattleTeam team: arena.getTeams().values()) {
            if (team.isEliminated())
                super.removeEntry(team);
            else if (!friendly.equals(team))
                super.addEntry(team);
        }

        super.updateInventory();
    }

    public void loadResources(){

        showingTeams = false;
        int resSlot = 0;
        ResourceConfig[] resources = ResourceConfig.values();
        for (int slot = (ROW+1); slot <= END;slot++)
        {
            setItem(slot,null);

            if (resSlot < resources.length)
                setItem(slot, new ItemStack(resources[resSlot].create()));
            resSlot ++;
        }
    }

    @Override
    public void operate(InventoryClickEvent event) {

        UUID id = event.getWhoClicked().getUniqueId();
        BattlePlayer battlePlayer = arena.getPlayers().getOrDefault(id, null);

        if (battlePlayer == null)
            return;

        InventoryOperationHelper.handleDefaultRestrictions(event, arena);
        if (InventoryOperationHelper.triedToPlaceIn(event, this))
            event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (ItemHelper.isItemInvalid(clicked))
            return;

        if (!event.getClickedInventory().equals(this))
            return;

        ItemMeta meta = clicked.getItemMeta();
        String display = meta.getDisplayName();


        if (display == null)
            return;


        event.setCancelled(true);
        if (showingTeams) {

            BattleTeam team = teams.getOrDefault(display, null);
            if (team == null) {

                if (isReturn(clicked)) {
                    battlePlayer.getRawPlayer().openInventory((Inventory) arena.getChatInv());
                }

                return;

            }


            handleResult(battlePlayer, team);
        }
        else {
            ResourceConfig currentResource = config.getOrDefault(display, null);
            if (currentResource == null) {

                if (isReturn(clicked)) {
                    battlePlayer.getRawPlayer().openInventory((Inventory) arena.getChatInv());
                }


                return;
            }

           handleResult(battlePlayer, currentResource);
        }






    }



    private boolean isReturn(ItemStack stack){
        return stack.equals(TeamOptionConfig.RETURN.create());
    }


    //these methods will handle the results of the click by calling back to the chat inventory to
    //determine the first half of a chat message to the player


    @Override
    protected void handleResult(BattlePlayer player, BattleTeam preference) {
        QuickChatInventory.complete(player.getUUID(), preference.getTeamColor().format());
    }

    @Override
    protected void handleResult(BattlePlayer player, ResourceConfig config) {
        QuickChatInventory.complete(player.getUUID(), config.getName());

    }
}
