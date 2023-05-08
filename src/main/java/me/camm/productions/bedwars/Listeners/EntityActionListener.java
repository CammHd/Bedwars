package me.camm.productions.bedwars.Listeners;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.GameRunner;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.DeathMessages.DeathCause;
import me.camm.productions.bedwars.Game.Teams.BattleTeam;
import me.camm.productions.bedwars.Game.Teams.TeamColor;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy.IGameOwnable;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy.IGameTeamable;
import me.camm.productions.bedwars.Game.Entities.ActiveEntities.Hierarchy.ILifeTimed;
import me.camm.productions.bedwars.Game.Entities.ShopKeeper;
import me.camm.productions.bedwars.Util.PacketSound;
import me.camm.productions.bedwars.Util.Tuple2;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static me.camm.productions.bedwars.Util.Helpers.DamageHelper.formatDeathMessage;
import static me.camm.productions.bedwars.Util.Helpers.DamageHelper.formatDeathVoidNonDirect;

/**
 * @author CAMM
 * This class listens and handles physical events by both entities and players
 */
public class EntityActionListener implements Listener
{
    private final Arena arena;
    private final GameRunner runner;
    private static final ConcurrentHashMap<UUID, Tuple2<EntityDamageByEntityEvent,Long>> damageHistory;
    private static final ConcurrentHashMap<UUID, IGameTeamable> activeEntities;
    private final Map<UUID,BattlePlayer> arenaPlayers;
    private final PacketHandler handler;

    //hashmaps for the history of damage by entities, and for the current active entities
    static {
       damageHistory = new ConcurrentHashMap<>();
        activeEntities = new ConcurrentHashMap<>();
    }

    /*
    - Associate indirect damage with players or entities on teams. (Use a hashmap and damageset class in util)
    - also account for persistent items, and degradeable items. (This should be taken care of in the battleplayer class) [DONE! just need testing]

    We will most likely also need to merge this class with a player move listener (for the void), and associate it with traps
     and the heal pool as well.

     */

    public EntityActionListener(Arena arena, GameRunner runner)  //construct
    {
        this.arena = arena;
        this.runner = runner;

        arenaPlayers = arena.getPlayers();
        handler = runner.getPacketHandler();
    }


