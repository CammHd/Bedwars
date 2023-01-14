package me.camm.productions.bedwars.Files;

import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/*
 * @author CAMM
 * Convenience class for writing to files
 */
@Deprecated
public class GameFileWriter {
    private BufferedWriter writer;
    private final File file;
    private final Plugin plugin;
    private final ChatSender sender;

    public GameFileWriter(String path, Plugin plugin) {
        file = new File(path);
        this.plugin = plugin;
        sender = ChatSender.getInstance();
    }

    //clears the file
    public void clear() {
        try {
            writer = new BufferedWriter(new FileWriter(file, false));
            writer.write("");
            writer.close();

        } catch (IOException ignored) {

        }
    }


    //writes to the file.
    //If delete is true, then overwrites what was in the file before
    public void write(ArrayList<String> lines, boolean delete) {
        try {
            writer = new BufferedWriter(new FileWriter(file, !delete));
            for (String s : lines) {
                writer.write(s + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
