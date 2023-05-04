package me.camm.productions.bedwars.Listeners;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Entities.ShopKeeper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutRemoveEntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.UUID;

import static me.camm.productions.bedwars.Util.Helpers.ItemHelper.isArmor;

public class BedwarsDuplexHandler extends ChannelDuplexHandler {
    private long time = System.currentTimeMillis();
    private final Arena arena;
    private final Player player;

    private final PacketHandler handler;

    public BedwarsDuplexHandler(Player player, Arena arena, PacketHandler handler){
        this.arena = arena;
        this.player = player;
        this.handler = handler;
    }

    //reading packets sent from player
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception
    {

        if (!(o instanceof PacketPlayInUseEntity)) {
            super.channelRead(channelHandlerContext, o);
            return;
        }

            System.out.println("On channel read uuid="+arena.getDebugUUID());

            if (!arena.getPlayers().containsKey(player.getUniqueId()))
            {
                super.channelRead(channelHandlerContext, o);
                return;
            }

            String action = accessValue(o, "action").toString();


            if (action.equalsIgnoreCase("ATTACK")) {
                super.channelRead(channelHandlerContext, o);
                return;
            }

            if (action.equalsIgnoreCase("INTERACT_AT")) {
                super.channelRead(channelHandlerContext, o);
                return;
            }

            int id = (int) accessValue(o, "a");
            if (id==-1)
            {
                super.channelRead(channelHandlerContext, o);
                return;
            }

            ShopKeeper clicked = null;
            for (ShopKeeper keeper: handler.getKeepers())
            {
                if (id==keeper.getId())
                {
                    clicked = keeper;
                    break;
                }
            }

            if (clicked==null)
            {
                super.channelRead(channelHandlerContext, o);
                return;
            }

            BattlePlayer openingPlayer = arena.getPlayers().get(player.getUniqueId());

            if (!openingPlayer.getIsAlive()) {
                super.channelRead(channelHandlerContext, o);
                return;
            }
//
//            InventoryView view = openingPlayer.getRawPlayer().getOpenInventory();
//
//
//            if (view != null && !view.getTopInventory().getType().equals(InventoryType.CRAFTING)) {
//                super.channelRead(channelHandlerContext, o);
//                return;
//            }

        //interaction delay so that the player doesn't accidently mass-click the NPC
        //this also prevents a CanceledPacketHandleException
        final int INTERACT_DELAY = 500;
        if (System.currentTimeMillis() - time < INTERACT_DELAY) {
                super.channelRead(channelHandlerContext, o);
                return;
            }


            if (clicked.getIsTeamKeeper())
                player.openInventory((Inventory)openingPlayer.getTeam().getTeamInventory());
            else
                player.openInventory(openingPlayer.getShopManager().getQuickBuy());

            time = System.currentTimeMillis();





    }

    //writing packets to a player.
    //So this is where we discriminate and see which packets we intercept, and which ones we write.
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {


                /*
                If we send a packet to the invis player that voids all of their items, then
                add the invis player to the handler, then
                resend a packet that gives them back, technically that should work right?
                 */


        if (o instanceof PacketPlayOutEntityEquipment)
        {
            try
            {
                Field entityId = PacketPlayOutEntityEquipment.class.getDeclaredField("a");
                entityId.setAccessible(true);
                int value = (int)entityId.get(o);
                entityId.setAccessible(false);

                Field item = PacketPlayOutEntityEquipment.class.getDeclaredField("c");
                item.setAccessible(true);
                net.minecraft.server.v1_8_R3.ItemStack stack = (net.minecraft.server.v1_8_R3.ItemStack)item.get(o);

                ItemStack bukkitItem = ItemHelper.toBukkitItem(stack);


                if (handler.invisiblesContains(value)&&isArmor(bukkitItem.getType()))
                {
                    BattlePlayer invisible = arena.getPlayers().get(handler.getInvisiblePlayer(value));
                    BattlePlayer receiving = arena.getPlayers().get(player.getUniqueId());

                    //If they are not on the same team.
                    if (!invisible.getTeam().equals(receiving.getTeam()))
                        return;
                }
            }
            catch (Exception ignored)
            {

            }
        }


        if (o instanceof PacketPlayOutRemoveEntityEffect) {
            TEST_INVIS:
            {
                try {
                    PacketPlayOutRemoveEntityEffect packet = (PacketPlayOutRemoveEntityEffect) o;
                    Field potionEffect = PacketPlayOutRemoveEntityEffect.class.getDeclaredField("b");
                    potionEffect.setAccessible(true);
                    int effectId = (int) potionEffect.get(packet);
                    potionEffect.setAccessible(false);


                    final int INVISIBILITY_ID = 14;
                    if (effectId != INVISIBILITY_ID)  //the effect id for invisibility is 14
                        break TEST_INVIS;

                    Object entityId = accessValue(packet, "a");
                    if (entityId == null)
                        break TEST_INVIS;

                    int idAsInt = (Integer)entityId;

                    if (handler.invisiblesContains(idAsInt))
                        break TEST_INVIS;

                    UUID id = handler.getInvisiblePlayer(idAsInt);

                    if (!arena.getPlayers().containsKey(id))
                        break TEST_INVIS;

                    BattlePlayer currentPlayer = arena.getPlayers().get(id);
                    handler.removeInvisiblePlayer(currentPlayer.getRawPlayer());
                    currentPlayer.removeUnprocessedInvisibility();


                } catch (Exception ignored) {

                }
            }
        }





        super.write(channelHandlerContext, o, channelPromise);
    }

    /*
    Gets a field from an object with the name provided in the fieldName and returns the value obtained
     */
    public Object accessValue(Object classObject, String fieldName)
    {
        Object result;
        try
        {
            Field field = classObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            result = field.get(classObject);
            field.setAccessible(false);
            return result;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
