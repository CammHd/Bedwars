package me.camm.productions.bedwars.Game;


import me.camm.productions.bedwars.Game.Commands.CommandKeyword;
import me.camm.productions.bedwars.Game.Commands.CommandProcessor;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;

import me.camm.productions.bedwars.Util.Exceptions.BedWarsException;
import me.camm.productions.bedwars.Util.Exceptions.CommandException;
import me.camm.productions.bedwars.Util.Exceptions.CommandPermissionException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.logging.Level;


/*
 This class is used to set-up parameters for the game.
 @author CAMM
 */
public class GameInitializer implements CommandExecutor
{
    private final Plugin plugin;
    private static Arena arena;
    private GameRunner runner;
    CommandProcessor processor;
    private final ChatSender messager;
    private final HashMap<String, CommandKeyword> words;

    //construct
    public GameInitializer(Plugin plugin)
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

    public void reset(){
        arena = null;
        runner = null;
        this.processor = new CommandProcessor();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {

        //try to get the enum value from the label.
        label = label.toLowerCase().trim();
        CommandKeyword word = words.getOrDefault(label, null);

        //if we cannot find a matching word, return
        if (word  == null) {
            sender.sendMessage(command.getUsage());
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

                    runner = processor.initRunner(sender, plugin,this);
                    arena = runner.getArena();
                    break;

                case SHOUT:
                   processor.shout(sender,args);
                    break;

                case REGISTER:

                    processor.registerPlayer(sender);
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
        catch (CommandPermissionException e) {
            sender.sendMessage(command.getPermissionMessage());
            sender.sendMessage("You require permission node: "+command.getPermission());
        }
        catch (CommandException e) {
                sender.sendMessage(e.getMessage());
        }
        catch (BedWarsException e) {
            sender.sendMessage(ChatColor.RED+e.getMessage());
            messager.sendConsoleMessage(e.getMessage(),Level.WARNING);
        }
        catch (Exception e) {
            sender.sendMessage(ChatColor.RED+"Error occurred trying to execute command \""+label+"\". Check the console for more info.");
            messager.sendConsoleMessage(e.getMessage(),Level.WARNING);
            e.printStackTrace();
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


