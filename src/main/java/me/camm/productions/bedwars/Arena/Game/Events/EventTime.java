package me.camm.productions.bedwars.Arena.Game.Events;

/**
 * @author CAMM
 * This enum is for the time events that occur in the game
 */
public enum EventTime
{

    TIME_IN_MINUTE(60),

    TICKS(2),
    RUNNABLE_PERIOD(20),

    DIAMOND_TIER_ONE_TIME(30),
    DIAMOND_TIER_TWO_TIME(23),
    DIAMOND_TIER_THREE_TIME(12),

    EMERALD_TIER_ONE_TIME(64),
    EMERALD_TIER_TWO_TIME(50),
    EMERALD_TIER_THREE_TIME(35),



    DIAMOND_UPGRADE_TWO(300), //300
    EMERALD_UPGRADE_TWO(600), //600

    DIAMOND_UPGRADE_THREE(900), //900
    EMERALD_UPGRADE_THREE(1200), //1200

    BED_WARNING_TIME(1500), //1500
    BED_DESTROY_TIME(1800), //1800

    DRAGON_WARNING_TIME(2100), //2100
    DRAGON_SPAWN_TIME(2400), //2400
    GAME_END_WARNING(2700), //2700
    TOTAL_GAME_TIME(3000); //3000


    private final int time;

    EventTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }
}
