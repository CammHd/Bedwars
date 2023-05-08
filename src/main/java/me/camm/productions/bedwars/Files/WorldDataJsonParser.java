package me.camm.productions.bedwars.Files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.BedWars;

import me.camm.productions.bedwars.Generators.Generator;
import me.camm.productions.bedwars.Generators.GeneratorType;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import org.bukkit.World;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;


/*
@author CAMM
This class reads from the WorldData.json file and creates both generator and objects.
 */
public class WorldDataJsonParser extends JsonParser
{


    //Keywords for the parser to look for in the files for constructing the arena
    enum Keyword {
        ARENA("Arena"),
        SPECTATOR("Spectator Spawn"),
        NAME("World Name"),
        VOID("Void Y Level"),
        BOUNDARY("Boundaries");

        final String data;
        Keyword(String data) {
            this.data = data;
        }
    }


    //Keywords for the reader to look for when creating the generators
    enum GenKeyword {
        GEN_TYPE("Type",false),
        LOCATION("Location",false),
        GENERATORS("Generators",false),
        SPEEDS("Tier Speed",false);

        final String data;
        final boolean expectPhrase;

        GenKeyword(String data, boolean expectPhrase) {
            this.data = data;
            this.expectPhrase = expectPhrase;
        }
    }

    //plugin
    private final Plugin plugin;



    //constructor. Getting the plugin from the Bedwars class.
    public WorldDataJsonParser()
    {
        plugin = BedWars.getInstance();
    }

    /*
    Gets the arena object for the game.
    Throws IOException, IllegalArgument exception if there are problems with reading
     */
    public Arena getArena() throws IOException, IllegalArgumentException {

        //getting the paths to the generator and world Data files.
        String data = FileManager.getDataPath() +"\\"+ FileManager.Files.WORLD_DATA.data;
        String generators = FileManager.getDataPath() +"\\"+  FileManager.Files.GENERATORS.data;

        //turning the paths into files.
        File worldFile = new File(data);
        File generatorFile = new File(generators);

        //get the parent object in world file, throws IOException
        JsonObject parent = getParent(worldFile);

        //the json is formatted as Arena -> Data
        //in the files. If we don't have the arena keyword, then we know that the file is invalid.
        //(basically a check to ensure that the data we have is good)
        if (!parent.has(Keyword.ARENA.data))
            throw new IllegalArgumentException("Json file for world data does not contain Arena object: "+data);

        //if we cannot find the generator file
        if (!generatorFile.exists())
            throw new FileNotFoundException("Json file for generators does not exist at path: "+generators);

        //json object for the generators
        JsonObject jsonGenerators = getParent(generatorFile);

        try {
            //we are getting the object within the Arena object to get the data.
            parent = parent.getAsJsonObject(Keyword.ARENA.data);

            //getting the values for the arena.
            World world = parseWorld(parent);
            Coordinate specSpawn = parseSpecSpawn(parent);
            int y = parseY(parent);
            GameBoundary bounds = parseBoundary(parent);

           //(GameBoundary bounds, Coordinate spectatorSpawn, int voidLevel, World world, Plugin plugin)
           Arena arena = new Arena(bounds, specSpawn, y, world, plugin);
           List<Generator> generatorList = build(jsonGenerators,arena);
           arena.setGenerators(generatorList);
           return arena;

        }
        catch (ClassCastException e) {
            //if variable parent cannot be got as a json object, then it is
            //json null, primitive, or array. This means that the data inside is
            //formatted differently, and is invalid.
            throw new IllegalArgumentException("Json data for world is not a json object:"+worldFile+". "+e.getMessage());
        }

    }

    /*
    This method gets the world object from the json data.
    Throws Illegal Argument exception if there is a problem reading
     */
    private World parseWorld(JsonObject parent) throws IllegalArgumentException {

        //If we cannot find the json primitive "Name", then that means we cannot find the world.
        if (!parent.has(Keyword.NAME.data))
            throw new IllegalArgumentException("World name not found in world data. "+Keyword.NAME.data +" not found");

        //Getting the name as the string, and getting the world from the world name
        String name = parent.get(Keyword.NAME.data).getAsString();
        World world = plugin.getServer().getWorld(name);

        // if we cannot find the world, then throw an exception
        if (world == null)
            throw new IllegalArgumentException("Could not find world folder matching name: "+name);

        return world;
    }