    public static void handleDeath(BattlePlayer victim, PacketHandler handler, Map<UUID,BattlePlayer> players, GameRunner runner, EntityDamageEvent.DamageCause cause)
    {

        //if the runner is not running, tp to spec spawn
        if (!runner.isRunning()) {
            victim.teleport(runner.getArena().getSpecSpawn());
            return;
        }

        Player hurt = victim.getRawPlayer();
        boolean isFinal = !victim.getTeam().getBedExists();

        if (damageHistory.containsKey(hurt.getUniqueId()))
        {
            Tuple2<EntityDamageByEntityEvent,Long> set = damageHistory.get(hurt.getUniqueId());
            long millis = set.getSecondElem();

            //give the previous damager 10 seconds to kill the player. If the kill occurred more than 10 seconds later,
            // then nobody gets the kill for the player.
            if (System.currentTimeMillis() - millis > 10000)
            {
                victim.handlePlayerIntoSpectator(handler, isFinal);
                formatDeathMessage(null,victim,null,cause, isFinal);
                runner.attemptEndGame();
                return;
            }

            //so it was direct player damage

            EntityDamageByEntityEvent event = set.getFirstElem();
            Entity damager = event.getDamager();

            if (players.containsKey(damager.getUniqueId()))
            {
                BattlePlayer killer = players.get(damager.getUniqueId());

                if (!victim.equals(killer))
                 handleKillerStats(killer,isFinal);

                formatDeathMessage(killer,victim,null,cause, isFinal);

                victim.handlePlayerIntoSpectator(handler,isFinal, killer.getRawPlayer());
                runner.attemptEndGame();
                return;
            }

            if (damager instanceof Projectile)
            {
                ProjectileSource source = ((Projectile)damager).getShooter();
                if (source instanceof Player) {
                    BattlePlayer shooter = players.getOrDefault(((Player)source).getUniqueId(),null);

                    if (!victim.equals(shooter))
                     handleKillerStats(shooter,isFinal);


                    //  //(BattlePlayer killer, BattlePlayer victim, IGameTeamable involved, EntityDamageEvent.DamageCause cause, Arena arena)
                    formatDeathMessage(shooter,victim,null,cause, isFinal);
                    victim.handlePlayerIntoSpectator(handler,isFinal, shooter.getRawPlayer());
                    runner.attemptEndGame();
                    return;
                }
            }


            if (damager instanceof TNTPrimed)
            {
                String name = damager.getCustomName();
                if (name != null) {

                    BattlePlayer killer = null;
                    for (BattlePlayer player: players.values())
                    {
                        if (player.getRawPlayer().getUniqueId().toString().equalsIgnoreCase(name)){
                           killer = player;
                           break;
                        }
                    }

                    if (killer == null) {
                        //(BattlePlayer killer, BattlePlayer victim, IGameTeamable involved, EntityDamageEvent.DamageCause cause, Arena arena)
                        formatDeathMessage(null,victim,null,cause, isFinal);
                        victim.handlePlayerIntoSpectator(handler, isFinal);
                        runner.attemptEndGame();
                        return;
                    }

                    if (!victim.equals(killer))
                        handleKillerStats(killer, isFinal);


                    formatDeathMessage(killer,victim,null,cause, isFinal);


                    victim.handlePlayerIntoSpectator(handler,isFinal, killer.getRawPlayer());
                    runner.attemptEndGame();
                    return;
                }
            }


            if (activeEntities.containsKey(damager.getUniqueId())) {
                IGameTeamable teamable = activeEntities.get(damager.getUniqueId());
                if (teamable instanceof IGameOwnable) {
                    BattlePlayer owner = ((IGameOwnable)teamable).getOwner();
                    if (owner == null)
                    {

                        formatDeathMessage(null,victim,teamable, cause, isFinal);
                        victim.handlePlayerIntoSpectator(handler, isFinal);
                        runner.attemptEndGame();
                        return;
                    }

                    if (!victim.equals(owner))
                       handleKillerStats(owner,isFinal);

                    //(BattlePlayer killer, BattlePlayer victim, IGameTeamable involved, EntityDamageEvent.DamageCause cause, Arena arena)
                    formatDeathMessage(owner,victim,teamable, cause, isFinal);
                    victim.handlePlayerIntoSpectator(handler,isFinal, owner.getRawPlayer());
                    runner.attemptEndGame();
                    return;

                }
                return;
            }
        }
             formatDeathMessage(null,victim,null, cause, isFinal);
            victim.handlePlayerIntoSpectator(handler, isFinal);
        runner.attemptEndGame();
    }

    private static void handleKillerStats(@NotNull BattlePlayer killer, boolean isFinal) {
        killer.playSound(PacketSound.DING);
        if (isFinal)
            killer.setFinals(killer.getFinals()+1);
        else
            killer.setKills(killer.getKills()+1);
    }

