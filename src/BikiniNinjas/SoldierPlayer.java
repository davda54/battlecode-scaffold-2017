package BikiniNinjas;

import battlecode.common.*;

public class SoldierPlayer {

    private static RobotController rc;
    private static Team enemy;

    public static void run(RobotController rc) throws GameActionException {

        SoldierPlayer.rc = rc;
        enemy = rc.getTeam().opponent();

        while (true) {
            try {
                innerLoop();
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    private static void innerLoop() throws GameActionException {

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

        // Move randomly
        Utilities.tryMove(rc, Utilities.randomDirection());
    }
}
