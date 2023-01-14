package me.camm.productions.bedwars.Util.Locations.Boundaries;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.TeamTraps.ITrap;
import me.camm.productions.bedwars.Util.DataSets.TimeSet;
import me.camm.productions.bedwars.Util.Locations.BlockRegisterType;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import me.camm.productions.bedwars.Util.PacketSound;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ExecutableBoundaryLoader implements Runnable
{
    private final Object lock;
    private final ArrayList<BattleTeam> primedTraps;
    private final ArrayList<BattleTeam> healAuras;
    private final ArrayList<TimeSet> coolingTeams;
    private final Thread thread;
    private volatile boolean running;
    private final Arena arena;

    private final Collection<BattlePlayer> players;

    private final static PotionEffect HEALING = new PotionEffect(PotionEffectType.REGENERATION,200,0,false);

    public ExecutableBoundaryLoader(Arena arena){
        this.arena = arena;
        this.lock = new Object();
        thread = new Thread(this);

        this.running = true;
      //  waiting = false;
        players = arena.getPlayers().values();

        primedTraps = new ArrayList<>();
        coolingTeams = new ArrayList<>();
        healAuras = new ArrayList<>();
    }

    public void stop(){
        running = false;
        resume();
    }

    public void resume(){
     //   waiting = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void run()
    {

        try
        {
            while (running)
            {


                synchronized (lock) {

                    if (primedTraps.size() == 0 && coolingTeams.size() == 0 && healAuras.size() == 0) {

                      //  waiting = true;
                           lock.wait();
                    }
                }


                    Thread.sleep(1000);
                    nextSecond();

                    players.forEach(player -> {
                        Location loc = player.getRawPlayer().getLocation();
                        Block block = loc.getBlock();

                        TRAPS:
                        {
                            if (primedTraps.isEmpty())
                                break TRAPS;

                            if (!player.getRawPlayer().isOnline())
                                break TRAPS;

                            if (!player.getIsAlive() || player.getIsEliminated())
                                break TRAPS;

                            if (!block.hasMetadata(BlockRegisterType.TRAP.getData()))
                                break TRAPS;




                            BattleTeam current = null;


                            Iterator<BattleTeam> traps = primedTraps.iterator();
                            while (traps.hasNext()) {
                                BattleTeam team = traps.next();

                                if (!team.doesBedExist()) {
                                    primedTraps.remove(team);
                                    continue;
                                }

                                if (block.hasMetadata(team.getTeamColor().getName()))
                                {
                                    current = team;
                                    break;
                                }

                            }


                                if (current == null)
                                    break TRAPS;

                                if (player.getTeam().equals(current))
                                    break TRAPS;

                                if (System.currentTimeMillis() - player.getLastMilk() <= 30000 )
                                   break TRAPS;



                               ITrap activated = current.activateNextTrap();
                               if (activated != null)
                               {
                                   current.sendTeamMessage(ChatColor.RED+"[TRAP] Your "+activated.name()+" was activated by "+player.getTeam().getTeamColor().getName()+" team!");
                                   current.sendTeamTitle(activated.getTrapTitle().getMessage(),"",5,40,5);
                                   current.sendTeamSoundPacket(PacketSound.ENDERMAN);

                                   primedTraps.remove(current);
                                   coolingTeams.add(new TimeSet(current, System.currentTimeMillis()));

                               }




                        }

                        HEALS:{
                            if (healAuras.isEmpty())
                                break HEALS;

                            BattleTeam current = null;

                            if (!block.hasMetadata(BlockRegisterType.AURA.getData()))
                                break HEALS;


                            for (BattleTeam team: healAuras)
                                {
                                    Coordinate coordinate = team.getAura().getRandomCoordinateWithin();
                                    team.sendTeamPacket(new PacketPlayOutWorldParticles(EnumParticle.VILLAGER_HAPPY,true,(float)coordinate.getX(),(float)coordinate.getY(),(float)coordinate.getZ(),0,0,0,(float)0.1,5));

                                    if (!block.hasMetadata(team.getTeamColor().getName()))
                                        continue;

                                    current = team;
                                    break;

                                }

                                if (current == null)
                                    break HEALS;
                                //use bukkit runnable here
                                if (!player.getTeam().equals(current)) {
                                    break HEALS;

                                }
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            //use bukkit runnable here
                                            player.getRawPlayer().addPotionEffect(HEALING);
                                            cancel();
                                        }
                                    }.runTask(arena.getPlugin());


                                //PotionEffectType type, int duration, int amplifier, boolean ambient


                        }
                    });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void load(BattleTeam team, boolean trap){
        resume();


        if (trap) {
            synchronized (primedTraps) {
            if (!primedTraps.contains(team) && team.doesBedExist()) {

                for (TimeSet set: coolingTeams)
                {
                    if (set.getTeam().equals(team))
                        return;
                }

                primedTraps.add(team);
            }
           }
        }
        else {
            synchronized (healAuras) {
                healAuras.add(team);
            }
        }

    }

    private synchronized void nextSecond()
    {

        if (coolingTeams.isEmpty())
            return;

        long millis = System.currentTimeMillis();

        TimeSet next = coolingTeams.get(0);//1

        //traps have 20 sec cooldown
        while ( (millis - next.getMillis() > 20000) && (!coolingTeams.isEmpty() ))
        {
            BattleTeam team = next.getTeam();
            if (team.nextTrap() != null && team.doesBedExist())
            {
                synchronized (primedTraps) {
                    coolingTeams.remove(0);
                    primedTraps.add(team);
                }
            }
            else
                coolingTeams.remove(0);

            if (coolingTeams.isEmpty())
                break;

            next = coolingTeams.get(0);
        }
    }

    public void start(){
        thread.start();
    }
}
