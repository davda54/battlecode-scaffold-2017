package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoldierPlayer extends AbstractPlayer {
    MapLocation[] archonLocs;
    int archonIndex = 0;

    public SoldierPlayer(RobotController rc) throws GameActionException {
        super(rc);
        archonLocs = rc.getInitialArchonLocations(enemy);
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
        List<BulletInfo> list = new ArrayList<>();
        for (BulletInfo b : bullets) {
            if (Math.abs(b.dir.degreesBetween(b.location.directionTo(myLocation))) <= thresholdAngle) {
                list.add(b);
            }
        }
        List<BulletInfo> dangerousBullets = list;
        int bulletCountThreshold = 15;
        if (bulletCountThreshold <= dangerousBullets.size() && rc.canFirePentadShot()) {
            //average angle
            Float acc = 0f;
            for (BulletInfo b : dangerousBullets) {
                Direction d = myLocation.directionTo(b.location);
                Float radians = d.radians;
                acc = acc + radians;
            }
            float shootDirRads = acc / dangerousBullets.size();
            rc.firePentadShot(new Direction(shootDirRads));
        }

        if (!navigation.isNavigating()) {

            MapLocation target = archonLocs[archonIndex];
            navigation.navigateTo(target);
        } else {
            if (myLocation.distanceTo(archonLocs[archonIndex]) <= 15 && robots.length == 0) {
                archonIndex = (archonIndex + 1) % archonLocs.length;
                navigation.navigateTo(archonLocs[archonIndex]);
            }


        }
        //TODO komunikace? (jeden soldier zaveli, jdeme na dalsiho archona, ostatni nasleduji...)
        //TODO attack/defense mod?
        //TODO Archon request defense?
    }


}
