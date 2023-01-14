package me.camm.productions.bedwars.Arena.Game.Events;

import me.camm.productions.bedwars.Arena.Game.GameRunner;
import me.camm.productions.bedwars.Arena.Players.Scoreboards.ScoreBoardHeader;

/**
 * @author CAMM
 * This action ends the game prematurely, invoking a tie condition
 */
public class GameEndAction extends GameActionPhysical
{
    private final GameRunner runner;

    //constructor
    public GameEndAction(GameRunner runner) {
        this.runner = runner;
        header = ScoreBoardHeader.GAME_END_HEADER.getPhrase();
        spent = false;
    }

    //making the runner end the game
    @Override
    public void activate() {
        if (spent)
            return;

        spent = true;
        sender.sendMessage(GameEventText.GAME_END_TEXT.getText());
        runner.endGame(null);
    }

    @Override
    String getHeader() {
        return header;
    }
}
