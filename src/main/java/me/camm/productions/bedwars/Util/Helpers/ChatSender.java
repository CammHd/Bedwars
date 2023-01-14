package me.camm.productions.bedwars.Util.Helpers;


import me.camm.productions.bedwars.BedWars;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatSender
{
    private final Server server;
    private final Logger logger;
    private static ChatSender sender;
    private ChatSender(Plugin plugin) {
        server = plugin.getServer();
       logger = server.getLogger();
        sender = this;
    }

    public void sendConsoleMessage(String message, Level l){
        logger.log(l,"[BEDWARS] "+message);
    }


    public static ChatSender getInstance(){
        if (sender == null) {
            sender = new ChatSender(BedWars.getInstance());
        }
            return sender;
    }


    public void broadcastMessage(String message, @Nullable GameState state){

        state = state == null ? GameState.RUNNING : state;
        server.broadcastMessage(state.getLevel()+message);
    }

    public void sendMessage(String message) {
        broadcastMessage(message,null);
    }

    public enum GameState {
        ERROR(ChatColor.RED+"[ERROR]"+ChatColor.GRAY),
        INFO(ChatColor.GREEN+"[INFO]"+ChatColor.GRAY),
        RUNNING(ChatColor.AQUA+"[BEDWARS]"+ChatColor.GRAY),
        WARN(ChatColor.YELLOW+"[WARN]"+ChatColor.GRAY);

        private final String level;

        GameState(String level){
            this.level = level;
        }

        public String getLevel(){
            return level;
        }


    }



}
