package me.camm.productions.bedwars.Util.Locations.Boundaries;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Teams.Traps.ITrap;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Locations.Coordinate;
import me.camm.productions.bedwars.Util.PacketSound;
import me.camm.productions.bedwars.Util.Tuple2;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;


/*
   @author bipi
   @author CAMM
 */
public class ExecutableBoundaryLoader implements Runnable
{
    private final Object lock;
    private final ArrayList<BattleTeam> primedTraps;
    private final ArrayList<BattleTeam> healAuras;
    private final ArrayList<Tuple2<BattleTeam, Long>> coolingTeams;
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
                           lock.wait();
                    }
                }

                    Thread.sleep(1000);
                    nextSecond();

                    players.forEach(player -> {
                        checkTraps(player);
                        checkHeals(player);
                    });
            }
        }
        catch (Exception e)
        {
            ChatSender sender = ChatSender.getInstance();
            sender.sendConsoleMessage("Boundary loader experienced an Interrupted exception", Level.WARNING);
        }

    }

    public void load(BattleTeam team, boolean trap){
        resume();


        if (trap) {
            synchronized (primedTraps) {
            if (!primedTraps.contains(team) && team.getBedExists()) {

                for (Tuple2<BattleTeam, Long> set: coolingTeams)
                {
                    if (set.getFirstElem().equals(team))
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



        /*

        Traps : loop over all players
           for all players which are on a different team than the trap's team
              if the player is in the trap area,
               then activate the trap


        Heal pools are opposite - only activate for players on same team


         */


    public void checkTraps(BattlePlayer player){

        if (primedTraps.isEmpty())
             return;

        if (!player.getRawPlayer().isOnline())
            return;

        if (!player.isAlive() || player.isEliminated())
            return;


        BattleTeam current = null;


        for (BattleTeam team : primedTraps) {
            if (!team.getBedExists()) {
                primedTraps.remove(team);
                continue;
            }

            if (team.equals(player.getTeam()))
                continue;

            Location loc = player.getRawPlayer().getLocation();
            if (team.getTrapArea().containsCoordinate(loc.getX(), loc.getY(), loc.getZ())) {
                current = team;
                break;
            }
        }


        if (current == null)
            return;

        final int MAGIC_MILK = 30000;
        if (System.currentTimeMillis() - player.getLastMilk() <= MAGIC_MILK)
            return;

        ITrap activated = current.activateNextTrap();
        if (activated != null)
        {
            current.sendTeamMessage(ChatColor.RED+"[TRAP] Your "+activated.name()+" was activated by "+player.getTeam().getTeamColor().getName()+" team!");
            current.sendTeamTitle(activated.getTrapTitle().getMessage(),"",5,40,5);
            current.sendTeamSoundPacket(PacketSound.ENDERMAN);

            primedTraps.remove(current);
            coolingTeams.add(new Tuple2<>(current, System.currentTimeMillis()));

        }




    }


    public void checkHeals(BattlePlayer player){

        /*
        If the player is on the same team, and they are in the boundary, then they get regeneration
         */



        BattleTeam team = player.getTeam();

        if (!healAuras.contains(team) )
            return;


        Coordinate c = team.getAura().getRandomCoordinateWithin();
        //EnumParticle var1, boolean var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, int var10, int... var11) {

        PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(EnumParticle.VILLAGER_HAPPY,true,
                (float)c.getX(), (float)c.getY(), (float)c.getZ(),0,0,0,0,1);

        team.sendTeamPacket(particles);
        if(player.getTeam().getAura().containsEntity(player.getRawPlayer()))
        {

            Player p = player.getRawPlayer();


            //must be in a bukkitrunnable or it will throw exception
            new BukkitRunnable()
            {
                //HEALING
                @Override
                public void run()
                {   ///
                    p.addPotionEffect(HEALING,true);
                    cancel();
                }
            }.runTask(arena.getPlugin());
        }


    }








    private synchronized void nextSecond()
    {

        if (coolingTeams.isEmpty())
            return;

        long millis = System.currentTimeMillis();

        Tuple2<BattleTeam, Long> next = coolingTeams.get(0);//1

        //traps have 20 sec cooldown
        final int COOLDOWN = 20000;
        while ( (millis - next.getSecondElem() > COOLDOWN) && (!coolingTeams.isEmpty() ))
        {
            BattleTeam team = next.getFirstElem();
            if (team.nextTrap() != null && team.getBedExists())
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
