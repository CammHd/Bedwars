package me.camm.productions.bedwars.Entities.ActiveEntities;

import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.DeathMessages.Cause;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Entities.ActiveEntities.Hierarchy.IGameOwnable;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

/**
 * @author CAMM
 * Models a piece of tnt that is exploding.
 */
public class GameTNT implements IGameOwnable
{
    private final BlockPlaceEvent event;
    private TNTPrimed tnt;
    private final BattlePlayer owner;
    private final BattleTeam team;

    private static final Random random = new Random();


    public GameTNT(BlockPlaceEvent event, BattlePlayer player)
    {
        this.event = event;
        this.owner = player;
        this.team = player.getTeam();
        spawn();
    }

    private void spawn()
    {
        Block block = event.getBlockPlaced();
        World world;
        if (event.isCancelled())  //if the event is not cancelled by another plugin [Register block]
            return;


        if (block.getType()!= Material.TNT)
            return;


                world = block.getWorld();
                block.setType(Material.AIR);
                tnt = world.spawn(block.getLocation().add(0.5,0,0.5), TNTPrimed.class);

               //
                tnt.setCustomName(owner.getRawPlayer().getUniqueId().toString());

                tnt.setFuseTicks(54);
                tnt.setYield(0F); // set yield to 0 so it doesn't interfere with velocity (originally 6)

                //This is basically random velocity for the tnt.
                tnt.setVelocity(new Vector( ((random.nextDouble()*0.3)-0.15)*0.2,0.3, ((random.nextDouble()*0.3)-0.15)*0.2));


    }

    @Override
    public UUID getUUID() {
        return tnt == null ? null : tnt.getUniqueId();
    }

    @Override
    public BattleTeam getTeam() {
        return team;
    }

    @Override
    public String getType() {
        return "tnt";
    }

    @Override
    public Cause getCauseType() {
        return Cause.TNT;
    }

    @Override
    public BattlePlayer getOwner() {
        return owner;
    }

    public String getName() {
        return tnt == null ? null: tnt.getCustomName();
    }
}
