package me.camm.productions.bedwars.Game.DeathMessages;

import java.util.Random;

/**
 * @author CAMM
 * Enum for the possible death messages in the game
 * This enum is for natural causes and indirect causes
 * natural cause: E.g you slip off the edge. the fall kills you
 * indirect cause: E.g someone hits you off the edge. The fall kills you, the hit doesn't
 *
 */
public enum DeathCause {

    VOID("the void",new String[]{"was thrown into","was knocked into","was shoved into","was yeeted into"}),
    VOID_NATURAL("the void",new String[]{"fell into","stumbled into","tripped into"}),
    FIREBALL(null,new String[]{"was fireballed"}),
    FIREBALL_VOID("the void", new String[]{"was fireballed into"}),
    SUFFOCATE("a wall",new String[]{"was suffocated in"}),
    SUFFOCATE_NATURAL("a wall",new String[]{"suffocated in"}),
    WATER_NATURAL(null,new String[]{"drowned"}),
    WATER(null,new String[]{"was drowned"}),
    NORMAL(null,new String[]{"was killed"}),
    PROJECTILE_VOID("the void", new String[]{"was shot into"}),
    FALL("high place",new String[]{"was knocked from a","was pushed off a","was doomed to fall from a"}),
    FALL_NATURAL("high place",new String[]{"fell from a","slipped from a","fell off a"}),
    HEAT(null,new String[]{"was roasted (literally and figuratively)","was cooked","was burned to a crisp"}),
    TNT_VOID("the void",new String[]{"was blown into"}),
    TNT(null,new String[]{"was blown up"});

    private final String name;
    private final String[] leads;
    private final static Random rand;

    DeathCause(String name, String[] leads){
        this.name = name;
        this.leads = leads;
    }

    static {
        rand = new Random();
    }

    public String randomLead(){
        return leads[rand.nextInt(leads.length)];
    }

    //gets a random combination of components to build and returns the string
    public String format(){
        if (name == null) {
            return randomLead();
        }
        else
            return randomLead() +" "+ name;
    }

    public String getName(){
        return name;
    }
}
