package me.camm.productions.bedwars.Structures;

import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;
import me.camm.productions.bedwars.Util.BlockTag;
import me.camm.productions.bedwars.Util.Helpers.BlockTagManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

import static me.camm.productions.bedwars.Structures.TowerParameter.*;

public class Tower
{
    private final Location placedLocation;
    private Location startLocation;
    private final byte color;


    private final World world;
    private  final Player player;
    private final Plugin plugin;

    private int xDirection;
    private int zDirection;

    private int xBaseLength;
    private int zBaseLength;

    private BlockFace face;




    public Tower(BlockPlaceEvent event, Plugin plugin, byte color)  //you would include a material in the constructor
    {
        this.color = color;
        this.plugin = plugin;
        this.player = event.getPlayer();
        this.placedLocation = event.getBlock().getLocation().subtract(0,1,0);
        this.world = event.getBlock().getWorld();

        setDirections();
        returnChest(event);

    }


    private void returnChest(BlockPlaceEvent event)
    {
        event.setCancelled(true);
        Block block = event.getBlockPlaced();
        BlockTagManager manager = BlockTagManager.get();

        if (!manager.isInbounds(block)) {
            event.getPlayer().sendMessage(ChatColor.RED+"You cannot place blocks here!");
            return;
        }

        if (!manager.hasTag(block)) {
            if (transact(event))
                construct();
            return;
        }

        if (manager.getTag(block) == BlockTag.ALL.getTag()) {
            if (transact(event))
              construct();
            return;
        }

        event.getPlayer().sendMessage(ChatColor.RED+"You cannot place blocks here!");

    }

    private boolean transact(BlockPlaceEvent event){

        final ItemStack empty = new ItemStack(Material.AIR, 1);
        Player player = event.getPlayer();
        Inventory inv = player.getInventory();

        for (int slot=0;slot<player.getInventory().getSize();slot++)
        {
            if (inv.getItem(slot) == null) continue;
            ItemStack item = inv.getItem(slot);

            if (item.getItemMeta() == null) continue;

            if (item.getType() != Material.CHEST || !ShopItem.POPUP_TOWER.name.equalsIgnoreCase(item.getItemMeta().getDisplayName()))
                continue;

            if (item.getAmount()>1)
            {
                item.setAmount(item.getAmount()-1);
                inv.setItem(slot,item);
            }
            else
                inv.setItem(slot, empty);


            return true;

        }
        return false;

    }

    /*

      new BukkitRunnable() {

      new ... {
      }

      }.runTask()

     */


    ///refactor this
    private void construct()  //building the tower
    {

        Location heightLocation = startLocation.clone();

        LineBuilder lineMaker = new LineBuilder(xDirection,zDirection,color,world,plugin);  //color is byte
        new BukkitRunnable()
        {
            int currentHeight = 0;
            public void run()
            {
                currentHeight = runnable(lineMaker, heightLocation,currentHeight);
                if (currentHeight == -1)
                    cancel();


            }//outer run()
        }.runTaskTimer(plugin, 0, PERIOD.getMeasurement());


        //public BlockLine(int xMultiplier, int zMultiplier, byte color, World world)


    }

    ///keep for reference, remove this.
    private Location getHalfLocation(Location beginning, int multiplier, int length, boolean isX)
    {
        return isX ? beginning.clone().add(multiplier*(length+1),0,0) : beginning.clone().add(0,0,multiplier*(length+1));
    }

   /*
   A yaw of 0 or 360 represents the positive z direction. [S]
A yaw of 180 represents the negative z direction. [N]
A yaw of 90 represents the negative x direction. [W]
A yaw of 270 represents the positive x direction. [E]
    */

    //Converting MC direction to normal directions that go from 0 --> 360
    //useful
    private double normalizeDirection(double direction)
    {
        //if mod 360 greater/equal 0    t: return dir   f: return dir+360
        return (direction %= 360) >= 0 ? direction : (direction + 360);
    }


    //refactor or remove
    private void setDirections()  //getting dir and init variables.
    {
        double yaw = normalizeDirection(player.getLocation().getYaw());

        if (yaw<AngledDirection.FORTY_FIVE.getDir()||yaw>AngledDirection.THREE_FIFTEEN.getDir()||(yaw<AngledDirection.TWO_TWENTY_FIVE.getDir()&&yaw>AngledDirection.ONE_THIRTY_FIVE.getDir()))  //z length
        {
            zBaseLength = BASE_WIDTH.getMeasurement();  //2
            xBaseLength = BASE_LENGTH.getMeasurement();  //3

        }
        else
        {
            zBaseLength = BASE_LENGTH.getMeasurement(); //3
            xBaseLength = BASE_WIDTH.getMeasurement();  //2

        }


        if (yaw> AngledDirection.THREE_FIFTEEN.dir ||yaw<AngledDirection.ONE_THIRTY_FIVE.dir) { ///////////CHANGED NEG AND POS

            xDirection = POSITIVE.getMeasurement();
        }
        else {
            xDirection = NEGATIVE.getMeasurement();

        }


        if (yaw>AngledDirection.FORTY_FIVE.dir&&yaw<AngledDirection.TWO_TWENTY_FIVE.dir) {

            zDirection = POSITIVE.getMeasurement();
        }
        else {

            zDirection = NEGATIVE.getMeasurement();
        }

        calculateStart();
        calculateFace(yaw);
    }


