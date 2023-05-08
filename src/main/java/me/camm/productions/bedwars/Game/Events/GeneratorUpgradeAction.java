package me.camm.productions.bedwars.Game.Events;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.GameRunner;
import me.camm.productions.bedwars.Generators.Generator;
import me.camm.productions.bedwars.Generators.GeneratorType;

/**
 * @author CAMM
 * This game event action is used to upgrade generators by 1 level
 */
public class GeneratorUpgradeAction extends GameActionPhysical
{

    private final GameRunner runner;
    private final GeneratorType type;

    /**
     *
     * @param runner game runner
     * @param type the generator type to upgrade
     * @param header the header to show on the scoreboards AS the time (in the wrapper class)
     *               approaches the activation time
     */
    public GeneratorUpgradeAction(GameRunner runner, GeneratorType type, String header) {
        this.runner = runner;
        this.type = type;
        spent = false;
        this.header = header;
    }

    @Override
    public void activate()
    {
        if (spent)
            return;

        spent = true;
        Arena arena = runner.getArena();

        int tier = 0;


        //looping through the specified generators and upgrading them
            for (Generator generator : arena.getGenerators())
            {
                if (generator.getGenType()==type)
                {
                    generator.setGeneratorTier(generator.getTier() + 1, generator.getNextNewSpawnTime());
                    tier = generator.getTier();
                }
            }


      //getting the game event text
        String coloredMessage = type == GeneratorType.DIAMOND ?
                GameEventText.UPGRADE_DIAMONDS.getText() : GameEventText.UPGRADE_EMERALDS.getText();

            //sending the upgrade message
        sender.sendMessage(coloredMessage + tier);
    }

    @Override
    String getHeader() {
        return header;
    }
}
