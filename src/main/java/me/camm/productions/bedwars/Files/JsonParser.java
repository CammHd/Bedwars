package me.camm.productions.bedwars.Files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public static JsonObject getParent(File file) throws IOException {
        try {
            return (JsonObject) new com.google.gson.JsonParser()
                    .parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        }
        catch (RuntimeException e) {
            throw new IOException("Error parsing json in file:  "+file.getName()+": "+e.getMessage());
        }
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
