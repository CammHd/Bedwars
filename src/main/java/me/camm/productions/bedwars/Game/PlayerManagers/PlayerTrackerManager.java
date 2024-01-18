package me.camm.productions.bedwars.Game.PlayerManagers;

import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerTrackerManager implements Runnable{


    private final Object lock;
    private final Thread thread;

    private volatile boolean running;


    private final Set<BattlePlayer> trackingEntries;


    public PlayerTrackerManager() {

        this.lock = new Object();
        thread = new Thread(this);
        this.running = false;
        this.trackingEntries = new HashSet<>();
    }


    public void start(){
        if (thread.isAlive())
            return;

        setRunning(true);
        thread.start();
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {

        try {
            while (running) {



                if (trackingEntries.isEmpty()) {
                    synchronized (lock) {
                        lock.wait();
                    }
                }


                    Iterator<BattlePlayer> iter = trackingEntries.iterator();


                    while (iter.hasNext()) {
                        BattlePlayer next = iter.next();

                        BattleTeam tracking = next.getTracking();
                        if (tracking == null) {
                            iter.remove();
                            continue;

                        }

                        BattlePlayer target = getTarget(tracking, next);

                        if (target == null) {
                            next.sendActionbarTitle( "{\"text\":\""+ChatColor.GREEN+"Tracking: None\"}");
                        }
                        else  {

                            Player rawTarget = target.getRawPlayer();
                            Player raw = next.getRawPlayer();
                            double distance = raw.getLocation().distance(rawTarget.getLocation());
                            distance *= 100;
                            distance = Math.round(distance);
                            distance /= 100;

                            raw.setCompassTarget(rawTarget.getLocation());
                            next.sendActionbarTitle("{\"text\":\""+ChatColor.GREEN+
                                    "Tracking: "+target.getRawPlayer().getName()+
                                    " Distance: "+distance+"\"}");
                        }
                    }




                Thread.sleep(1000);
            }

        }
        catch (InterruptedException ignored) {
        }


    }





    public BattlePlayer getTarget(BattleTeam tracking, BattlePlayer tracker){

        Collection<BattlePlayer> players = tracking.getPlayers().values();

        if (players.size() == 0)
            return null;

        BattlePlayer closest = players.iterator().next();
        double dist = Double.MAX_VALUE;
        Location origin = tracker.getRawPlayer().getLocation();
        for (BattlePlayer teamPlayer: players) {


          double current = teamPlayer.getRawPlayer().getLocation().distanceSquared(origin);

          if (current <= dist) {
              dist = current;
              closest = teamPlayer;
          }
        }

        return closest;
    }




    public void resume(){
        synchronized (lock) {
            lock.notify();
        }
    }

    public void addEntry(BattlePlayer player){
        synchronized (trackingEntries) {
            trackingEntries.add(player);

        }

        resume();
    }

}