    /*
    This method gets the coordinate for the spectator spawn.
    Throws Illegal Argument exception if there is a problem reading
     */
    private Coordinate parseSpecSpawn(JsonObject parent) throws IllegalArgumentException {

        // if we cannot find the keyword for the spectator data, throw an exception
        if (!parent.has(Keyword.SPECTATOR.data))
            throw new IllegalArgumentException("Spectator spawn not found in world data. "+Keyword.SPECTATOR.data +"not found");

        //We now get the json element from the keyword.
        //if it's not a json object, then that means that the data is formatted differently, and we don't know what to read.
        //throw an exception in this case.
        JsonElement elem = parent.get(Keyword.SPECTATOR.data);
        if (! (elem instanceof JsonObject))
            throw new IllegalArgumentException("Expected json object for coordinate data. Got "+elem.getClass().getName());

        //Getting the data in the form of a hashmap, and returning a coordinate. buildCoordinate() throws an exception
        //if the coordinate values are invalid. E.g for x coordinate we get "abc" instead of a number
       Map<String, String> data = parseNormally((JsonObject)elem);
      return JsonBuilder.buildCoordinate(data);

    }

    /*
    This method gets a boundary from a set of data.
    Throws IllegalArgException if there is a problem reading the file
     */
    private GameBoundary parseBoundary(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(Keyword.BOUNDARY.data))
            throw new IllegalArgumentException("Arena boundary not found in world data. "+Keyword.BOUNDARY.data +" not found");

        JsonElement elem = parent.get(Keyword.BOUNDARY.data);
        if (!(elem instanceof JsonObject))
              throw new IllegalArgumentException("Expected json object for boundary data. Got "+elem.getClass().getName());

