package me.camm.productions.bedwars.Util.Explosions;



import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.List;

import static java.lang.Double.NaN;


/*
 *
 * @author CAMM
 * Models a class for calculating velocity to apply to entities
 *
 *
 */
public class KnockbackCalculator {
    private final EntityExplodeEvent event;
    private final boolean isFireball;
    private double range;

    public KnockbackCalculator(EntityExplodeEvent event)
    {
        this.event = event;
        Entity entity = event.getEntity();
      isFireball = entity instanceof Fireball  || entity.getType().toString().toLowerCase().contains("fireball");
      range = isFireball? VectorParameter.FIREBALL_RANGE.getValue() : VectorParameter.TNT_RANGE.getValue();
      range /= 2;
    }


    public void applyVelocity()  //unfinished. Need to refactor since physics is not entirely accurate. Note added 2021-11m-16d
    {

        Entity exploded = this.event.getEntity();
        Location origin = exploded.getLocation().clone();

        List<Entity> nearEntities = exploded.getNearbyEntities(range, range, range);



        for (Entity e: nearEntities) //for all of the nearby entities to the explosion..
        {
            if (!VectorUtils.isValidVelocityType(e))   //So if the entity can be affected by velocity
               continue;

            constructAndImpart(origin, e.getLocation().clone(),e);

        }//for nearby
    }//method


    public Vector convertToFireballDist(Location entityLoc, Entity target, Location explosionLoc){

        AxisAlignedBB box = ((CraftEntity)target).getHandle().getBoundingBox();
        double yHalf = (box.e-box.b)/2 + box.b;
        Location centreMass =
                new Location(entityLoc.getWorld(), (box.d-box.a)/2 + box.a, (box.e-yHalf)/2 + yHalf,(box.f-box.c)/2 + box.c);


        double delX, delY, delZ;
        delX = centreMass.getX() - explosionLoc.getX();
        delY = centreMass.getY() - explosionLoc.getY();
        delZ = centreMass.getZ() - explosionLoc.getZ();


        return new Vector(delX, delY, delZ);

    }

    public Vector convertToTNTDist(Location entityLoc, Location explosionLoc){

            entityLoc.add(0,0.5,0);


        return new Vector(entityLoc.getX() - explosionLoc.getX(),
                entityLoc.getY() - explosionLoc.getY(),
                entityLoc.getZ() - explosionLoc.getZ());




    }

    public Vector convertToTNTProjectileDist(Location entityLoc, Location explosionLoc) {

        entityLoc.add(0.5,0.5,0.5);
        explosionLoc.add(0.5,0.5,0.5);

        return new Vector(entityLoc.getX() - explosionLoc.getX(), entityLoc.getY() - explosionLoc.getY(), entityLoc.getZ() - explosionLoc.getZ());

    }

    /*
    Construct the velocity and imparts it on to the entity
    @author CAMM

     */
    private void constructAndImpart(Location explosionLoc, Location entityLoc, Entity target){



          /*
        Bounding box fields:

        a = min X
        b = min Y
        c = min Z

        d = max X
        e = max Y
        f = max Z

        See: https://nms.screamingsandals.org/1.8.8/net/minecraft/server/VVV/AxisAlignedBB.html
        See: https://github.com/KevyPorter/Minecraft-Forge-Utils/blob/master/fields.csv
         */



        /*

        Hypothesis:
        The y magnitude is the total magnitude of the explosion split into the x,y,and z axes.
        the x and z do not split up the H magnitude, but it is instead used for both.

        Use the centre of mass (Not the head, but the centre of the body.)

          Results: Promising.

          also check on explosion pwr to make it stronger

         */


        double delX,delY,delZ;
        Vector conversion;

        if (isFireball) {
            conversion = convertToFireballDist(entityLoc, target, explosionLoc);
        }
        else if (target instanceof TNTPrimed){
          conversion = convertToTNTProjectileDist(entityLoc, explosionLoc);
        }
        else
            conversion = convertToTNTDist(entityLoc, explosionLoc);

        delX = conversion.getX();
        delY = conversion.getY();
        delZ = conversion.getZ();

        double totalDist = Math.sqrt(delX*delX + delY*delY + delZ*delZ); //hypotenuse for vert angle
        double horDistance = Math.sqrt(delX*delX +delZ*delZ);


        double totalMagnitude;

        if (target instanceof TNTPrimed) {
            totalMagnitude = getTNTProjectileMagnitude(totalDist);
        }
        else {
            totalMagnitude = isFireball? getFireballVectorMagnitude(totalDist) : getTNTVectorMagnitude(totalDist);

        }



        if (totalMagnitude == 0)
            return;

        boolean straightUp = false;
        boolean onlyHorizontal = false;

        //theta = arcsin(opposite/hypotenuse)
        //we are suggesting that sine represents y axis (from a side view, since sine90 = 1, which is directly up)
        //1*totalMag = player goes directly up. as opposed to cos, which goes to 0.
        //0*totalMag = player goes nowhere.


        /*
        Get the vertical angle of the motion.
        If the horDistance ==0, we want to avoid div by 0, so it is 90* since it is straight up.

        theta = atan (opp / adj)
         */
        double vertAngle;

        /*
             +z (90)
             |
             |
       ------------- +x (0)
             |
             |
        Accounting for edge cases here.
        since we cannot div by 0, we say it's 90*, so directly up.
         */
        if (horDistance == 0) {
            vertAngle = Math.toRadians(90);
        }
        else if (delY == 0)
        {

            //so if the displacement y is 0, we know that there is no upwards motion. There is no vert angle.
            vertAngle = NaN;
            onlyHorizontal = true;
        }
        else {
            /*
            Theta = tan^-1 (opp/adj)
                |
                |  y
            ----|
         hor dist

             */
            vertAngle = Math.atan(delY/horDistance);
        }



        double horAngle;
        if (delX == 0 && delZ == 0) {
            //if hor dist = 0, then hor Angle dne, and we are going straight up.
            horAngle = NaN;
            straightUp = true;
        } else if (delX == 0 || delZ == 0) {

            //if 1 angle is 0, we account for an edge case.
            horAngle = 0;

            //if x is positive, then we have 0 deg, else 180.
            /*
           +z (90)
             |
             |
       ------------- +x (0)
             |
             |
             */
            if (delZ == 0)
                horAngle = delX > 0 ? 0: Math.toRadians(180);


            //same with the z values.
            if (delX == 0)
                horAngle = delZ > 0 ? Math.toRadians(90) : Math.toRadians(270);
        }
        else {
            //the angle is the tan of delZ and delX, with Z as the "y axis" and x as the "x axis".
            horAngle = Math.atan(delZ/delX);
            //this gives a value close to 0 from the negative side if the position of the player
            // is on the -x side, so we must add 180* to rotate it to the correct direction it if so.

            if (delX<0)
                horAngle += Math.PI;  //180* in rad form
        }

        double xVel, yVel, zVel;
        double horMagnitude;



        //splitting up the hor magnitude into magnitudes for the x and z planes.
        if (onlyHorizontal) {
            yVel = 0;
            horMagnitude = totalMagnitude;
        }
        else {
            yVel = totalMagnitude * Math.sin(vertAngle);
            horMagnitude = totalMagnitude * Math.cos(vertAngle);
        }


        if (straightUp) {
            xVel = 0;
            zVel = 0;
        }
        else {
            xVel = horMagnitude * Math.cos(horAngle);
            zVel = horMagnitude * Math.sin(horAngle);
        }


        //impart the velocity
        impartVelocity(xVel,yVel,zVel, target);

    }

