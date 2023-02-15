package me.camm.productions.bedwars.Util.Helpers;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.TeamColor;
import me.camm.productions.bedwars.Util.BlockTag;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Bipi
 *  @author CAMM
 *  @author Mohit
 */
public class BlockTagManager {

    ConcurrentMap<Long, Byte> blocks;
    static BlockTagManager helper = null;
    static Arena arena = null;

    private BlockTag[] teamEntries = null;
    private TeamColor[] colors = null;

   public static BlockTagManager get(){

       if (arena == null)
           throw new IllegalStateException();

        if (helper == null)
         helper = new BlockTagManager();
        return helper;
    }


    public static int getValueAtPosition(int shift, byte number) {
        byte result = (byte) (number >> shift);
        return Math.abs(result % 2);
    }
    public static int getIndex(byte number) {
        int index;
        if (getValueAtPosition(7, number) == 1) {
            number = (byte) (Math.abs(number) - 1);  //getting how big it is
            index = (int) (Math.log(number) / Math.log(2) + 1); /// change of base formula
        }
        else index = (int) (Math.log(number) / Math.log(2));

        return index;
    }

    private BlockTag[] getTeamEntries(){

       if (teamEntries != null)
           return teamEntries;

        TeamColor[] teams = TeamColor.values();
        BlockTag[] values = new BlockTag[teams.length];
        for (TeamColor color: teams) {
            int index = getIndex(color.getTag().getTag());
            values[index] = color.getTag();
        }
        teamEntries = values;
        return teamEntries;
    }

    private TeamColor[] getTeamColors(){
       if (colors != null)
           return colors;

        TeamColor[] teams = TeamColor.values();
        TeamColor[] values = new TeamColor[teams.length];
        for (TeamColor color: teams) {
            int index = BlockTagManager.getIndex(color.getTag().getTag());
            values[index] = color;
        }
        colors = values;
        return colors;
    }

    private TeamColor toColorFromTag(BlockTag tag){
        TeamColor[] values = getTeamColors();
        int index = BlockTagManager.getIndex(tag.getTag());
        return values[index];
    }

    public TeamColor toColorFromTag(byte tag) {

       if (tag == BlockTag.NONE.getTag() || tag == BlockTag.ALL.getTag())
           return null;

       int index = getIndex(tag);
      return toColorFromTag(getTeamEntries()[index]);
    }



    public static void initialize(Arena gameArena){
       arena = gameArena;
    }

    //;private construct for singleton
    private BlockTagManager(){
      blocks = new ConcurrentHashMap<>(1000);
    }




    //Hashing function to generate unique ids for block locations
    // Function can be used to hash any tuple of integers
    //https://stackoverflow.com/questions/1358468/how-to-create-unique-integer-number-from-3-different-integers-numbers1-oracle-l
    public long hashBlock(int x, int y, int z)
    {
        //h = (a*6151 + b)*786433 + c
        // h = (a*p1 + b)*p2 +c
        // https://planetmath.org/goodhashtableprimes
        // Based on Java Arrays hashcode function

     //   return (x * 786433L + y) * 786433L + z;
        return ((786433L + x) * 786433L + y) * 786433L + z;


        //Did testing. larger numbers = better. does not matter if numbers are same
        //before, had error ~20 blocks per 100 000. now is much less
        //(not measured. ran out of memory while going up to larger values)
        // - Chippy
    }



    //BlockTag
    //BattleTeam

    public boolean canEntityBreak(Block block, Entity entity) {

       Location loc = block.getLocation();
       GameBoundary arenaBounds = arena.getBounds();
       if (!arenaBounds.containsCoordinate(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
       {
           return false;
       }

       EntityType type = entity.getType();

       // player, dragon
       switch (type) {
           case PLAYER:
               return canPlayerBreak((Player)entity, block);

           case ENDER_DRAGON:
               return canDragonBreak(loc);

        }
       return false;
    }

    public boolean isInbounds(Block block) {
       return arena.getBounds().containsCoordinate(block.getX(), block.getY(), block.getZ());
    }



    private static boolean canPlayerBreak(Player player, Block block){
       BattlePlayer bp = arena.getPlayers().getOrDefault(player.getUniqueId(), null);
       if (bp == null)
           return false;

       BlockTagManager manager = get();

       //no tag == map block
       if (!manager.hasTag(block))
           return false;

       byte tag = manager.getTag(block.getX(), block.getY(), block.getZ());

        //ALL tag denotes blocks placed by players
       return  tag == BlockTag.ALL.getTag();
    }

    private boolean canDragonBreak(Location l)
    {
        // Get metadata
        byte b = getTag(l.getBlockX(), l.getBlockY(), l.getBlockZ());

        // Check for generator metadata
        return b != BlockTag.NONE.getTag();

    }

    /*
    check if it's in bounds


       check if there's a generator tag



     */

    /*
    fireballs, tnt
     check material -> if it's
     tnt can break wool, planks, stained clay, endstone
     fireballs can break all but endstone

     planks -> check the byte data, tag
     wool -> check the tag, data

     */




    /*

    canEntityBreak(Block, Entity)
    {
         check if it is in bounds

         check if it is dragon
            if it has tag 0000 0000 <- generator/base air tag
                cancel

            else
                we can just break it (the dragon is breaking it)



          if it's not a dragon...

        check if the block has tag 1111 1111 <- player placed tag
           then, if it does

           if it's a bed
              check for team tags
                 if tag != the breaker's team

              we can break it (//for players)



                 if it's a fireball / tnt -> we conflict (compare the explosion strength with
                  blast resistance)

         otherwise cancel
    }
    // dragon, player, fireballs, tnt



    byte x = 0000 0000
    if x == BlockTag. ...






    canPlace(Block <- position in the world)
    {

    }
    //tower, player, egg  sponges


    canEntityInteract(Block block) {
    }
    //player
     */



    /*
    TODO: IMPLEMENT METADATA
    Unifying method for all entities to determine whether they
    can interact with things
     */


    /* onBlockBreak // tntexploded



    o player.get
    getPlayerTag(o)


    get

    hasPerm method called

     */


    /*
      interface
       -> break block ()
           -> check for tags
     */



    public boolean hasTag(Block block){
        return blocks.containsKey(hashBlock(block.getX(), block.getY(), block.getZ()));
    }


    public void removeTag(int x, int y, int z) {
       long hash = hashBlock(x,y,z);
       blocks.remove(hash);
    }


    public void addBlock(int x, int y, int z, byte tag)
    {
        long index = hashBlock(x, y, z);

        //rtn for now
        if (blocks.containsKey(index)) {
            return;
        }

        blocks.put(index, tag);
    }

    /*
    if the block has a tag, then replaces that tag with a new one
     */
    public void overrideTag(int x, int y, int z, byte newTag){
        long index = hashBlock(x,y,z);
        if (blocks.containsKey(index))
            blocks.replace(index, newTag);
        else blocks.put(index, newTag);
    }


/**
@pre a check has been done to see if the block has a tag
 */
    public byte getTag(int x, int y, int z)
    {
        long index = hashBlock(x, y, z);
        return blocks.get(index);

    }

    public byte getTag(Block block)
    {
        return getTag(block.getX(), block.getY(), block.getZ());
    }






}
