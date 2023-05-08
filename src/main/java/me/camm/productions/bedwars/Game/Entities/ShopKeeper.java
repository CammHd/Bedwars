package me.camm.productions.bedwars.Game.Entities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.camm.productions.bedwars.Util.SiteInfo;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author CAMM
 * Models a shop keeper that the player buys things from
 */
public class ShopKeeper
{
    private ArmorStand shopType;
    private static final HashMap<UUID, String[]> skinValues = new HashMap<>();
    private final Plugin plugin;
    private final EntityPlayer npc;
    private final GameProfile profile;
    private final DataWatcher watcher;
    private final int id;
    private final boolean isTeamKeeper;
    private final Location spawnLocation;
    private final double yaw;

    public ShopKeeper(Player appearance, Plugin plugin, Location loc, World world, boolean isTeamKeeper, double yaw)
    {


        Chunk chunk = world.getChunkAt(loc);
        if (!chunk.isLoaded())
            chunk.load();


        this.spawnLocation = loc;
        this.yaw = yaw;
        this.plugin = plugin;
        this.isTeamKeeper = isTeamKeeper;

        profile = new GameProfile(UUID.randomUUID(), isTeamKeeper? ChatColor.BOLD+""+ChatColor.GOLD+"SHOP": ChatColor.BOLD+""+ChatColor.AQUA+"SHOP");

        new BukkitRunnable()
        {
            public void run()
            {
                shopType = world.spawn(loc.clone().add(0,2.3,0),ArmorStand.class);
                shopType.setMarker(true);
                shopType.setVisible(false);
                shopType.setGravity(false);

                shopType.setCustomName(isTeamKeeper? ChatColor.YELLOW+"TEAM UPGRADES":ChatColor.AQUA+"QUICK BUY");
                shopType.setCustomNameVisible(true);
                cancel();
            }
        }.runTask(plugin);


        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer npcWorld = ((CraftWorld)world).getHandle();

        npc = new EntityPlayer(server,npcWorld,profile, new PlayerInteractManager(npcWorld));
        this.id = npc.getId();
        npc.setLocation(loc.getX(),loc.getY(),loc.getZ(),-(loc.getPitch()),loc.getYaw());

        watcher = npc.getDataWatcher();
        watcher.watch(10,(byte)127);
        setSkin(appearance.getUniqueId());
    }

    public boolean getIsTeamKeeper()
    {
        return isTeamKeeper;
    }

    public void setSkin(UUID uuid)
    {

            String[] properties;
            if (skinValues.containsKey(uuid))
                properties = skinValues.get(uuid);
            else
               properties = getSkinInfo(uuid);

            if (properties!=null)
                profile.getProperties().put("textures", new Property("textures", properties[0], properties[1]));




    }

    public int getId()
    {
        return id;
    }



    public String[] getSkinInfo(UUID uuid)
    {
        try
        {
            URL url = new URL(SiteInfo.PROFILE_GET.getURL()+uuid.toString()+ SiteInfo.PROFILE_CAPPER.getURL());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            if (connection.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                ArrayList<String> values = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                Stream<String> lines = reader.lines();
                lines.forEach(values::add);

                String combined  = String.join(" ",values);
                int valueIndex = combined.indexOf("\"value\" :");
                int signatureIndex = combined.indexOf("\"signature\" :");

                String value = combined.substring(valueIndex+11,combined.indexOf("\"",valueIndex+11));
                String signature = combined.substring(signatureIndex+15,combined.indexOf("\"",signatureIndex+15));


                String[] data = new String[] {value,signature};
                skinValues.putIfAbsent(uuid,data);
                return data;
            }
            return null;

        }
        catch (Exception e) {
            return null;
        }
    }


    public void sendNPCToAll()
    {
        for (Player player: Bukkit.getOnlinePlayers())
            sendNPC(player);
    }

    public Location getLocation()
    {
        return spawnLocation;
    }

    public void unloadNPC(Player player)
    {
        sendPacket(player, new PacketPlayOutEntityDestroy(npc.getId()));
    }

    public void removeNPC() {
        new BukkitRunnable() {
            public void run() {
                shopType.remove();

                for (Player player: Bukkit.getOnlinePlayers())
                    unloadNPC(player);
            }
        }.runTask(plugin);
    }

    public void sendNPC(Player player)
    {
        sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,npc));
        sendPacket(player, new PacketPlayOutNamedEntitySpawn(npc));
        sendPacket(player, new PacketPlayOutEntityMetadata(npc.getId(),watcher,true));

        new BukkitRunnable()
        {
            public void run()
            {
                sendPacket(player, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,npc));
                cancel();
            }
        }.runTaskLater(plugin,50);

    }

    public void sendPacket(Player player, Packet<?> packet)
    {
        ( (CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    public void setRotationForAllPlayers() {
        npc.setLocation(npc.locX, npc.locY, npc.locZ, 0, (float) yaw);
        //int var1, byte var2, byte var3
        for (Player player : Bukkit.getOnlinePlayers()) {
          setRotation(player);
        }
    }

    public void setRotation(Player player)
    {
        sendPacket(player, new PacketPlayOutEntityHeadRotation(npc, (byte) (yaw * 256 / 360)));
        sendPacket(player, new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.getId(), (byte) (yaw * 256 / 360), (byte)0, true));
        sendPacket(player, new PacketPlayOutAnimation(npc, 0)); //pitch

        new BukkitRunnable()
        {
            public void run()
            {
                sendPacket(player, new PacketPlayOutAnimation(npc, 0));
                cancel();
            }
        }.runTaskLater(plugin, 50);
    }




    } //creating npcs for the shops
 //use game profiles?
// make sure that you differentiate between players and npcs if you do this
