package me.camm.productions.bedwars.Util.Helpers;

import me.camm.productions.bedwars.Arena.Game.Events.*;
import me.camm.productions.bedwars.Arena.Game.GameRunner;
import static me.camm.productions.bedwars.Arena.Game.Events.EventTime.*;
import me.camm.productions.bedwars.Arena.Game.Events.GameEventText;

import static me.camm.productions.bedwars.Arena.Players.Scoreboards.ScoreBoardHeader.*;
import static me.camm.productions.bedwars.Generators.GeneratorType.*;

import java.util.ArrayList;


@Deprecated
public class EventBuilder
{
    public static ArrayList<ActionEvent> build(GameRunner runner) {
        ArrayList<ActionEvent> times = new ArrayList<>();
        times.add(new ActionEvent(DIAMOND_UPGRADE_TWO.getTime(),new GeneratorUpgradeAction(runner,DIAMOND,DIAMOND_TWO_HEADER.getPhrase())));
        times.add(new ActionEvent(EMERALD_UPGRADE_TWO.getTime(), new GeneratorUpgradeAction(runner,EMERALD,EMERALD_TWO_HEADER.getPhrase())));

        times.add(new ActionEvent(DIAMOND_UPGRADE_THREE.getTime(), new GeneratorUpgradeAction(runner,DIAMOND,DIAMOND_THREE_HEADER.getPhrase())));
        times.add(new ActionEvent(EMERALD_UPGRADE_THREE.getTime(), new GeneratorUpgradeAction(runner,EMERALD,EMERALD_THREE_HEADER.getPhrase())));

        times.add(new ActionEvent(BED_WARNING_TIME.getTime(), new ChatAction(GameEventText.BED_DESTROY_SCHEDULED_WARNING.getText())));
        times.add(new ActionEvent(BED_DESTROY_TIME.getTime(), new BedBreakAction(runner)));

        times.add(new ActionEvent(DRAGON_WARNING_TIME.getTime(), new ChatAction(GameEventText.DRAGON_SPAWN_SCHEDULED_WARNING.getText())));
        times.add(new ActionEvent(DRAGON_SPAWN_TIME.getTime(), new DragonSpawnAction(runner)));

        times.add(new ActionEvent(GAME_END_WARNING.getTime(), new ChatAction(GameEventText.GAME_END_SCHEDULED_WARNING.getText())));
        times.add(new ActionEvent(TOTAL_GAME_TIME.getTime(), new GameEndAction(runner)));


     return times;
    }


}
