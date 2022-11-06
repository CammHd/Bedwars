package me.camm.productions.bedwars.Files;

import com.google.gson.JsonObject;
import me.camm.productions.bedwars.Arena.GameRunning.Arena;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.TeamColor;
import me.camm.productions.bedwars.Generators.Forge;
import me.camm.productions.bedwars.Util.Locations.Boundaries.GameBoundary;
import me.camm.productions.bedwars.Util.Locations.Coordinate;



import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class TeamFileJsonParser extends JsonParser {

    enum Keyword {
        FORGE("Forge",false),
        PLAYER_SPAWN("Player Spawn",false),
        CHEST("Team Chest",false),
        BED_LOCATION("Bed Location",true),
        HEAL_POOL("Heal Pool Area",true),
        QUICK_BUY("Quick Buy Merchant",false),
        TEAM_BUY("Team Upgrades Merchant",false),
        RESTRICT_PLACE("Restricted Place Area",true),
        TRAP_TRIGGER("Trap Trigger Area",true);

       final String keyword;
       final boolean expectPhrase;
        Keyword(String word, boolean expectPhrase) {
            keyword = word;
            this.expectPhrase = expectPhrase;

        }

    }

    public enum DataHolder {
        CREATE_TEAM("Create Team"),
        FORGE_SPEED("Forge Speed"),
        FORGE_PICKUP("Forge Pickup Distance");

        public final String data;

        DataHolder(String data) {
            this.data = data;
        }

    }

    private final Arena arena;
    private final Map<String, TeamColor> colors;

    public TeamFileJsonParser(Arena arena) {
    this.arena = arena;

    colors = new HashMap<>();
    for (TeamColor color: TeamColor.values()) {
        colors.put(color.getName().toLowerCase(), color);
      }
    }

    public ArrayList<BattleTeam> getTeams() throws IllegalArgumentException {

        String path = FileManager.getDataPath() + FileManager.Directories.TEAMS.data;
        ArrayList<BattleTeam> teams = new ArrayList<>();

        for (TeamColor color : TeamColor.values()) {
            String team = path + color.getName() + ".json";
            File file = new File(team);

            if (file.exists()) {
                BattleTeam t = construct(file);
                if (t == null)
                    continue;

                teams.add(t);

            }
        }
        return teams;
    }

    private BattleTeam construct(File file) throws IllegalArgumentException {
       final String TEAM = "Team";

        String[] values = file.getName().split("[.]");

        if (values.length == 0)
            throw new IllegalArgumentException("Tried to construct team but data file has no name!");

        String color = values[0].toLowerCase();
        if (!colors.containsKey(color))
            throw new IllegalArgumentException("Tried to construct team but could not resolve team color "+color);

        TeamColor teamColor = colors.get(color);

        try {
            JsonObject parent = getParent(file);
      if (!parent.has(TEAM))
             throw new IllegalArgumentException("File data is invalid :"+file.getName() +" " +
                     "json does not contain Team object");
        parent = (JsonObject) parent.get(TEAM);

        boolean construct = parent.has(DataHolder.CREATE_TEAM.data) &&
                parent.get(DataHolder.CREATE_TEAM.data).getAsBoolean();

        if (!construct)
            return null;

        return construct(parent,teamColor);

        }
        catch (IOException | ClassCastException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }


    private BattleTeam construct(JsonObject parent, TeamColor color) throws IllegalArgumentException {
        Forge forge;
        Coordinate chest, quickbuy, teambuy, spawn;
        GameBoundary restrictedPlace, healPool, trapArea, bed;

        forge = null;
        chest = quickbuy = teambuy = spawn = null;
        restrictedPlace = healPool = trapArea = bed = null;


        for (Keyword key: Keyword.values()) {

            HashMap<String, String> entries;
            if (key.expectPhrase) {
                entries = parseExpectPhrase(parent);
            }
            else entries = parseNormally(parent);

            switch (key) {
                case CHEST: {
                    chest = JsonBuilder.buildCoordinate(entries);
                }
                    break;

                case FORGE:
                    forge = JsonBuilder.buildForge(entries, arena.getWorld(),color);
                    break;

                case TEAM_BUY:
                    teambuy = JsonBuilder.buildCoordinate(entries);
                    break;

                case HEAL_POOL:
                    healPool = JsonBuilder.buildBoundary(entries);
                    break;

                case QUICK_BUY:
                    quickbuy = JsonBuilder.buildCoordinate(entries);
                    break;

                case BED_LOCATION:
                    bed = JsonBuilder.buildBoundary(entries);
                    break;

                case PLAYER_SPAWN:
                    spawn = JsonBuilder.buildCoordinate(entries);
                    break;

                case TRAP_TRIGGER:
                    trapArea = JsonBuilder.buildBoundary(entries);
                    break;

                case RESTRICT_PLACE:
                    restrictedPlace = JsonBuilder.buildBoundary(entries);
                    break;
            }

        }

        if (forge == null || spawn == null || bed == null
            || chest == null || quickbuy == null || teambuy == null ||
                restrictedPlace == null || healPool == null || trapArea == null) {
         throw new IllegalArgumentException("A value is invalid for the team "+color.getName());
        }


        return new BattleTeam(arena,color,forge,spawn,bed,chest,quickbuy,teambuy,restrictedPlace,healPool,trapArea);
    }




}


