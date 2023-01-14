package me.camm.productions.bedwars.Entities.ActiveEntities;



import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.DeathMessages.Cause;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Entities.ActiveEntities.Hierarchy.IGameAutonomous;
import me.camm.productions.bedwars.Listeners.EntityActionListener;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;



/**
 * @author CAMM
 * Models a ender dragon in the game
 */
public class GameDragon extends EntityEnderDragon implements IGameAutonomous
{
    private final Arena arena;
    private final BattleTeam team;
    private final Location centre;
    private final EntityActionListener listener;
    private static int number;

    private long targetTime;
    private int nextTime;
    private BattlePlayer currentTarget;

    private final Random rand;
    private final boolean valid;

    static {
        number = 0;
    }

    public GameDragon(World world, Location spawn, Arena arena, BattleTeam team, Location centre, EntityActionListener listener)
    {
        super(world);
        this.dead = false;
        this.dimension = 0;
        this.centre = centre;
        this.setPosition(spawn.getX(),spawn.getY(),spawn.getZ());
        this.listener = listener;
        this.rand = new Random();
        valid = true;

        this.arena = arena;
        this.team = team;
        targetTime = System.currentTimeMillis();
        currentTarget = null;

        this.a = centre.getX();
        this.b = centre.getY();
        this.c = centre.getZ();

        this.setCustomName(team.getTeamColor().getChatColor()+team.getTeamColor().getName()+getType()+" ("+number+")");
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(75);
        this.setHealth(75);
        number ++;


        nextTime = rand.nextInt(16) + 10;
    }


    //Used by nms. Just here so it doesn't throw an exception.
    @SuppressWarnings("unused")
    public GameDragon(World world){
        super(world);
        valid = false;

        arena = null;
        team = null;
        centre = null;
        listener = null;
        rand = null;
    }

    public synchronized void setTarget(Entity entity)
    {
        this.target = entity;
    }

