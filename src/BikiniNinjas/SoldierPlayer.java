package BikiniNinjas;

import battlecode.common.*;

import java.util.Arrays;
import java.util.List;

public class SoldierPlayer extends AbstractPlayer {

    public SoldierPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {

    }

    @Override
    protected void step() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }
        //shooting other bots is preferred, otherwise shoot in direction of dangerous bullets
        double thresholdAngle = 15;
        BulletInfo[] bullets = rc.senseNearbyBullets();
        BulletInfo[] dangerousBullets = (BulletInfo[]) Arrays.stream(bullets).filter(b -> Math.abs( b.dir.degreesBetween(b.location.directionTo(myLocation)))<= thresholdAngle).toArray();
        int bulletCountThreshold = 15;
        if (bulletCountThreshold <= dangerousBullets.length && rc.canFirePentadShot() ){
            float shootDirRads = Arrays.stream(dangerousBullets).map(b->myLocation.directionTo(b.location)).map(d -> d.radians).reduce(0f, (f1,f2)-> f1+f2 )/dangerousBullets.length;
            rc.firePentadShot(new Direction(shootDirRads));
        }


        // Move randomly
        Utilities.tryMove(rc, Utilities.randomDirection());
    }


}
