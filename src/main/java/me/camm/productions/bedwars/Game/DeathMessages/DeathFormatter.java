package me.camm.productions.bedwars.Game.DeathMessages;

import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy.IGameTeamable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

/**
 * @author CAMM
 * Class to format death messages
 */
public class DeathFormatter
{

        /*

Formats:

    victim was deathCause by killer's minion

    victim deathCauseNatural

    victim was deathCause by killer

    victim directMessage by killer

     */

    private final static Random rand;
    private final static DirectDeathMessage[] messages;

    static {
        rand = new Random();
        messages = DirectDeathMessage.values();
    }

    public static String format(Player victim){
         return victim.getName()+" died";
    }

    public static String format(BattlePlayer victim, DeathCause deathCause) {
        return victim.getTeam().getTeamColor().getChatColor()+
                victim.getRawPlayer().getName()+ChatColor.RESET+" "+ deathCause.format()+".";
    }

    public static String formatDirectDeath(BattlePlayer victim, BattlePlayer killer){
        return victim.getTeam().getTeamColor().getChatColor()+victim.getRawPlayer().getName()+
                ChatColor.RESET+" "+messages[rand.nextInt(messages.length)].getMessage()+" "+
                killer.getTeam().getTeamColor().getChatColor()+killer.getRawPlayer().getName()
                +ChatColor.RESET+".";
    }

    public static String format(Player victim, IGameTeamable teamable){
        return victim.getName()+" "+ DeathCause.NORMAL.format()+" by "+teamable.getType()+".";
    }


    public static String format(BattlePlayer victim, BattlePlayer killer, DeathCause deathCause){
        return victim.getTeam().getTeamColor().getChatColor()+
                victim.getRawPlayer().getName()+ChatColor.RESET+" "+ deathCause.format()+" by "+
                killer.getTeam().getTeamColor().getChatColor()+killer.getRawPlayer().getName()+
                ChatColor.RESET+".";
    }

   public static String format(BattlePlayer victim, BattlePlayer killer, DeathCause deathCause, IGameTeamable initiator){
        return victim.getTeam().getTeamColor().getChatColor()+
                victim.getRawPlayer().getName()+ChatColor.RESET+" "+ deathCause.format()+" by "+
                killer.getTeam().getTeamColor().getChatColor()+
                killer.getRawPlayer().getName()+"'s "+initiator.getType()+ChatColor.RESET+".";
   }

   public static String format(Player victim, Player killer, IGameTeamable teamable){
        return victim.getName()+" "+messages[rand.nextInt(messages.length)].getMessage()+" "+killer.getName()+"'s "+teamable.getType();
   }


   //this is for natural actions
   public static DeathCause toNaturalCause(EntityDamageEvent.DamageCause cause){
        DeathCause damage = DeathCause.NORMAL;

        switch (cause) {
            case VOID:
                damage = DeathCause.VOID_NATURAL;
                break;

            case FALL:
                damage = DeathCause.FALL_NATURAL;
                break;

            case FIRE:
            case LAVA:
            case FIRE_TICK:
                damage = DeathCause.HEAT;
                break;

            case DROWNING:
                damage = DeathCause.WATER_NATURAL;
                break;

            case SUFFOCATION:
                damage = DeathCause.SUFFOCATE_NATURAL;
                break;
        }

        return damage;
   }

    //this is not for entity actions, only the end action
   public static DeathCause toInitiatedCause(EntityDamageEvent.DamageCause cause){
        DeathCause damage = DeathCause.NORMAL;

        switch (cause) {
            case DROWNING:
                damage = DeathCause.WATER;
                break;

            case FIRE_TICK:
            case LAVA:
            case FIRE:
                damage = DeathCause.HEAT;
                break;

            case VOID:
                damage = DeathCause.VOID;
                break;

            case SUFFOCATION:
                damage = DeathCause.SUFFOCATE;
                break;

            case FALL:
                damage = DeathCause.FALL;
                break;
        }
        return damage;

   }

}