    @Override
    public void spawn()
    {
        if (!valid)
            return;

        GameDragon dragon = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                world.addEntity(dragon, CreatureSpawnEvent.SpawnReason.CUSTOM);
                cancel();

            }
        }.runTask(arena.getPlugin());

    }

    public void remove()
    {
        this.world.removeEntity(this);
    }

    @Override
    public void handleEntityTarget(org.bukkit.entity.Entity entity)
    {
        if (entity !=null)
       this.setTarget(((CraftEntity)entity).getHandle());
        else
            this.setTarget(null);
    }


    @Override
    public void m()  //Method to move, overridden
    {

        float f;
        float f1;

        this.bu = this.bv;
        float f2;


        //So if the dragon is dead
        if (this.getHealth() <= 0)
        {
            f = (this.random.nextFloat() - 0.5F) * 8.0F;
            f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
            f2 = (this.random.nextFloat() - 0.5F) * 8.0F;

            this.world.addParticle(EnumParticle.EXPLOSION_HUGE, this.locX + (double)f, this.locY + 2.0D + (double)f1, this.locZ + (double)f2, 0.0D, 0.0D, 0.0D, 3);

            world.removeEntity(this);
        }
        else //if it's alive
        {

            this.attemptToBattle();

            f = 0.2F / (MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ) * 10.0F + 1.0F);
            f *= (float)Math.pow(2.0D, this.motY);
            if (this.bx) {
                this.bv += f * 0.5F;
            } else {
                this.bv += f;
            }

            this.yaw = MathHelper.g(this.yaw);
            if (this.ce()) {
                this.bv = 0.5F;
            } else {
                if (this.bl < 0) {
                    for(int i = 0; i < this.bk.length; ++i) {
                        this.bk[i][0] = this.yaw;
                        this.bk[i][1] = this.locY;
                    }
                }

                if (++this.bl == this.bk.length) {
                    this.bl = 0;
                }

                this.bk[this.bl][0] = this.yaw;
                this.bk[this.bl][1] = this.locY;
                double deltaX;
                double deltaY;
                double deltaZ;

                float f3;
                float f6;
                float f7;




                deltaX = this.a - this.locX;
                deltaY = this.b - this.locY;
                deltaZ = this.c - this.locZ;
                double d8;
                double d9;
                double d4;
                if (this.target != null)
                {
                    this.a = this.target.locX;
                    this.c = this.target.locZ;

                    d8 = this.a - this.locX;
                    d9 = this.c - this.locZ;
                    double d7 = Math.sqrt(d8 * d8 + d9 * d9);
                    d4 = 0.4000000059604645D + d7 / 80.0D - 1.0D;
                    if (d4 > 10.0D) {
                        d4 = 10.0D;
                    }

                    this.b = this.target.getBoundingBox().b + d4;
                }
                else
                {
                    this.a += this.random.nextGaussian() * 2.0D;
                    this.c += this.random.nextGaussian() * 2.0D;
                }

                if (this.target==null && ((System.currentTimeMillis()-targetTime)/1000) >= nextTime)
                {
                    currentTarget = null;
                    this.attemptTargetRandomPlayer();
                }
                else
                {

                   if (currentTarget != null && !currentTarget.getIsAlive() && !currentTarget.getIsEliminated()) {
                        currentTarget = null;
                        target = null;
                   }
                }

                deltaY /= MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                f3 = 0.6F;
                deltaY = MathHelper.a(deltaY, (-f3), f3);
                this.motY += deltaY * 0.10000000149011612D;
                this.yaw = MathHelper.g(this.yaw);
                d8 = 180.0D - MathHelper.b(deltaX, deltaZ) * 180.0D / 3.1415927410125732D;
                d9 = MathHelper.g(d8 - this.yaw);
                if (d9 > 50.0D) {
                    d9 = 50.0D;
                }

                if (d9 < -50.0D) {
                    d9 = -50.0D;
                }


                Vec3D vec3d = (new Vec3D(this.a - this.locX, this.b - this.locY, this.c - this.locZ)).a();
                d4 = (-MathHelper.cos(this.yaw * 3.1415927F / 180.0F));
                Vec3D vec3d1 = (new Vec3D(MathHelper.sin(this.yaw * 3.1415927F / 180.0F), this.motY, d4)).a();
                float f4 = ((float)vec3d1.b(vec3d) + 0.5F) / 1.5F;
                if (f4 < 0.0F) {
                    f4 = 0.0F;
                }

                this.bb *= 0.8F;
                float f5 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ) + 1.0F;
                double d10 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ) + 1.0D;
                if (d10 > 40.0D) {
                    d10 = 40.0D;
                }

                this.bb = (float)((double)this.bb + d9 * (0.699999988079071D / d10 / (double)f5));
                this.yaw += this.bb * 0.1F;
                f6 = (float)(2.0D / (d10 + 1.0D));
                f7 = 0.06F;
                this.a(0.0F, -1.0F, f7 * (f4 * f6 + (1.0F - f6)));
                if (this.bx) {
                    this.move(this.motX * 0.800000011920929D, this.motY * 0.800000011920929D, this.motZ * 0.800000011920929D);
                } else {
                    this.move(this.motX, this.motY, this.motZ);
                }


                Vec3D vec3d2 = getUnitOrZero(new Vec3D(this.motX, this.motY, this.motZ));


                float f8 = ((float)vec3d2.b(vec3d1) + 1.0F) / 2.0F;

                f8 = 0.8F + 0.15F * f8;

                this.motX *= f8;
                this.motZ *= f8;
                this.motY *= 0.9100000262260437D;



                ///////////////////////////////////////////////
                this.aI = this.yaw;

                //body parts of the dragon. If you're messing with size, probably need to mess with this too.
                this.bn.width = this.bn.length = 3.0F;
                this.bp.width = this.bp.length = 2.0F;
                this.bq.width = this.bq.length = 2.0F;
                this.br.width = this.br.length = 2.0F;
                this.bo.length = 3.0F;
                this.bo.width = 5.0F;
                this.bs.length = 2.0F;
                this.bs.width = 4.0F;
                this.bt.length = 3.0F;
                this.bt.width = 4.0F;




                f1 = (float)(this.b(5, 1.0F)[1] - this.b(10, 1.0F)[1]) * 10.0F / 180.0F * 3.1415927F;
                f2 = MathHelper.cos(f1);
                float f9 = -MathHelper.sin(f1);

                float f10 = this.yaw * 3.1415927F / 180.0F; //getting yaw in radians

                float f11 = MathHelper.sin(f10);
                float f12 = MathHelper.cos(f10);
                this.bo.t_();
                this.bo.setPositionRotation(this.locX + (double)(f11 * 0.5F), this.locY, this.locZ - (double)(f12 * 0.5F), 0.0F, 0.0F);
                this.bs.t_();
                this.bs.setPositionRotation(this.locX + (double)(f12 * 4.5F), this.locY + 2.0D, this.locZ + (double)(f11 * 4.5F), 0.0F, 0.0F);
                this.bt.t_();
                this.bt.setPositionRotation(this.locX - (double)(f12 * 4.5F), this.locY + 2.0D, this.locZ - (double)(f11 * 4.5F), 0.0F, 0.0F);

                if (!this.world.isClientSide && this.hurtTicks == 0) {
                    this.setHitEntityVelocity(this.world.getEntities(this, this.bs.getBoundingBox().grow(4.0D, 2.0D, 4.0D).c(0.0D, -2.0D, 0.0D)));
                    this.setHitEntityVelocity(this.world.getEntities(this, this.bt.getBoundingBox().grow(4.0D, 2.0D, 4.0D).c(0.0D, -2.0D, 0.0D)));
                    this.attemptKnockBackHitEntities(this.world.getEntities(this, this.bn.getBoundingBox().grow(1.0D, 1.0D, 1.0D)));
                }

                double[] adouble = this.b(5, 1.0F);
                double[] adouble1 = this.b(0, 1.0F);
                f3 = MathHelper.sin(this.yaw * 3.1415927F / 180.0F - this.bb * 0.01F);
                float f13 = MathHelper.cos(this.yaw * 3.1415927F / 180.0F - this.bb * 0.01F);
                this.bn.t_();
                this.bn.setPositionRotation(this.locX + (double)(f3 * 5.5F * f2), this.locY + (adouble1[1] - adouble[1]) + (double)(f9 * 5.5F), this.locZ - (double)(f13 * 5.5F * f2), 0.0F, 0.0F);

                for(int j = 0; j < 3; ++j) {
                    EntityComplexPart entitycomplexpart = null;
                    if (j == 0) {
                        entitycomplexpart = this.bp;
                    }

                    if (j == 1) {
                        entitycomplexpart = this.bq;
                    }

                    if (j == 2) {
                        entitycomplexpart = this.br;
                    }

                    double[] adouble2 = this.b(12 + j * 2, 1.0F);
                    float f14 = this.yaw * 3.1415927F / 180.0F + this.b(adouble2[0] - adouble[0]) * 3.1415927F / 180.0F;
                    float f15 = MathHelper.sin(f14);
                    f6 = MathHelper.cos(f14);
                    f7 = 1.5F;
                    float f18 = (float)(j + 1) * 2.0F;


                    entitycomplexpart.t_(); //
                    entitycomplexpart.setPositionRotation(this.locX - (double)((f11 * f7 + f15 * f18) * f2), this.locY + (adouble2[1] - adouble[1]) - (double)((f18 + f7) * f9) + 1.5D, this.locZ + (double)((f12 * f7 + f6 * f18) * f2), 0.0F, 0.0F);
                }


                    this.bx = this.breakBlocks(this.bn.getBoundingBox()) | this.breakBlocks(this.bo.getBoundingBox());



            }
        }
    }

    //basically converting rotation.
    private float b(double d0) {
        return (float)MathHelper.g(d0);
    }

    //a() compares the magnitude of the vector. If it's close to 0, returns a 0 vector, else returns a unit vector.
    private Vec3D getUnitOrZero(Vec3D x)
    {
        return x.a();
    }


    //This doesn't actually do anything in terms of damaging entities.
    // It really justs sets some values to false or true about knockback. It doesn't even change velocity of the entity.
    private void attemptKnockBackHitEntities(List<Entity> list) {
        for (Entity entity : list) {
            if (entity instanceof EntityLiving) {

                entity.damageEntity(DamageSource.mobAttack(this), 10.0F);
                this.a(this, entity);
            }
        }

    }


    //this method is called alot during run time.
    private void setHitEntityVelocity(List<Entity> list) {
        double boxDistanceX = (this.bo.getBoundingBox().a + this.bo.getBoundingBox().d) / 2.0D;
        double boxDistanceZ = (this.bo.getBoundingBox().c + this.bo.getBoundingBox().f) / 2.0D;

        for (Entity entity : list) {
            if (entity instanceof EntityLiving) {
                double distanceX = entity.locX - boxDistanceX;
                double distanceZ = entity.locZ - boxDistanceZ;
                double d4 = distanceX * distanceX + distanceZ * distanceZ;

                //adding velocity to the hit entity
                entity.g(distanceX / d4 * 4.0D, 0.20000000298023224D, distanceZ / d4 * 4.0D);
            }
          //  setTarget(null);
        }


    }


    private boolean breakBlocks(AxisAlignedBB axisalignedbb) {

        //Getting the edges of the hitboxes of the body parts.
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.floor(axisalignedbb.b);
        int k = MathHelper.floor(axisalignedbb.c);
        int l = MathHelper.floor(axisalignedbb.d);
        int i1 = MathHelper.floor(axisalignedbb.e);
        int j1 = MathHelper.floor(axisalignedbb.f);

        boolean hasRunIntoUnbreakableBlocks = false;
        boolean hasDestroyedBlocks = false;
        List<org.bukkit.block.Block> destroyedBlocks = new ArrayList<>();
        CraftWorld craftWorld = this.world.getWorld();

        for(int k1 = i; k1 <= l; ++k1) {
            for(int l1 = j; l1 <= i1; ++l1) {
                for(int i2 = k; i2 <= j1; ++i2) {
                    BlockPosition blockposition = new BlockPosition(k1, l1, i2);
                    net.minecraft.server.v1_8_R3.Block block = this.world.getType(blockposition).getBlock();


                    if (block.getMaterial() == Material.AIR) {
                        continue;
                    }

                    if (block != Blocks.BARRIER && block != Blocks.OBSIDIAN &&
                            block != Blocks.END_STONE && block != Blocks.BEDROCK && block != Blocks.COMMAND_BLOCK)
                    {
                        hasDestroyedBlocks = true;
                        destroyedBlocks.add(craftWorld.getBlockAt(k1, l1, i2));
                    } else {
                        hasRunIntoUnbreakableBlocks = true;
                    }

                }
            }
        }


        if (hasDestroyedBlocks) {

            for (org.bukkit.block.Block block : destroyedBlocks) {
                org.bukkit.Material blockId = block.getType();
                if (blockId != org.bukkit.Material.AIR) {
                    int blockX = block.getX();
                    int blockY = block.getY();
                    int blockZ = block.getZ();

                    this.world.setAir(new BlockPosition(blockX, blockY, blockZ));
                }
            }
        }

        return hasRunIntoUnbreakableBlocks;
    }


    //Attempts to damage entities and regain health
    private void attemptToBattle() {
        if (this.bz != null) {
            if (this.bz.dead) {
                if (!this.world.isClientSide) {
                    //  CraftEventFactory.entityDamage = this.bz;

                    this.damageEntity(this.bn, DamageSource.explosion(null), 10.0F);
                    //   CraftEventFactory.entityDamage = null;
                }

                this.bz = null;
            }
        }
    }


    public void damageEntity(EntityComplexPart entitycomplexpart, DamageSource damagesource, float f)
    {
       a(entitycomplexpart, damagesource,f);
    }

    public void dealRawDamage(DamageSource source, float damage) {
        dealDamage(source, damage);
        this.target = null;
        // you could probably add the death animation thingy here.
    }


    //This actually deals damage to the entity.
    @Override
    public boolean a(EntityComplexPart entitycomplexpart, DamageSource damagesource, float f) {
        if (entitycomplexpart != this.bn) {
            f = f / 4.0F + 1.0F;
        }

        this.a = centre.getX();
        this.b = centre.getY();
        this.c = centre.getZ();


        this.target = null;
        this.dealDamage(damagesource, f);


        return true;
    }

    private void attemptTargetRandomPlayer()
    {
        if (!valid)
            return;

        int iterations = 0;

        BattlePlayer[] players = arena.getPlayers().values().toArray(new BattlePlayer[arena.getPlayers().values().size()]);


        BattlePlayer nextTarget = players[rand.nextInt(players.length)];
        while (iterations < 10 && nextTarget.getTeam().equals(team) &&
                !(nextTarget.getIsAlive() && !(nextTarget.getIsEliminated()) && nextTarget.getRawPlayer().isOnline())) {
            nextTarget = players[rand.nextInt(players.length)];
            iterations ++;
        }


        if (nextTarget.getTeam().equals(this.team))
            return;
        else if (nextTarget.getIsAlive() && !(nextTarget.getIsEliminated()) && nextTarget.getRawPlayer().isOnline()) {
            handleEntityTarget(nextTarget.getRawPlayer());
            targetTime = System.currentTimeMillis();
            currentTarget = nextTarget;
            nextTime = rand.nextInt(16)+10;
            return;
        }


        this.bw = false;

        boolean flag;

        do {
            this.a = centre.getX();
            this.b = centre.getY() + this.random.nextFloat() * 50.0F;
            this.c = centre.getZ();

            this.a += this.random.nextFloat() * 80.0F - 60.0F;
            this.c += this.random.nextFloat() * 80.0F - 60.0F;

            double d0 = this.locX - this.a;
            double d1 = this.locY - this.b;
            double d2 = this.locZ - this.c;

            flag = d0 * d0 + d1 * d1 + d2 * d2 > 100.0D;
        } while(!flag);



    }


    //death animation thingy
    @Override
    protected void aZ() {


        if (!this.dead) {
            ++this.by;
            if (this.by >= 180 && this.by <= 200) {
                float f = (this.random.nextFloat() - 0.5F) * 8.0F;
                float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
                float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
                this.world.addParticle(EnumParticle.EXPLOSION_HUGE, this.locX + (double)f, this.locY + 2.0D + (double)f1, this.locZ + (double)f2, 0.0D, 0.0D, 0.0D);
            }

            this.aI = this.yaw += 20.0F;
            this.die();

        }
    }


    @Override
    public UUID getUUID() {
        return this.uniqueID;
    }

    @Override
    public BattleTeam getTeam() {
        return team;
    }

    @Override
    public String getType() {
        return "Dragon";
    }

    @Override
    public Cause getCauseType() {
        return Cause.NORMAL;
    }

    @Override
    public void register() {
        if (!valid)
            return;
      listener.addEntity(this);
    }

    @Override
    public void unregister() {
        if (!valid)
            return;
        listener.removeEntity(this.uniqueID);
    }
}
