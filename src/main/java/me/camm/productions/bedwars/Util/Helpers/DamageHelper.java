package me.camm.productions.bedwars.Util.Helpers;


import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.DeathMessages.Cause;
import me.camm.productions.bedwars.Arena.Players.DeathMessages.DeathFormatter;
import me.camm.productions.bedwars.Entities.ActiveEntities.Hierarchy.IGameTeamable;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageHelper {




    public static void formatDeathMessage(BattlePlayer killer, BattlePlayer victim, IGameTeamable involved, EntityDamageEvent.DamageCause cause, boolean finalKill){

        String message;
        if (killer == null) {

            if (involved == null)
                // is natural death
                message = DeathFormatter.format(victim,DeathFormatter.toNaturalCause(cause));
            else
                message = DeathFormatter.format(victim, Cause.NORMAL);
            //is indirect death w/ no owner

        }
        else
        {

            if (involved == null) {
                Cause type = DeathFormatter.toInitiatedCause(cause);
                if (type == Cause.NORMAL)
                    // is direct death
                    message = DeathFormatter.formatDirectDeath(victim, killer);
                else
                    // is indirect death initiated by player
                    message = DeathFormatter.format(victim,killer,type);

            }
            else
                message = DeathFormatter.format(victim,killer,DeathFormatter.toInitiatedCause(cause),involved);
            //the killer and the initiator are not null

        }

        ChatSender sender = ChatSender.getInstance();
        if (!finalKill)
            sender.sendMessage(ChatColor.YELLOW+message);
        else
            sender.sendMessage(ChatColor.YELLOW+message+ChatColor.AQUA+ChatColor.BOLD+" FINAL KILL!");

    }


    public static void formatDeathVoidNonDirect(BattlePlayer killer, BattlePlayer victim, Cause cause, boolean isFinal){
        String message;

        if (cause != Cause.FIREBALL_VOID && cause != Cause.TNT_VOID && cause != Cause.PROJECTILE_VOID)
            return;

        message = DeathFormatter.format(victim,killer,cause);
        ChatSender sender = ChatSender.getInstance();

        if (!isFinal)
            sender.sendMessage(ChatColor.YELLOW+message);
        else
            sender.sendMessage(ChatColor.YELLOW+message+ChatColor.AQUA+""+ChatColor.BOLD+" FINAL KILL!");
    }





}
