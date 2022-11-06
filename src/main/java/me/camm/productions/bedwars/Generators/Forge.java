package me.camm.productions.bedwars.Generators;

import me.camm.productions.bedwars.Arena.Teams.TeamColor;
import me.camm.productions.bedwars.Util.Randoms.WeightedItem;
import me.camm.productions.bedwars.Util.Randoms.WeightedRandom;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * @author CAMM
 * class for a forge in the game
 */
public class Forge implements Runnable {
    private final String type;
    private final World world;
    private final Location location;
    private final Plugin plugin;

    private int goldCount, ironCount;
    private boolean recount;

    private final UUID id;

    private final String color;

    private final long initialTime;
    private volatile long spawnTime;


    private volatile int tier;
    private volatile boolean isAlive;
    private final double pickup;

    private final Random spawningTimeRand;

  //  private static final double PICKUP_DISTANCE;
    private static final int MAX_GOLD;
    private static final int MAX_IRON;

    //weighted random for spawning items
    private final WeightedRandom<WeightedItem<Material>> spawningRandom;
    private final WeightedItem<Material> emeraldChance;
    private final WeightedItem<Material> goldChance;

    private static final String FORGE = "FORGE";

    static {
      //  PICKUP_DISTANCE = 1.5;
        MAX_GOLD = 16;
        MAX_IRON = 48;

    }

    public Forge(double x, double y, double z, World world, TeamColor color, long initialTime, Plugin plugin, double pickup)  //construct
    {
        this.recount = false;
        this.location = new Location(world, x, y, z);
        this.color = color.getName();
        this.initialTime = initialTime;
        this.world = world;
        this.tier = 0;
        this.plugin = plugin;
        this.type = FORGE;
        this.pickup = pickup;

        this.spawnTime = initialTime;
        this.isAlive = true;
        this.id = UUID.randomUUID();
        ironCount = goldCount = 0;


        Chunk chunk = world.getChunkAt(location);
        if (!chunk.isLoaded())
            chunk.load();

        spawningTimeRand = new Random();
        emeraldChance = new WeightedItem<>(Material.EMERALD, 0);
        goldChance = new WeightedItem<>(Material.GOLD_INGOT, 0);

        ArrayList<WeightedItem<Material>> materials = new ArrayList<>();
        materials.add(new WeightedItem<>(Material.IRON_INGOT, 0.8));
        materials.add(goldChance);
        materials.add(emeraldChance);
        spawningRandom = new WeightedRandom<>(materials);

    }

    public static String getKeyword(){
        return FORGE;
    }

    //returns the forge location
    public Location getForgeLocation() {
        return location;
    }

    //returns the pickup distance
    public double getDistance() {
        return pickup;
    }

    //disables the forge
    public synchronized void disableForge() {
        this.isAlive = false;
    }

    //gets the team color of the forge
    public String getColor() {
        return color;
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

    //Spawns an item
    public synchronized void spawnItem() {
        int freedom = verifyCount();
        Material mat;

        switch (freedom) {
            case -1:
                mat = null;
                break;

            case 0:
                mat = Material.IRON_INGOT;
                break;

            case 1:
                mat = Material.GOLD_INGOT;
                break;

            default:
                mat = spawningRandom.getNext().getItem();

        }

        if (mat == null)
            return;

        drop(mat);
    }


    //drops an item onto the ground dependant on the material
    private void drop(Material mat) {
        if (!isAlive || !plugin.isEnabled())
            return;


        /*
        Gold chance is initially very small. We increase it to a max of 0.2
         */
        goldChance.setWeight(Math.min(goldChance.getWeight() + 0.01, 0.2));

        new BukkitRunnable() {
            @Override
            public void run() {
                Item spawned = world.dropItem(location, new ItemStack(mat, 1));
                spawned.setCustomName(id.toString());
                spawned.setVelocity(new Vector(0, 0, 0));
                spawned.setMetadata(type, new FixedMetadataValue(plugin, 1));

                if (mat == Material.IRON_INGOT) {
                     ironCount ++;
                }

                if (mat == Material.GOLD_INGOT) {
                    goldCount ++;
                }

                cancel();
            }
        }.runTask(plugin);



    }

    //updates the amount of gold or iron that the forge has spawned on the ground.
    public synchronized void updateChildren(Material mat, int amount) {

        if (mat == Material.GOLD_INGOT) {
            goldCount -= amount;

            if (goldCount <=0 || goldCount >= MAX_GOLD)
                recount = true;
        }
        else {
            ironCount -= amount;

            if (ironCount <=0 || ironCount >= MAX_IRON)
                recount = true;
        }

          }



          //verifies that the forge is not spawning more than it's cap
    private int verifyCount()
        {
            if (!recount) {
                if (goldCount >= MAX_GOLD) //if gold is invalid
                    return ironCount >= MAX_IRON ? SpawningFreedom.NO_SPAWNING.getFreedom() : SpawningFreedom.ONLY_IRON.getFreedom();
                else
                    return ironCount >= MAX_IRON ? SpawningFreedom.ONLY_GOLD.getFreedom() : SpawningFreedom.FULL_SPAWNING.getFreedom();
            }


                int goldCount = 0;
                int ironCount = 0;

                Collection<Entity> nearby = world.getNearbyEntities(location, pickup, pickup, pickup);
                for (Entity entity : nearby) {

                    if (!(entity instanceof Item))
                        continue;


                    Item item = (Item) entity;
                    if (!item.hasMetadata(type))
                        continue;

                    ItemStack stack = item.getItemStack();
                    Material mat = stack.getType();

                    switch (mat) {
                        case GOLD_INGOT:
                            goldCount += stack.getAmount();
                            break;
                        case IRON_INGOT:
                            ironCount += stack.getAmount();
                    }

                }


                if (goldCount >= MAX_GOLD) //if gold is invalid
                    return ironCount >= MAX_IRON ? SpawningFreedom.NO_SPAWNING.getFreedom() : SpawningFreedom.ONLY_IRON.getFreedom();
                else
                    return ironCount >= MAX_IRON ? SpawningFreedom.ONLY_GOLD.getFreedom() : SpawningFreedom.FULL_SPAWNING.getFreedom();

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
                        spawnItem();
                    }
                    catch (InterruptedException e)
                    {
                        disableForge();
                    }
                }



    }

    //get the tier
    public synchronized int getTier(){
      return tier;
    }

    public UUID getId(){
      return id;
    }

    private enum SpawningFreedom {
      NO_SPAWNING(-1),  //Don't spawn anything
        ONLY_IRON(0),
        ONLY_GOLD(1),
        FULL_SPAWNING(2);  //spawn everything

      final int freedom;

      SpawningFreedom(int freedom)
      {
          this.freedom = freedom;
      }

      int getFreedom()
      {
          return freedom;
      }


    }

}
