package me.camm.productions.bedwars.Files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonParser {

    protected final String FROM = "From";
    protected final String TO = "To";

    public enum Identifier {
        X("x"),
        Y("y"),
        Z("z"),
        X1("x1"),
        Y1("y1"),
        Z1("z1"),
        ROTATION("Rotation");

        public final String id;
        Identifier (String id) {
            this.id = id;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean replace(String key, JsonElement elem, JsonObject parent){
        try {
            Field map = JsonObject.class.getDeclaredField("members");
            map.setAccessible(true);
            LinkedTreeMap<String, JsonElement> tree = (LinkedTreeMap<String, JsonElement>) map.get(parent);
            map.setAccessible(false);
            tree.replace(key, elem);

            return true;

        }
        catch (Exception e) {
            return false;
        }
    }

    public static JsonObject getParent(File file) throws IOException {
        try {
            return (JsonObject) new com.google.gson.JsonParser()
                    .parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        }
        catch (RuntimeException e) {
            throw new IOException("Error parsing json in file:  "+file.getName()+": "+e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static JsonElement getEntry(String key, JsonObject parent) {
        if (!parent.has(key))
            throw new IllegalArgumentException("Parent does not contain the key");

        try
        {
            Field field = JsonObject.class.getDeclaredField("members");
            field.setAccessible(true);
            LinkedTreeMap<String, JsonElement> tree = (LinkedTreeMap<String, JsonElement>) field.get(parent);
            field.setAccessible(false);


            return tree.get(key);

        }
        catch (Exception ignored) {

        }

        return null;

    }



    protected HashMap<String, String> parseNormally(JsonObject parent){
        HashMap<String, String> entries = new HashMap<>();
        getMapEntries(entries, parent);
        return entries;
    }



    protected HashMap<String, String> parseExpectPhrase(JsonObject parent) {
        HashMap<String, String> entries = new HashMap<>();
        JsonObject from = parent.getAsJsonObject(FROM);
        JsonObject to = parent.getAsJsonObject(TO);

        getMapEntries(entries, from);
        getMapEntries(entries, to);
        return entries;
    }



    protected void getMapEntries(HashMap<String, String> entries, JsonObject o) {
        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            entries.put(entry.getKey(), entry.getValue().toString());
        }
    }

}
