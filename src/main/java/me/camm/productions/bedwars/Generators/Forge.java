package me.camm.productions.bedwars.Generators;

import com.google.common.base.Predicate;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Randoms.WeightedItem;
import me.camm.productions.bedwars.Util.Randoms.WeightedRandom;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityItem;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class Forge implements Runnable {

    private long lastInteraction;
    private final int id;
    private final World world;
    private final Location location;
    private final Plugin plugin;

    private int goldCount, ironCount;
    private boolean recount;

    private final long initialTime;
    private volatile long spawnTime;
    private volatile int tier;
    private volatile boolean isAlive;
    private final double pickup;
    private final Random spawningTimeRand;



    //weighted random for spawning items
    private final WeightedRandom<WeightedItem<Material>> spawningRandom;
    private final WeightedItem<Material> emeraldChance;


    private volatile Material nextSpawn;
    private static int forgeCount;
    private static final String FORGE = "FORGE";


    private static int maxGold;
    private static int maxIron;

    private static double goldChance;
    private static double ironChance;

    private static final Predicate<Entity> isItemCheck;


    static {
        maxGold = 16;
        maxIron = 48;
        forgeCount = 0;

        ironChance = 0.8;
        goldChance = 0.2;

        isItemCheck = new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                if (entity == null)
                    return false;

                return entity instanceof EntityItem;
            }
        };


    }



    public Forge(double x, double y, double z, World world, long initialTime, Plugin plugin, double pickup)  //construct
    {
        this.recount = false;
        this.location = new Location(world, x, y, z);
        this.initialTime = initialTime;
        this.world = world;
        this.tier = 0;
        this.plugin = plugin;

        this.id = forgeCount;
        forgeCount ++;


        this.pickup = pickup;
        this.spawnTime = initialTime;
        this.isAlive = true;
        ironCount = goldCount = 0;


        spawningTimeRand = new Random();
        emeraldChance = new WeightedItem<>(Material.EMERALD, 0);

        ArrayList<WeightedItem<Material>> materials = new ArrayList<>();
        materials.add(new WeightedItem<>(Material.IRON_INGOT, ironChance));
        materials.add(new WeightedItem<>(Material.GOLD_INGOT, goldChance));

        materials.add(emeraldChance);
        spawningRandom = new WeightedRandom<>(materials);

        nextSpawn = null;
        lastInteraction = System.currentTimeMillis();

    }

    //@pre the forge is not created yet
    public static void setMaxGold(int newNumber){
    maxGold = newNumber;
    }

    //@pre the forge is not created yet
    public static void setMaxIron(int newNumber) {
        maxIron = newNumber;
    }


    //@pre The forge is not created yet
    public static void setGoldChance(double chance) {
        goldChance = chance;
    }

    //@pre the forge is not created yet
    public static void setIronChance(double chance) {
        ironChance = chance;
    }

    public synchronized void updateGoldCount(int subtract){
        goldCount -= subtract;
    }

    public synchronized void updateIronCount(int subtract) {
        ironCount -= subtract;
    }


    public Plugin getPlugin(){
        return plugin;
    }

    public void queryRecount(){
        this.recount = true;
    }

    public static String getKeyword(){
        return FORGE;
    }

    //returns the forge location
    public Location getForgeLocation() {
        return location;
    }

    //returns the pickup distance
    public double getPickupDistance() {
        return pickup;
    }

    //disables the forge
    public synchronized void stopForge() {
        this.isAlive = false;
    }

    public int getId(){
        return id;
    }


    //sets the tier of the forge to a new tier
    public synchronized void setTier(int newTier)
    {
        this.tier = newTier;

        switch (tier) {
            case 1:
                spawnTime = (long) (initialTime / 1.5);
                break;

            case 2:
                spawnTime = (long) (initialTime / 2.5);
                break;

            case 3:
                emeraldChance.setWeight(0.005);
                break;

            case 4:
                spawnTime = (long) (initialTime / 3.5);
                //emeraldChance.setWeight(0.005);
                break;
        }
    }

    //returns a random time for the thread to sleep
    public long randomize() {
        return (long) (spawnTime * (spawningTimeRand.nextDouble() * 1.5));
    }


    //drops an item onto the ground dependent on the material
    private void spawnItem() {

        if (!isAlive || !plugin.isEnabled()) {
            stopForge();
            return;
        }

        if (nextSpawn == null)
            return;

        Material next = nextSpawn;
        if (next == Material.GOLD_INGOT) {
            goldCount++;
        }
        else if (next == Material.IRON_INGOT) {
            ironCount ++;
        }


        new BukkitRunnable() {
            public void run() {
                Item item = world.dropItem(location,new ItemStack(next,1));
                item.setVelocity(new Vector(0,0,0));
                item.setMetadata(FORGE,new FixedMetadataValue(plugin,id));
                cancel();
            }
        }.runTask(plugin);


    }

    //updates the amount of gold or iron that the forge has spawned on the ground.
    public void calculateNextDrop() {

        double millis = spawnTime * (goldCount + ironCount);
        if (System.currentTimeMillis() >= millis + lastInteraction && (ironCount + goldCount >= maxIron + maxGold)) {
            lastInteraction = System.currentTimeMillis();
            queryRecount();
        }

        Material next = Material.IRON_INGOT;
        boolean restriction = false;

        if (ironCount >= maxIron) {
            next = Material.GOLD_INGOT;
            restriction = true;
        }

        if (goldCount >= maxGold) {
            next = restriction ? null: Material.IRON_INGOT;
            restriction = true;
        }

        if (!restriction) {
        next = spawningRandom.getNext().getItem();
        }
        nextSpawn = next;
    }


    //Updates the number of items on the ground
    private void recount() {

        if (!recount)
            return;

        ironCount = goldCount = 0;

        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        AxisAlignedBB checkArea = new AxisAlignedBB(location.getX() - pickup, location.getY() - pickup, location.getZ() - pickup,
                location.getX() + pickup, location.getY() + pickup, location.getZ() + pickup);

        Collection<Entity> items = nmsWorld.a((net.minecraft.server.v1_8_R3.Entity) null, checkArea, isItemCheck);
        for (Entity nmsItem : items) {
            CraftItem bukkitItem = (CraftItem) nmsItem.getBukkitEntity();


            //if it was dropped by A forge
            if (!bukkitItem.hasMetadata(FORGE))
                continue;

            boolean droppedByThisForge = bukkitItem.getMetadata(FORGE).stream().anyMatch(metadataValue -> {
                return metadataValue.value() instanceof Integer && metadataValue.asInt() == this.id;
            });

            if (!droppedByThisForge)
                continue;

            switch (bukkitItem.getItemStack().getType()) {
                case IRON_INGOT:
                    ironCount++;
                    break;

                case GOLD_INGOT:
                    goldCount++;
                    break;
            }
        }
        recount = false;
    }


        //core thread for the spawning mechanic
    @Override
    public void run()
    {
                while (isAlive)
                {
                    try
                    {
                        Thread.sleep(randomize());
                        if (recount)
                            recount();

                        calculateNextDrop();
                        spawnItem();

                    }
                    catch (InterruptedException e)
                    {
                        ChatSender sender = ChatSender.getInstance();
                        sender.sendConsoleMessage("Thread for forge at "+location.toString()+" was interrupted.", Level.WARNING);
                    }
                }
    }

    //get the tier
    public synchronized int getTier(){
      return tier;
    }

}