    //redo it
    private void calculateStart()//finding the start location for the lines to originate from
    {
        Location initial = placedLocation.clone();

        int xPrimeLength;
        if (xBaseLength==BASE_LENGTH.getMeasurement())
            xPrimeLength = PRIME_TWO.getMeasurement();
        else
            xPrimeLength = PRIME_ONE.getMeasurement();


        int zPrimeLength;
        if (zBaseLength==BASE_LENGTH.getMeasurement())
            zPrimeLength = PRIME_TWO.getMeasurement();
        else
            zPrimeLength = PRIME_ONE.getMeasurement();

        initial.setX(placedLocation.getBlockX()+((xDirection*-1)* xPrimeLength)); //((xDirection*-1)*xPrimeLength))
        initial.setZ(placedLocation.getBlockZ()+((zDirection*-1)* zPrimeLength));

        this.startLocation = initial;

    }

    //keep for reference
    private void calculateFace(double yaw) //calculate face for the ladders
    {
        if (yaw>AngledDirection.THREE_FIFTEEN.dir||yaw<AngledDirection.FORTY_FIVE.dir)
        {
            face = BlockFace.SOUTH;
        }
        else if (yaw>AngledDirection.FORTY_FIVE.dir&&yaw<AngledDirection.ONE_THIRTY_FIVE.dir)
        {
            face = BlockFace.WEST;
        }
        else if (yaw>AngledDirection.ONE_THIRTY_FIVE.dir&&yaw<AngledDirection.TWO_TWENTY_FIVE.dir)
        {
            face = BlockFace.NORTH;
        }
        else
            face = BlockFace.EAST;


    }

    //refactor
    public int runnable(LineBuilder lineMaker, Location heightLocation, int currentHeight)
    {

                    if (currentHeight< HEIGHT.getMeasurement())
                    {
                        lineMaker.drawSolidX(xBaseLength,heightLocation,true);
                        lineMaker.drawSolidZ(zBaseLength,heightLocation,true);

                        lineMaker.placeLadder(placedLocation.clone().add(0,currentHeight,0),face);

                        Location xHalf = getHalfLocation(startLocation,xDirection,xBaseLength,true);  //
                        Location zHalf = getHalfLocation(startLocation, zDirection, zBaseLength, false);

                        if (currentHeight< DOCKING_HEIGHT.getMeasurement())
                        {
                            if (xBaseLength==BASE_LENGTH.getMeasurement())  //x is longer, so x has the entrance
                            {
                                // public void drawJumpedX(int placeNumber, int period, Location loc, boolean skipOne)
                                lineMaker.drawJumpedX(2,2,zHalf.clone().add(0,currentHeight,0),true); /////////////////////////// [false] period 1
                                lineMaker.drawSolidZ(zBaseLength, xHalf.add(0, currentHeight, 0),true);
                            }
                            else //z longer, so z is the entrance
                            {
                                lineMaker.drawJumpedZ(2,2,xHalf.clone().add(0,currentHeight,0),true);///////////// [false] , period 1
                                lineMaker.drawSolidX(xBaseLength, zHalf.add(0, currentHeight, 0),true);
                            }

                        }
                        else {
                            lineMaker.drawSolidX(xBaseLength, zHalf.add(0, currentHeight, 0),true);
                            lineMaker.drawSolidZ(zBaseLength, xHalf.add(0, currentHeight, 0),true);
                        }

                        heightLocation.setY(heightLocation.getBlockY()+1);
                        currentHeight++;
                    }
                    else
                    {
                        lineMaker.placeLadder(placedLocation.clone().add(0,currentHeight,0),face);  //Just 1 more ladder block


                        new BukkitRunnable()//2
                        {
                            public void run()
                            {
                                lineMaker.drawHatch(startLocation.clone().add(0, HEIGHT.getMeasurement(), 0), xBaseLength == BASE_LENGTH.getMeasurement());

                                new BukkitRunnable()//3
                                {
                                    public void run()
                                    {
                                        if (xBaseLength==BASE_LENGTH.getMeasurement())
                                        {
                                            lineMaker.drawSegmentedX(startLocation.clone().add(0,HEIGHT.getMeasurement(),zDirection*-1),1,MAIN_BATTLEMENTS.getMeasurement());
                                            lineMaker.drawSegmentedX(startLocation.clone().add(0,HEIGHT.getMeasurement(), zDirection*PLATFORM_WIDTH.getMeasurement()),1,MAIN_BATTLEMENTS.getMeasurement());
                                            lineMaker.drawRoundPerimeter(startLocation.clone().add(xDirection*-1,BATTLEMENT_BASE_HEIGHT.getMeasurement(),zDirection*-1),PLATFORM_LENGTH.getMeasurement(),PLATFORM_WIDTH.getMeasurement());
                                        }
                                        else
                                        {
                                            lineMaker.drawSegmentedZ(startLocation.clone().add(xDirection*-1,HEIGHT.getMeasurement(),0),1,MAIN_BATTLEMENTS.getMeasurement());
                                            lineMaker.drawSegmentedZ(startLocation.clone().add(xDirection*PLATFORM_WIDTH.getMeasurement(),HEIGHT.getMeasurement(),0),1,MAIN_BATTLEMENTS.getMeasurement());
                                            lineMaker.drawRoundPerimeter(startLocation.clone().add(xDirection*-1,BATTLEMENT_BASE_HEIGHT.getMeasurement(),zDirection*-1),PLATFORM_WIDTH.getMeasurement(),PLATFORM_LENGTH.getMeasurement());
                                        }
                                        cancel(); //3
                                    }
                                }.runTask(plugin);//3

                                cancel();  //inner 2

                            }//run() inner
                        }.runTask(plugin);//inner 1

                        return -1;
                    }
                    return currentHeight;
                }



                //get rid of?
    private enum AngledDirection
    {
        FORTY_FIVE(45),
        ONE_THIRTY_FIVE(135),
        TWO_TWENTY_FIVE(225),
        THREE_FIFTEEN(315);

        private final int dir;

        AngledDirection(int dir)
        {
            this.dir = dir;
        }

        int getDir()
        {
            return dir;
        }
    }

}



