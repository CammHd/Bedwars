package me.camm.productions.bedwars.Arena.GameRunning.Commands;

import me.camm.productions.bedwars.Arena.GameRunning.Arena;
import me.camm.productions.bedwars.Arena.GameRunning.GameRunner;

import me.camm.productions.bedwars.Util.Helpers.ChatSender;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.logging.Level;


/*
 This class is used to set-up parameters for the game.
 @author CAMM
 */
public class GameIntializer implements CommandExecutor
{
    private final Plugin plugin;
    private static Arena arena;
    private GameRunner runner;
    CommandProcessor processor;
    private final ChatSender messager;
    private final HashMap<String, CommandKeyword> words;

    //construct
    public GameIntializer(Plugin plugin)
    {

       this.plugin = plugin;
       arena = null;
       runner = null;
       processor = new CommandProcessor();
       messager = ChatSender.getInstance();
       words = new HashMap<>();

       for (CommandKeyword word: CommandKeyword.values())
           words.put(word.getWord(),word);


    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {

        //make sure the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED+"Must be a player to use this command.");
            return true;
        }

        //try to get the enum value from the label.
        label = label.toLowerCase().trim();
        CommandKeyword word = words.getOrDefault(label, null);

        //if we cannot find a matching word, return
        if (word  == null) {
            return false;
        }


//depending on the word, we do different things

        try {
            switch (word) {
                case SETUP:


                    if (runner != null) {
                        if (runner.isRunning()) {
                            sender.sendMessage("You must end the game manually first.");
                           break;
                        }
                    }

                    if (arena != null) {
                        sender.sendMessage("Overriding previous set up for the arena.");
                        arena.unregisterMap();
                    }



                    runner = processor.initRunner(sender, plugin);
                    arena = runner.getArena();
                    break;

                case SHOUT:
                   processor.shout(sender,args);
                    break;

                case REGISTER:
                    processor.registerPlayer((Player)sender);
                    break;

                case START:
                   processor.startGame(sender);
                    break;

                case UNREGISTER:
                  processor.unregister(sender);
                    break;

                case END:
                 processor.manualEndGame(sender);
                    break;
            }
        }
        catch (Exception e) {
                messager.sendConsoleMessage(e.getMessage(), Level.WARNING);
        }
        return true;
    }


    //getters
    public Arena getArena()
    {
        return arena;
    }

    public GameRunner getRunner()
    {
        return runner;
    }
}