    /*
    returns a value for the y value of the velocity vector. x is used since
     the value is calculated through a function.

  distance is the distance from the explosion
  MAX is the max velocity to impart on the entity



     */
    private double getTNTVectorMagnitude(double distance){

        double offset = 1;
        distance -= offset;
        final double MAX = 1.86;
        if (distance <= offset)
            return MAX;
        else {

            if (distance >= VectorParameter.TNT_RANGE.getValue())
                return 0;
            //this is a function for a graph.
            //we used measurements to get a series of points, then made a graph that best fits those
            //points for the velocity.
            // (Are there other ways to write this w/o the magic numbers?)

        /*
        -0.00489x^6 + 0.088x^5 - 0.602x^4 + 1.916x^3 -2.702x^2 +0.942x +1.86
         */
            double magnitude = (-0.00489 * (Math.pow(distance, 6))) +
                    (0.088 * (Math.pow(distance, 5))) -
                    (0.602 * (Math.pow(distance, 4))) +
                    (1.916 * (distance * distance * distance)) -
                    (2.702 * (distance * distance)) +
                    (0.942 * distance) +
                    (MAX);

            return Math.max(magnitude, 0);
        }
    }






    //need to check
    private double getTNTProjectileMagnitude(double distance) {
        final double max = 0.8;
        if (distance <1)
            return max;

        double mag  = -0.0158333*(Math.pow(distance, 4)) +
                0.178333*(Math.pow(distance, 3)) -
                0.699167 * (distance * distance) +
                0.936667 * distance +
                0.4;

        return Math.max(Math.min(mag, max), 0);

                //-0.0158333x^{4}+0.178333x^{3}-0.699167x^{2}+0.936667x+0.4
    }

    private double getFireballVectorMagnitude(double distance) {
//0.00136847x^5 - 0.0108515x^4 - 0.00874143x^3 + 0.209415x^2 - 0.668084x + 1.78
        final double max = 1.78;


        if (distance <= 1)
            return max;

        if (distance >= VectorParameter.FIREBALL_RANGE.getValue())
            return 0;

        //we need to shift the distance.
        distance = distance - 1;

        double mag  = 0.00136847 * Math.pow(distance, 5) -
                0.0108515 * Math.pow(distance, 4) -
                0.0087143 * Math.pow(distance, 3) +
                0.209415 * distance * distance -
                0.668084 * distance +
                1.78;

        return Math.max(mag, 0);
    }

    /*
    Imparts velocity onto the entity

     */
    private void impartVelocity(double xComponent, double yComponent, double zComponent, Entity targeted)
    {

        double MAX = VectorParameter.MAX_MAGNITUDE.getValue();
        Vector calculated = new Vector(xComponent,yComponent,zComponent);
        Vector entityVel = targeted.getVelocity();

        if (calculated.clone().add(entityVel).lengthSquared() <= (MAX * MAX))
        {
            targeted.setVelocity(entityVel.add(calculated));
            return;
        }

        Vector sum = calculated.add(entityVel);
        targeted.setVelocity(sum.normalize().multiply(MAX));
    }



    /*
    if tnt use eqn -0.0158333x^{4}+0.178333x^{3}-0.699167x^{2}+0.936667x+0.4
    and if x < 1 use 0.8
     */

}