    private void updateHistoryDamage(@NotNull BattlePlayer hurt, EntityDamageByEntityEvent event){
        if (damageHistory.containsKey(hurt.getUUID()))
            damageHistory.replace(hurt.getUUID(),new Tuple2<>(event,System.currentTimeMillis()));
        else
            damageHistory.put(hurt.getUUID(),new Tuple2<>(event, System.currentTimeMillis()));

    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        if (isItem(damaged))
        {
            event.setCancelled(true);
            return;
        }

       if (damaged instanceof Player && damager instanceof Player)
           handlePVPDamage(event);
       else
           handleIndirectEntityDamage(event);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){

        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();

        switch (type) {
            case IRON_GOLEM:
            case SILVERFISH:
            case ENDER_DRAGON:
                event.getDrops().clear();
                event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onAchievementGet(PlayerAchievementAwardedEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        if (!arenaPlayers.containsKey(player.getUniqueId()))
            return;

        if (runner.isRunning()) {
            event.setCancelled(true);

            BattlePlayer sending = arenaPlayers.get(player.getUniqueId());
            BattleTeam team = sending.getTeam();
            TeamColor color = team.getTeamColor();

            team.sendTeamMessage(ChatColor.RESET + "<" + color.getChatColor() + player.getName() + ChatColor.RESET + ">" + event.getMessage());
        }
    }



    @EventHandler
    public void onPlayerIndirectDamage(EntityDamageEvent event)
    {
        Entity damaged = event.getEntity();

        if (isItem(damaged))
        {
            event.setCancelled(true);
            return;
        }

        if (damaged instanceof Player)
        {
            EntityDamageEvent.DamageCause cause = event.getCause();
            Player player = (Player)damaged;

            if (!arena.getPlayers().containsKey(player.getUniqueId()))
                return;

            BattlePlayer hurt = arena.getPlayers().get(player.getUniqueId());


            switch (cause)
            {
                case FALL:
                case FIRE:
                case MAGIC:
                case LAVA:
                case VOID:
                case DROWNING:
                case FIRE_TICK:
                case CONTACT:
                case SUFFOCATION:
                case FALLING_BLOCK:
                case LIGHTNING:
                case CUSTOM:
                case MELTING:
                case STARVATION:
                case POISON:
                case THORNS:
                case WITHER:
                case SUICIDE:

                    if (!runner.isRunning()) {
                        hurt.teleport(runner.getArena().getSpecSpawn());
                        return;
                    }

                    //If the player is a spectator
                    if (!hurt.isAlive() ||  hurt.isEliminated())
                    {
                        event.setCancelled(true);
                        return;
                    }

                    // if the player will not die from this event.
                    if (playerSurvives(player, event))
                        return;

                    event.setCancelled(true);
                    handleDeath(hurt,handler,arenaPlayers,runner,cause);
                    break;
            }
        }
    }





    //If 1 or 0 entities are players.
    private void handleIndirectEntityDamage(EntityDamageByEntityEvent event)
    {

        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        //If 1 is a projectile, other is a player.
        if (damager instanceof Projectile && damaged instanceof Player) {
            handlePlayerIndirectProjectileDamage(event);
            return;
        }

        //If both entities are not players.
        if (!(damager instanceof Player) && !(damaged instanceof Player)) {
            handleNonPlayerEntityDamage(event);
            return;
        }

        if (activeEntities.containsKey(damaged.getUniqueId()) && damager instanceof Player) {

            BattlePlayer hitter = arenaPlayers.getOrDefault(damager.getUniqueId(),null);
            if (hitter == null) {
                event.setCancelled(true);
                return;
            }

            IGameTeamable teamable = activeEntities.get(damaged.getUniqueId());
            if (teamable.getTeam().equals(hitter.getTeam())) {
                event.setCancelled(true);
                return;
            }
        }

        if (!arenaPlayers.containsKey(damaged.getUniqueId()))
            return;


        if (damager instanceof TNTPrimed) {
            String customName = damager.getCustomName();
            Player hit = (Player)damaged;

            if (customName == null)
            {
                event.setCancelled(true);
                return;
            }
                BattlePlayer playerHit = arenaPlayers.get(hit.getUniqueId());

            if (!playerHit.isAlive() || playerHit.isEliminated())
            {
                event.setCancelled(true);
                return;
            }



                updateHistoryDamage(playerHit,event);

                BattlePlayer playerOwner = null;
                for (BattlePlayer player: arenaPlayers.values()) {
                    if (player.getRawPlayer().getUniqueId().toString().equalsIgnoreCase(customName)) {
                        playerOwner = player;
                        break;
                    }
                }


                if (playerOwner == null || !playerOwner.isAlive()|| playerOwner.isEliminated())
                {
                    event.setCancelled(true);
                    return;
                }

                 playerHit.removeInvisibilityEffect();
                if (playerSurvives(playerHit.getRawPlayer(),event))
                    return;

                handleDeath(playerHit,handler,arenaPlayers,runner,event.getCause());
                return;
        }// if it is tnt

        if (activeEntities.containsKey(damager.getUniqueId()))
        {
            IGameTeamable teamable = activeEntities.get(damager.getUniqueId());
            BattlePlayer hit = arenaPlayers.get(damaged.getUniqueId());

            if (!hit.isAlive() ||  hit.isEliminated())
            {
                event.setCancelled(true);
                return;
            }


            if (teamable.getTeam().equals(hit.getTeam())) {
                event.setCancelled(true);
                return;
            }

            hit.removeInvisibilityEffect();
            updateHistoryDamage(hit,event);

            if (!playerSurvives(hit.getRawPlayer(),event)) {
                handleDeath(hit,handler,arenaPlayers,runner,event.getCause());
            }
        }
        else
            event.setCancelled(true);



        /*
        Below this line, 1 is a player, 1 is an entity, and the damaging entity is
        not a projectile (Not a fireball, not an arrow),
        so it's either a golem, tnt, dragon, or silverfish.
         */
    }


    //If one entity is a player and the other is a projectile (Either fireball or arrow)
    private void handlePlayerIndirectProjectileDamage(EntityDamageByEntityEvent event)
    {
        Projectile damager = (Projectile) event.getDamager();
        ProjectileSource source = damager.getShooter();
        Entity hurt = event.getEntity();

        if (source == null) {
            event.setCancelled(true);
            return;
        }

      Map<UUID, BattlePlayer> registered = arena.getPlayers();
   if (!registered.containsKey(hurt.getUniqueId()))
       return;

   BattlePlayer hurtPlayer = registered.get(hurt.getUniqueId());
        if (!hurtPlayer.isAlive() ||  hurtPlayer.isEliminated())
        {
            event.setCancelled(true);
            return;
        }

        if (!(source instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        if (!registered.containsKey(((Player)source).getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        BattlePlayer sourcePlayer = registered.get(((Player)source).getUniqueId());
        if (!sourcePlayer.isAlive()||sourcePlayer.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        if (damager instanceof Arrow)
        {
            Arrow arrow = (Arrow)damager;


            if (hurtPlayer.equals(sourcePlayer)) {
                event.setCancelled(true);
                return;
            }

            double hp = hurtPlayer.getRawPlayer().getHealth()-event.getFinalDamage();
            hp *= 1000;
            hp = Math.round(hp);
            hp /= 1000.0;

            updateHistoryDamage(hurtPlayer,event);
      sourcePlayer.sendMessage(ChatColor.YELLOW+hurtPlayer.getRawPlayer().getName()+" is on "+Math.max(hp,0.0)+" hp!");
            hurtPlayer.removeInvisibilityEffect();

            if (!playerSurvives((Player)hurt,event))
            {
                event.setCancelled(true);
                arrow.setVelocity(arrow.getVelocity().multiply(-1));
                arrow.remove();

                handleDeath(hurtPlayer, handler, arenaPlayers,runner,event.getCause());

            }
            return;
        }



        if (damager instanceof Fireball)
        {
            updateHistoryDamage(hurtPlayer,event);
            hurtPlayer.removeInvisibilityEffect();


            if (!playerSurvives(hurtPlayer.getRawPlayer(), event)) {
                event.setCancelled(true);
               handleDeath(hurtPlayer,handler, arenaPlayers,runner,event.getCause());
            }
        }
    }

    //If both entities are not players.
    private void handleNonPlayerEntityDamage(EntityDamageByEntityEvent event)
    {
        Entity hurt = event.getEntity();
        Entity damager = event.getDamager();

        if (damager instanceof Projectile)
        {
            Projectile hurter = (Projectile)damager;

            if (hurt instanceof Projectile)
                return;

            //fireballs are not discriminant
            if (hurter instanceof Fireball){
                return;
            }


            ProjectileSource source = hurter.getShooter();
            if (source instanceof Player && arenaPlayers.containsKey(((Player) source).getUniqueId())){
                BattlePlayer shot = arenaPlayers.get(((Player)source).getUniqueId());

                if (!activeEntities.containsKey(hurt.getUniqueId()))
                    return;

                IGameTeamable teamable = activeEntities.get(hurt.getUniqueId());
                if (shot.getTeam().equals(teamable.getTeam()))
                    event.setCancelled(true);


            }
        }

        if (activeEntities.containsKey(hurt.getUniqueId()) && activeEntities.containsKey(damager.getUniqueId())) {

            IGameTeamable hit = activeEntities.get(hurt.getUniqueId());
            IGameTeamable hitter = activeEntities.get(damager.getUniqueId());

            if (hit.getTeam().equals(hitter.getTeam()))
                event.setCancelled(true);


            if (hit instanceof ILifeTimed) {
                ((ILifeTimed)hit).handleEntityTarget(null);
            }

            if (hitter instanceof ILifeTimed) {
                ((ILifeTimed)hitter).handleEntityTarget(null);
            }
        }

        /*
        //If both entities are not players.
        Note that 1 could still be a projectile.
        So it could be a golem and a fireball for example,
         or a golem and another team's silverfish/ golem.
         */

    }





    /*
    This method handles direct pvp damage. (melee damage)
     */
    private void handlePVPDamage(EntityDamageByEntityEvent event)
    {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        Player hit = (Player)damaged;
        Player hitter = (Player)damager;


        //If both players are not registered
        if (!arenaPlayers.containsKey(hit.getUniqueId()) || !arenaPlayers.containsKey(hitter.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        BattlePlayer hurt = arenaPlayers.get(hit.getUniqueId());
        BattlePlayer hurter = arenaPlayers.get(hitter.getUniqueId());
        if (hurt.getTeam().equals(hurter.getTeam())) {
            event.setCancelled(true);
            return;
        }

        if (hurter.isEliminated() || !hurter.isAlive()) {
            event.setCancelled(true);
            return;
        }

        hurt.removeInvisibilityEffect();
        updateHistoryDamage(hurt,event);

        //If both players are from the same team.
        if (playerSurvives(hurt.getRawPlayer(),event))
            return;

        event.setCancelled(true);
        handleDeath(hurt,handler, arenaPlayers,runner,event.getCause());
        //debug for scoreboard
    }


    public synchronized void addEntity(IGameTeamable entity)
    {
        try {
            //So fireballs and tnt shouldn't be registered here. Check the gameTNT and Fireball classes.

            activeEntities.put(entity.getUUID(), entity);
        }
        catch (NullPointerException ignored)
        {

        }
    }



    public synchronized void removeEntity(UUID id)
    {
        activeEntities.remove(id);
    }

    public synchronized boolean contains(UUID id)
    {
        return activeEntities.containsKey(id);
    }

    private boolean isItem(Entity entity)
    {
        return entity instanceof Item;
    }

    public synchronized IGameTeamable getEntity(UUID id){
        return activeEntities.getOrDefault(id, null);
    }

    //returns whether or not a player would survive (not die) from the damage of a damage event
    private boolean playerSurvives(Player player, EntityDamageEvent event) {
        return player.getHealth() - event.getFinalDamage() >= 1;
    }


    public static class LocationManager extends BukkitRunnable
    {
        private final ArrayList<ShopKeeper> keepers;
        private final Arena arena;
        private volatile boolean isRunning;
        private final PacketHandler handler;
        private final GameRunner runner;

        public LocationManager(Arena arena, ArrayList<ShopKeeper> keepers, PacketHandler handler, GameRunner runner)
        {
            this.arena = arena;
            this.keepers = keepers;
            this.isRunning = true;
            this.handler = handler;
            this.runner = runner;
        }

        public synchronized void setRunning(boolean isRunning)
        {
            this.isRunning = isRunning;
        }

        private void updateNPCs(BattlePlayer player, Player raw){
            keepers.forEach(keeper -> {
                if ((raw.getLocation().distanceSquared(keeper.getLocation())>2304)&&(!player.containsNPC(keeper.getId())))
                {
                    keeper.unloadNPC(raw);
                    player.addResender(keeper);
                }
                //2304 is 48 ^2. We check if the distance is comparable to 48, but without the extra calculations of sqrt
                else if (player.containsNPC(keeper.getId())&&raw.getLocation().distanceSquared(keeper.getLocation())<2304)
                {
                    player.removeResender(keeper.getId());
                    keeper.sendNPC(raw);
                    keeper.setRotation(raw);
                }
            });

        }


        private void defaults(BattlePlayer player, Player raw){
            updateNPCs(player, raw);
            raw.setFoodLevel(20);
        }

        @Override
        public void run()
        {
            Map<UUID, BattlePlayer> players = arena.getPlayers();

                    if (!isRunning || !runner.isRunning())
                    {
                        cancel();
                        return;
                    }

                    for (BattlePlayer player: players.values()) {
                        Player rawPlayer = player.getRawPlayer();
                        boolean finalDeath = !player.getTeam().getBedExists();

                        if (rawPlayer.getLocation().getY() <= arena.getVoidLevel()) {
                           defaults(player,rawPlayer);
                           continue;
                        }

                        if (player.isEliminated() || !player.isAlive()) {
                            player.teleport(arena.getSpecSpawn());
                            defaults(player,rawPlayer);
                            continue;
                        }

                        if (!damageHistory.containsKey(player.getUUID())) {
                            formatDeathMessage(null,player,null, EntityDamageEvent.DamageCause.VOID, finalDeath);
                            player.handlePlayerIntoSpectator(handler, !player.getTeam().getBedExists());
                            runner.attemptEndGame();
                            continue;
                        }

                        Tuple2<EntityDamageByEntityEvent,Long> damageEntry = damageHistory.get(player.getUUID());
                        long eventSystemTime = damageEntry.getSecondElem();

                        EntityDamageByEntityEvent event = damageEntry.getFirstElem();
                        UUID attackerUUID = event.getDamager().getUniqueId();
                        Entity damager = damageEntry.getFirstElem().getDamager();

                        //(BattlePlayer killer, BattlePlayer victim, IGameTeamable involved, EntityDamageEvent.DamageCause cause, Arena arena)
                        if (System.currentTimeMillis() - eventSystemTime > 10000) {
                            formatDeathMessage(null,player,null, EntityDamageEvent.DamageCause.VOID, finalDeath);
                            player.handlePlayerIntoSpectator(handler, finalDeath);
                            runner.attemptEndGame();
                            continue;
                        }

                        if (players.containsKey(attackerUUID))
                        {
                            BattlePlayer killer = players.get(attackerUUID);
                            if (!killer.getTeam().equals(player.getTeam()))
                                handleKillerStats(killer,finalDeath);

                            formatDeathMessage(killer,player,null, EntityDamageEvent.DamageCause.VOID, finalDeath);
                            player.handlePlayerIntoSpectator(handler, finalDeath, killer.getRawPlayer());
                            runner.attemptEndGame();
                            continue;
                        }


                        if (activeEntities.containsKey(attackerUUID)) {
                            IGameTeamable teamable = activeEntities.get(attackerUUID);

                            if (teamable instanceof IGameOwnable) {
                                BattlePlayer owner = ((IGameOwnable) teamable).getOwner();

                                if (!owner.getTeam().equals(player.getTeam()))
                                    handleKillerStats(owner,finalDeath);

                                formatDeathMessage(owner,player,teamable, EntityDamageEvent.DamageCause.VOID, finalDeath);
                                player.handlePlayerIntoSpectator(handler, finalDeath, owner.getRawPlayer());
                                runner.attemptEndGame();
                                continue;
                            }
                        }


                        if (damager instanceof Projectile) {
                            ProjectileSource source = ((Projectile)damager).getShooter();

                            if (!(source instanceof Player)) {
                                player.handlePlayerIntoSpectator(handler, finalDeath, null);
                                runner.attemptEndGame();
                                continue;
                            }

                            Player shooter = (Player) source;
                            BattlePlayer currentPlayer = players.getOrDefault(shooter.getUniqueId(),null);

                            if (currentPlayer != null) {

                                if (!currentPlayer.getTeam().equals(player.getTeam())) {
                                    handleKillerStats(currentPlayer, finalDeath);
                                }

                                formatDeathVoidNonDirect(currentPlayer, player, damager instanceof Arrow ? DeathCause.PROJECTILE_VOID: DeathCause.FIREBALL_VOID, finalDeath);
                                player.handlePlayerIntoSpectator(handler, finalDeath, currentPlayer.getRawPlayer());
                                runner.attemptEndGame();
                                continue;
                            }
                        }


                        if (damager instanceof Explosive) {

                            String name = damager.getCustomName();
                            BattlePlayer owner = null;
                            if (name != null)
                            {
                                for (BattlePlayer current: players.values()) {
                                    if (current.getRawPlayer().getUniqueId().toString().equalsIgnoreCase(name)) {
                                        owner = current;
                                        break;
                                    }
                                }

                                if (owner != null && !owner.getTeam().equals(player.getTeam()))
                                    handleKillerStats(owner, finalDeath);
                                formatDeathVoidNonDirect(owner, player, DeathCause.TNT_VOID,finalDeath);
                            }

                            player.handlePlayerIntoSpectator(handler, finalDeath, owner == null ? null: owner.getRawPlayer());
                            runner.attemptEndGame();
                        }
                    }
        }
    }


}

