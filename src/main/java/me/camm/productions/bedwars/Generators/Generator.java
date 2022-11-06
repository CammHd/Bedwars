package me.camm.productions.bedwars.Generators;


import me.camm.productions.bedwars.Arena.GameRunning.Events.EventTime;
import me.camm.productions.bedwars.Util.Locations.BlockRegisterType;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

import static me.camm.productions.bedwars.Arena.GameRunning.Events.EventTime.*;

/**
 * @author CAMM
 * Models a generator object in the plugin
 */
public class Generator
{

    private final static int GROUPED_PLAYER_NUMBER;


    //number to check for when deciding the cap for diamond and emerald spawn amounts
    static {
        GROUPED_PLAYER_NUMBER = 3;
    }



    private final String name;

    private int nextSpawnTime;
    private volatile int totalSpawnTime;
    private String spawnWord;  //To show the type of spawn
    private int tier;

    private final Material type;
    //What the generator is spawning
    private final GeneratorType genType;

    private int timeCount;
    private int playerNumber;

    private final Plugin plugin;

    //Armorstand that has the name of the product (E.g Diamond)
    private ArmorStand generatorType;

    //Armorstand showing time left before next product spawn (Diamond in x seconds ...)
    private ArmorStand timeTitle;

    //Armorstand that shows the tier of the generator. (E.g 1,2,3)
    private ArmorStand tierTitle;

    //Armorstand with a diamond or emerald block on it's head that spins.
    private ArmorStand spinningBlock;


    private final World world;

    //registered area where you can't place or break blocks
    private final GameBoundary box;


    private final double x;
    private final double y;  //location of the spinning block. The 3 other titles will be calibrated to match the location
    private final double z;

    //time of upgrades is controlled externally

    public Generator(Coordinate location, World world, GeneratorType type, Plugin plugin, GameBoundary box)  //construct
    {
        this.box = box;
        this.name = BlockRegisterType.GENERATOR.getData();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.world = world;
        this.playerNumber = 0;
        this.plugin = plugin;
        this.timeCount = 0;

        this.genType = type;
        this.type = genType.getSpinningBlockMaterial();
    }

    /*
 @Author CAMM
 When this method is called, the generator spawns in the world and gets ready to rumble :D

  */
    public void spawnIntoWorld()
    {
        spinningBlock = (ArmorStand) world.spawnEntity(new Location(world, x,y,z), EntityType.ARMOR_STAND);
        timeTitle = (ArmorStand)world.spawnEntity(new Location(world, x,y+1.8,z), EntityType.ARMOR_STAND);
        generatorType = (ArmorStand) world.spawnEntity(new Location(world, x,y+2,z), EntityType.ARMOR_STAND);
        tierTitle = (ArmorStand) world.spawnEntity(new Location(world, x,y+2.2,z), EntityType.ARMOR_STAND);

        Chunk loaded = world.getChunkAt((int) x, (int) z);

        if (!loaded.isLoaded())
            loaded.load();






        switch (genType)
        {
            case DIAMOND:
            {
                generatorType.setCustomName(ChatColor.AQUA+genType.getSimpleName());
                setTimeTitle(EventTime.DIAMOND_TIER_ONE_TIME.getTime());  //Diamond in [n] seconds
                setGeneratorTier(1,DIAMOND_TIER_ONE_TIME.getTime());
                totalSpawnTime = DIAMOND_TIER_ONE_TIME.getTime();
                nextSpawnTime = DIAMOND_TIER_ONE_TIME.getTime();
            }
            break;

            case EMERALD:
            {
                generatorType.setCustomName(ChatColor.GREEN+genType.getSimpleName());
                setTimeTitle(EMERALD_TIER_ONE_TIME.getTime());  //basically the time till spawn
                totalSpawnTime = EMERALD_TIER_ONE_TIME.getTime();
                nextSpawnTime = EMERALD_TIER_ONE_TIME.getTime();
                setGeneratorTier(1,EMERALD_TIER_ONE_TIME.getTime());
            }
        }

        //Initializing string for title to show time left until product spawn.
        spawnWord = ChatColor.YELLOW+genType.getSimpleName()+" in ";
        spinningBlock.setHelmet(new ItemStack(genType.getSpinningBlockMaterial()));

        //Setting the armorstands to be invisible, for their names to show (if applicable)
        //and both turning off gravity and changing their hitbox size.
        timeTitle.setVisible(false);
        timeTitle.setGravity(false);
        timeTitle.setCustomNameVisible(true);

        spinningBlock.setVisible(false);
        spinningBlock.setGravity(false);

        generatorType.setCustomNameVisible(true);
        generatorType.setVisible(false);
        generatorType.setGravity(false);

        tierTitle.setCustomNameVisible(true);
        tierTitle.setVisible(false);
        tierTitle.setGravity(false);

        spinningBlock.setMarker(true);
        timeTitle.setMarker(true);
        generatorType.setMarker(true);
        tierTitle.setMarker(true);

    }


    //getters, setters and helpers
    public void registerBox()
    {
        box.register(world,name,1,plugin);  //register all blocks
    }

    public void setPlayerNumber(int newNumber)
    {
        this.playerNumber = newNumber;
    }

