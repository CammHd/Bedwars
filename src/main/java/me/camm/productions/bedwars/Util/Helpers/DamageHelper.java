package me.camm.productions.bedwars.Util.Helpers;


import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.DeathMessages.DeathCause;
import me.camm.productions.bedwars.Game.DeathMessages.DeathFormatter;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy.IGameTeamable;
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
                message = DeathFormatter.format(victim, DeathCause.NORMAL);
            //is indirect death w/ no owner

        }
        else
        {

            if (involved == null) {
                DeathCause type = DeathFormatter.toInitiatedCause(cause);
                if (type == DeathCause.NORMAL)
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


    public static void formatDeathVoidNonDirect(BattlePlayer killer, BattlePlayer victim, DeathCause deathCause, boolean isFinal){
        String message;

        if (deathCause != DeathCause.FIREBALL_VOID && deathCause != DeathCause.TNT_VOID && deathCause != DeathCause.PROJECTILE_VOID)
            return;

        message = DeathFormatter.format(victim,killer, deathCause);
        ChatSender sender = ChatSender.getInstance();

        if (!isFinal)
            sender.sendMessage(ChatColor.YELLOW+message);
        else
            sender.sendMessage(ChatColor.YELLOW+message+ChatColor.AQUA+""+ChatColor.BOLD+" FINAL KILL!");
    }





}
