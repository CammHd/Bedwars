package me.camm.productions.bedwars.Game.DeathMessages;


/**
 * @author CAMM
 * Enum for direct death messages.
 * format is [player] (something) by [killer]
 */
public enum DirectDeathMessage
{
    //a direct death is where a player directly kills another player,
    // either with a bow or melee or explosives
    FRIED("was deep fried by"),
    GRAVY_SAUCE("slipped in gravy sauce spilled by"),
    TOOK_L("took the L to"),
    COVID("caught a cold from"),
    NORMAL("died to"),
    FLIPPED("got Bit Flipped by"),
    AMONGUS("played too much Among us because of"),
    CHOPPED("was chuffed to bits by"),
    REJECTED("was rejected by"),
    MATH("'s mind was frizzled by a math test given by"),
    FAILED_ENG(" f4Iled there engrish coarse doo 2"),
    STAND_UP("stood up too fast due to"),
    EATEN("was eaten by"),
    PHASED("was phased out of existence by"),
    REVERSED("forgot to bring uno reverse cards when fighting"),
    TRAVELLER("was a traveller like you, but then they were killed by"),

    CIRCUITS("'s circuits got fried by"),
    OFF_SWITCH("'s off switch was found by"),

    NO_SCOPED("was no-scoped by"),
    SCAMMED("was scammed by"),

    BACK_STAB("was back-stabbed by"),
    ATE_COOKIE("ate a bad cookie set out by"),
    SPOOKED("was spooked by"),
    BANANA_PEEL("slipped on a banana peel dropped by"),
    BONKED("was bonked by"),
    BAMBOOZLED("was bamboozled by"),
    WATCH_DOG("was snitched to the teacher by"),
    BECAME_DEV("became a Plugin Developer because of");

    private final String message;

    DirectDeathMessage(String message)
    {
        this.message = message;
    }


    public String getMessage() {
        return message;
    }


}