    public GeneratorType getGenType() {
        return genType;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public Material getType()
    {
        return type;
    }


    public int getTimeCount()
    {
        return timeCount;  //time count for the milliseconds in the runtime
    }

    public void setTimeCount(int newTime)
    {
        this.timeCount = newTime;
    }

    public int getTier()
    {
        return tier;
    }

    public int getNextSpawnTime()
    {
        return nextSpawnTime;
    }

    public void setTimeTitle(int seconds)  //setting the time title
    {
        timeTitle.setCustomName(spawnWord+ChatColor.RED+seconds+ChatColor.YELLOW+" seconds");
    }


    //returns the newSpawning time of the next tier.
    public int getNextNewSpawnTime()
    {
        int nextTime;

        switch (tier)
        {
            case 1:
                nextTime = genType==GeneratorType.DIAMOND? DIAMOND_TIER_TWO_TIME.getTime(): EMERALD_TIER_TWO_TIME.getTime();
                break;

            case 2:
                nextTime =  genType==GeneratorType.DIAMOND? DIAMOND_TIER_THREE_TIME.getTime() : EMERALD_TIER_THREE_TIME.getTime();
                break;

            default:
                nextTime = genType==GeneratorType.DIAMOND? DIAMOND_TIER_ONE_TIME.getTime(): EMERALD_TIER_ONE_TIME.getTime();
        }
        return nextTime;
    }


    //sets the tier of the generator
    public void setGeneratorTier(int tier, int newSpawnTime)
    {
        //1 --> -1
        //2 --> 0
        //3 --> 1


        this.tier = tier;
        this.totalSpawnTime = newSpawnTime;
        this.nextSpawnTime = newSpawnTime;
        this.tierTitle.setCustomName(ChatColor.YELLOW+"Tier "+this.tier);
        setTimeTitle(newSpawnTime);
    }


    //uses sinusodial and cosine function to make the block spin around based on an x value, time
    public void setRotation(double time)
    {
        double yaw = (360*(Math.sin(0.3*time)))*5;
        double yComponent = (timeTitle.getLocation().getY()-1.8) + (0.1*Math.cos(time));
        Location blockLocation = spinningBlock.getLocation();
        blockLocation.setYaw((float)yaw);
        blockLocation.setY(yComponent);
        spinningBlock.teleport(blockLocation);
    }

    /*
    Updates the time and returns whether an item should be spawned.
     */
    public boolean updateSpawnTime()  //advance the time, if 0, then spawn an item
    {
        this.nextSpawnTime--;
        if (this.nextSpawnTime<=0)
        {
            this.nextSpawnTime = totalSpawnTime;  //reset the time
            return true;
        }
      return false;   //If the time is 0, return true, else return false
    }

    //spawns an item into the world depending on the type
    public void spawnItem()
    {
        int nearby = 0;
        Item current;

        List<Entity> items = generatorType.getNearbyEntities(generatorType.getLocation().getX(),
                generatorType.getLocation().getY(),generatorType.getLocation().getBlockZ());

        Material product = genType.getProductMaterial();

        for (Entity entity : items) {
            if (!entity.getType().equals(EntityType.DROPPED_ITEM))
             continue;

            current = (Item) entity;
            if (current.getItemStack().getType() != product) //  if diamond/emerald
              continue;

            if (current.getLocation().distance(generatorType.getLocation()) > 5)
              continue;

            if (current.hasMetadata(name))
              nearby += current.getItemStack().getAmount();

        } //for

        if (playerNumber< GROUPED_PLAYER_NUMBER)
        {
            switch (genType)
            {
                case DIAMOND:
                {
                    if (nearby<GeneratorItemSpawnLimits.TWOS_LESS_DIAMOND.getNumber()) //if the nearby diamonds are less than 4
                        summonItem();
                }
                break;

                case EMERALD:
                {
                    if (nearby<GeneratorItemSpawnLimits.TWOS_LESS_EMERALD.getNumber())
                        summonItem();
                }
                break;
            }//switch


        }
        else   //there are at least 3 players
        {
            switch (genType)
            {
                case DIAMOND:
                {
                    if (nearby<GeneratorItemSpawnLimits.THREES_MORE_DIAMOND.getNumber())
                        summonItem();
                }
                break;

                case EMERALD:
                {
                    if (nearby< GeneratorItemSpawnLimits.THREES_MORE_EMERALD.getNumber())
                       summonItem();
                }
            }
        }


    }//method



    //actually creates an item and spawns it
    private void summonItem()
    {
        new BukkitRunnable()
        {
            public void run()
            {
                Item dropped = world.dropItem(generatorType.getLocation(), new ItemStack(genType.getProductMaterial(),1));
                dropped.setMetadata(name, new FixedMetadataValue(plugin,1));
                dropped.setVelocity(new Vector(0,0,0));
                cancel();
            }
        }.runTask(plugin);

    }





   private enum GeneratorItemSpawnLimits {

        TWOS_LESS_DIAMOND(4),
        TWOS_LESS_EMERALD(2),

        THREES_MORE_DIAMOND(8),
        THREES_MORE_EMERALD(6);

        private final int number;

         GeneratorItemSpawnLimits(int number)
        {
            this.number = number;
        }

        public int getNumber()
        {
            return number;
        }
    }



} //class

