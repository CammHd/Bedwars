package me.camm.productions.bedwars.Util.Helpers;

import org.bukkit.block.Block;

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

   public static BlockTagManager get(){
        if (helper == null)
         helper = new BlockTagManager();
        return helper;
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
        return ((786433L + x) * 786433L + y) * 78633L + z;


        //Did testing. larger numbers = better. does not matter if numbers are same
        //before, had error ~20 blocks per 100 000. now is much less
        //(not measured. ran out of memory while going up to larger values)
        // - Chippy
    }



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


/**
@pre a check has been done to see if the block has a tag
 */
    public byte getTag(int x, int y, int z)
    {
        long index = hashBlock(x, y, z);
        return blocks.get(index);

    }






}
