package me.camm.productions.bedwars.Files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.camm.productions.bedwars.Arena.GameRunning.Arena;
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


public class WorldDataJsonParser extends JsonParser
{


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


    enum GenKeyword {
        GEN_TYPE("Type",false),
        LOCATION("Location",false),
        SPEEDS("Tier Speeds",false);

        final String data;
        final boolean expectPhrase;

        GenKeyword(String data, boolean expectPhrase) {
            this.data = data;
            this.expectPhrase = expectPhrase;
        }
    }

    private final Plugin plugin;



    public WorldDataJsonParser()
    {
        plugin = BedWars.getPlugin();
    }

    public Arena getArena() throws IOException, IllegalArgumentException {

        String data = FileManager.getDataPath() + FileManager.Files.WORLD_DATA.data;
        String generators = FileManager.getDataPath() + FileManager.Files.GENERATORS.data;

        File worldFile = new File(data);
        File generatorFile = new File(generators);

        JsonObject parent = getParent(worldFile);
        if (!parent.has(Keyword.ARENA.data))
            throw new IllegalArgumentException("Json file for world data does not contain Arena object: "+data);

        if (!generatorFile.exists())
            throw new FileNotFoundException("Json file for generators does not exist at path: "+generators);

        JsonObject jsonGenerators = getParent(generatorFile);


        try {
            parent = parent.getAsJsonObject(Keyword.ARENA.data);

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
            throw new IllegalArgumentException("Json data for world is not a json object:"+worldFile);
        }

    }

    private World parseWorld(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(Keyword.NAME.data))
            throw new IllegalArgumentException("World name not found in world data. "+Keyword.NAME.data +" not found");

        String name = parent.get(Keyword.NAME.data).getAsString();
        World world = plugin.getServer().getWorld(name);

        if (world == null)
            throw new IllegalArgumentException("Could not find world folder matching name: "+name);

        return world;
    }

    private Coordinate parseSpecSpawn(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(Keyword.SPECTATOR.data))
            throw new IllegalArgumentException("Spectator spawn not found in world data. "+Keyword.SPECTATOR.data +"not found");

        JsonElement elem = parent.get(Keyword.SPECTATOR.data);
        if (! (elem instanceof JsonObject))
            throw new IllegalArgumentException("Expected json object for coordinate data. Got "+elem.getClass().getName());

       Map<String, String> data = parseNormally((JsonObject)elem);
      return JsonBuilder.buildCoordinate(data);

    }

    private GameBoundary parseBoundary(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(Keyword.BOUNDARY.data))
            throw new IllegalArgumentException("Arena boundary not found in world data. "+Keyword.BOUNDARY.data +" not found");

        JsonElement elem = parent.get(Keyword.BOUNDARY.data);
        if (!(elem instanceof JsonObject))
              throw new IllegalArgumentException("Expected json object for boundary data. Got "+elem.getClass().getName());

        Map<String, String> data = parseExpectPhrase((JsonObject)elem);
        return JsonBuilder.buildBoundary(data);
    }



    private int parseY(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(Keyword.VOID.data))
            throw new IllegalArgumentException("Void y level not found in world data. "+Keyword.VOID.data+" not found");

        JsonElement elem = parent.get(Keyword.VOID.data);
        if (!(elem instanceof JsonPrimitive))
            throw new IllegalArgumentException("Expected json primitive for y level. Got "+elem.getClass().getName());

        return JsonBuilder.toInt(elem.getAsString());

    }



    //@param collection -> the object that is at the lowermost level enclosing generator data
    // E.g generators -> generator1

    private GeneratorType parseType(JsonObject parent) throws IllegalArgumentException {

        if (!parent.has(GenKeyword.GEN_TYPE.data))
            throw new IllegalArgumentException("Generator type not found in generator data. "+GenKeyword.GEN_TYPE.data+"not found");

        // type should be DIAMOND or EMERALD
        String type = parent.get(GenKeyword.GEN_TYPE.data).getAsString().toUpperCase();
        return GeneratorType.valueOf(type);
    }



    private Generator construct(JsonObject parent, Arena arena) throws IllegalArgumentException {


            Coordinate location = parseGenLocation(parent);
            int[] tierTimes = parseTierTime(parent);
            GeneratorType type = parseType(parent);
            GameBoundary bounds = parseBoundary(parent);

            //Coordinate location, World world, GeneratorType type, Plugin plugin, GameBoundary box
            return new Generator(location, arena.getWorld(), type, BedWars.getPlugin(), bounds);


    }


    private Coordinate parseGenLocation(JsonObject parent) throws IllegalArgumentException {
        if (!parent.has(GenKeyword.LOCATION.data))
            throw new IllegalArgumentException("Could not find location data in generator data. "+GenKeyword.GEN_TYPE.data+" not found");

        JsonElement elem = parent.get(GenKeyword.LOCATION.data);
        if (!(elem instanceof JsonObject))
            throw new IllegalArgumentException("Expected json object for coordinate data. Got "+elem.getClass().getName());

        HashMap<String, String> data = parseNormally((JsonObject) elem);
        return JsonBuilder.buildCoordinate(data);
    }



    private int[] parseTierTime(JsonObject parent) {

        ChatSender sender = ChatSender.getInstance();


        if (!parent.has(GenKeyword.SPEEDS.data))
            throw new IllegalArgumentException("Generator speeds not found in generator data. "+GenKeyword.SPEEDS.data+" not found.");

        int[] values = new int[]{-1, -1, -1};

        Set<Map.Entry<String, JsonElement>> elems = parent.entrySet();
        for (Map.Entry<String, JsonElement> entry : elems) {

           String key = entry.getKey();
           char c = key.charAt(key.length() -1);
           String value = entry.getValue().getAsString();


           if (!Character.isDigit(c))
               continue;

            try {
                int tier = Integer.parseInt(c+"");
                int time = JsonBuilder.toInt(value);
                if (time <= 0)
                    throw new IllegalArgumentException("Time must be greater than 0 for generators");

                if (values[tier-1] == -1)
                    values[tier-1] = time;

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


        for (int slot = 0 ; slot < values.length ; slot ++) {
            if (values[slot] < 0)
                throw new IllegalArgumentException("Missing time for generator at tier "+(slot+1)+" for generator");
        }

        return values;
    }


    private List<Generator> build(JsonObject parent, Arena arena){

        ChatSender sender = ChatSender.getInstance();
        List<Generator> generators = new ArrayList<>();
        Set<Map.Entry<String, JsonElement>> elements = parent.entrySet();
        for (Map.Entry<String, JsonElement> entry : elements) {

            JsonElement elem = entry.getValue();
            if (! (elem instanceof JsonObject)) {
                sender.sendConsoleMessage("Malformed json element " + entry.getKey() + " for generators. Skipping.", Level.WARNING);
                continue;
            }

            try {
                Generator generator = construct(parent, arena);
                generators.add(generator);

            }
            catch (IllegalArgumentException e) {
                sender.sendConsoleMessage("Skipping generator "+entry.getKey()+" due to bad data: "+e.getMessage(),Level.WARNING);
            }

        }

        return generators;

    }


}