        //Since boundaries have a From and To, we expect 2 objects to make up the data of 1 boundary.
        Map<String, String> data = parseExpectPhrase((JsonObject)elem);
        return JsonBuilder.buildBoundary(data);
    }



    /*
    This method takes a json object and gets a y level for a void.
    Throws IllegalArgException if there is a problem
     */
    private int parseY(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(Keyword.VOID.data))
            throw new IllegalArgumentException("Void y level not found in world data. "+Keyword.VOID.data+" not found");

        //We get the object, and try to get it as a primitive.
        //if it is not primitive, then it is null, an object, or array, which is not good
        //since we are only trying to get 1 value for y
        JsonElement elem = parent.get(Keyword.VOID.data);
        if (!(elem instanceof JsonPrimitive))
            throw new IllegalArgumentException("Expected json primitive for y level. Got "+elem.getClass().getName());

        return JsonBuilder.toInt(elem.getAsString());

    }



    /*
    Gets a generator type from a json object for use in constructing generators.
    Throws an exception if there is an issue.
     */
    private GeneratorType parseType(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(GenKeyword.GEN_TYPE.data))
            throw new IllegalArgumentException("Generator type not found in generator data. "+GenKeyword.GEN_TYPE.data+"not found");

        // type should be DIAMOND or EMERALD
        String type = parent.get(GenKeyword.GEN_TYPE.data).getAsString().toUpperCase();
        return GeneratorType.valueOf(type);
    }



    /*
    Creates a generator object from a json object and an arena
    Throws an exception if there is an issue.
    TODO Refactor generators to make use of the tier time read from the config
     */
    private Generator construct(JsonObject parent, Arena arena) throws IllegalArgumentException {

            Coordinate location = parseGenLocation(parent);
            int[] tierTimes = parseTierTime(parent);
            GeneratorType type = parseType(parent);
            GameBoundary bounds = parseBoundary(parent);

            //Coordinate location, World world, GeneratorType type, Plugin plugin, GameBoundary box
            return new Generator(location, arena.getWorld(), type, BedWars.getInstance(), bounds);


    }


    /*
    Attempts to read from a json object, the spawn location of the generator.
    Takes a json object with the Location tag and a json object content
     */
    private Coordinate parseGenLocation(JsonObject parent) throws IllegalArgumentException {
        if (!parent.has(GenKeyword.LOCATION.data)) {

            throw new IllegalArgumentException("Could not find location data in generator data. \"" + GenKeyword.LOCATION.data + "\" not found");

        }
        JsonElement elem = parent.get(GenKeyword.LOCATION.data);
        if (!(elem instanceof JsonObject))
            throw new IllegalArgumentException("Expected json object for coordinate data. Got "+elem.getClass().getName());

        HashMap<String, String> data = parseNormally((JsonObject) elem);
        return JsonBuilder.buildCoordinate(data);
    }



    /*
    Returns an array of the time it takes for a generator to spawn an item
    Takes a json object with the Speeds tag and content of a json object
    Array length is always 3, for there are 3 tiers to a generator
     */
    private int[] parseTierTime(JsonObject parent) {

        ChatSender sender = ChatSender.getInstance();


        if (!parent.has(GenKeyword.SPEEDS.data))
            throw new IllegalArgumentException("Generator speeds not found in generator data. "+GenKeyword.SPEEDS.data+" not found.");

        JsonElement elem = parent.get(GenKeyword.SPEEDS.data);
        if (!(elem instanceof JsonObject)) {
            throw new IllegalArgumentException("Expected json object for generator times. Got "+elem.getClass().getName());
        }
        parent = (JsonObject) elem;

        int[] generatorTimes = new int[]{-1, -1, -1};

        Set<Map.Entry<String, JsonElement>> elems = parent.entrySet();
        for (Map.Entry<String, JsonElement> entry : elems) {

           String key = entry.getKey();
           char c = key.charAt(key.length() -1);
           String value = entry.getValue().toString();


           if (!Character.isDigit(c))
               continue;

            try {
                int tier = Integer.parseInt(c+"");
                int time = JsonBuilder.toInt(value);
                if (time <= 0)
                    throw new IllegalArgumentException("Time must be greater than 0 for generators");

                if (generatorTimes[tier-1] == -1)
                    generatorTimes[tier-1] = time;

            }
            catch (NumberFormatException | IndexOutOfBoundsException e) {
                sender.sendConsoleMessage("Tried to convert "+c+" to generator tier ", Level.WARNING);
                sender.sendConsoleMessage(e.getMessage(),Level.WARNING);
            }
            catch (IllegalArgumentException e) {
                sender.sendConsoleMessage("Tried to convert "+value+" to a generator time.", Level.WARNING);
                sender.sendConsoleMessage(e.getMessage(),Level.WARNING);
            }
        }


        for (int slot = 0 ; slot < generatorTimes.length ; slot ++) {
            if (generatorTimes[slot] <= 0)
                throw new IllegalArgumentException("Missing time for generator at tier "+(slot+1)+" for generator");
        }

        return generatorTimes;
    }


    /*
       Builds a list of generators from data read from json files
       If a generator has bad data, it is skipped
       Returns: list of generators
     */
    private List<Generator> build(JsonObject parent, Arena arena) throws IllegalArgumentException{

        ChatSender sender = ChatSender.getInstance();
        List<Generator> generators = new ArrayList<>();

        if (!parent.has(GenKeyword.GENERATORS.data))
            throw new IllegalArgumentException("Could not find Json object "+GenKeyword.GENERATORS.data+" for generators.");

        JsonElement parentElem = parent.get(GenKeyword.GENERATORS.data);
        if (!(parentElem instanceof JsonObject))
            throw new IllegalArgumentException("Expected json object for generator data. Got "+parentElem.getClass().getName());

        parent = (JsonObject)parentElem;

        Set<Map.Entry<String, JsonElement>> elements = parent.entrySet();
        for (Map.Entry<String, JsonElement> entry : elements) {

            JsonElement elem = entry.getValue();
            if (! (elem instanceof JsonObject)) {
                sender.sendConsoleMessage("Malformed json element " + entry.getKey() + " for generators. Skipping.", Level.WARNING);
                continue;
            }

            try {
                JsonElement info = parent.get(entry.getKey());
                if (!(info instanceof JsonObject))
                    throw new IllegalArgumentException("Expected json object, got: "+info.getClass().getName()+" for "+entry.getKey());


                Generator generator = construct((JsonObject)info, arena);
                generators.add(generator);

            }
            catch (IllegalArgumentException e) {
                sender.sendConsoleMessage("Skipping generator \""+entry.getKey()+"\" due to bad data: "+e.getMessage(),Level.WARNING);
            }

        }

        return generators;

    }


}
