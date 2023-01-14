package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import com.google.common.collect.HashBasedTable;
import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.QuickChatConfig;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamOptionConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import me.camm.productions.bedwars.Util.PacketSound;
import net.minecraft.server.v1_8_R3.Tuple;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuickChatInventory extends CraftInventoryCustom implements IGameInventory {

    private final Arena arena;
    private static final Map<UUID, Tuple<String, BattlePlayer>> playerOptions = new HashMap<>();
    private HashBasedTable<String, Material, QuickChatConfig> configItems;
    public QuickChatInventory(Arena arena) {
        super(null, InventoryProperty.LARGE_SHOP_SIZE.getValue(), InventoryName.QUICK_CHAT.getTitle());
        this.arena = arena;
        init();
    }


    private void init(){

        configItems = HashBasedTable.create();

        for (QuickChatConfig config: QuickChatConfig.values()) {

            Material mat = config.getMat();
            String name = config.getItemName();

            configItems.put(name, mat, config);

            ItemStack stack = ItemHelper.toSimpleItem(mat, name);
            if (config.getLore()!= null) {
                ItemHelper.addLore(stack, config.getLore());
            }
            setItem(config.getSlot(), stack);
        }

        setItem(TeamOptionConfig.RETURN.getSlots()[0],TeamOptionConfig.RETURN.create());

    }

    public static void complete(UUID id, String completion) {

        Tuple<String, BattlePlayer> set = playerOptions.getOrDefault(id, null);
        if (set == null)
            return;

        BattlePlayer player = set.b();
        String finish = set.a();

        BattleTeam team = player.getTeam();
        Player raw = player.getRawPlayer();
       raw.closeInventory();
        team.sendTeamMessage(team.getTeamColor().getChatColor()+raw.getName()+
                ChatColor.GOLD+" says "+ finish + completion);

        player.getTeam().sendTeamSoundPacket(PacketSound.DING);

    }


    private void send(BattlePlayer player, String message){
        BattleTeam team = player.getTeam();
        Player raw = player.getRawPlayer();
        raw.closeInventory();
        team.sendTeamMessage(team.getTeamColor().getChatColor()+raw.getName()+
                ChatColor.GOLD+" says "+message);

        player.getTeam().sendTeamSoundPacket(PacketSound.DING);
    }


    public static void add(BattlePlayer player, String clicked){
        playerOptions.put(player.getUUID(), new Tuple<>(clicked, player));
    }





    @Override
    public boolean equals(Inventory other) {
        return super.equals(other);
    }

    @Override
    public void operate(InventoryClickEvent event) {

        if (InventoryOperationHelper.triedToPlaceIn(event, this)) {
            event.setCancelled(true);
            return;
        }

        BattlePlayer battlePlayer = arena.getPlayers().getOrDefault(event.getWhoClicked().getUniqueId(), null);

        if (battlePlayer == null)
            return;


       ItemStack clicked = event.getCurrentItem();
        if (ItemHelper.isItemInvalid(clicked)) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        String name = meta.getDisplayName();
        Material type = clicked.getType();

        if (type == null)
            return;

        if (name == null)
            return;

        if (!configItems.contains(name, type)) {

            if (clicked.equals(TeamOptionConfig.RETURN.create())) {
                event.setCancelled(true);
                battlePlayer.getRawPlayer().openInventory((Inventory)arena.getSelectionInv());
            }
            return;
        }
        ///check if it is a return to selection inv


        QuickChatConfig option = configItems.get(name, type);


        event.setCancelled(true);


        if (option.allowsOptions()) {
            add(battlePlayer,option.getMessage());

            //open an inventory here

            ChatOptionSelectionInventory optionInv = battlePlayer.getChatOptionInv();
            if (option.isTeamRelated()) {
               optionInv.loadTeams();
                ///populate with team stuff
            }
            else  {
                //populate with resource stuff
               optionInv.loadResources();
            }


            battlePlayer.getRawPlayer().openInventory(optionInv);

            return;
        }

        send(battlePlayer, option.getMessage());

        ////
    }

    @Override
    public void operate(InventoryDragEvent event) {

        if (InventoryOperationHelper.didTryToDragIn(event, this)) {
            event.setCancelled(true);

            return;
        }
        InventoryOperationHelper.handleDefaultRestrictions(event, arena);
    }
}
