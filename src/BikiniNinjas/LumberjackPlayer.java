package BikiniNinjas;

import battlecode.common.*;

public class LumberjackPlayer {

    private static RobotController rc;
    private static Team enemy;

    public static void run(RobotController rc) throws GameActionException {

        LumberjackPlayer.rc = rc;
        enemy = rc.getTeam().opponent();

        while (true) {
            try {
                innerLoop();
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }

    private static void innerLoop() throws GameActionException {

        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

        if(robots.length > 0 && !rc.hasAttacked()) {
            // Use strike() to hit all nearby robots!
            rc.strike();
        } else {
            // No close robots, so search for robots within sight radius
            robots = rc.senseNearbyRobots(-1,enemy);

            // If there is a robot, move towards it
            if(robots.length > 0) {
                MapLocation myLocation = rc.getLocation();
                MapLocation enemyLocation = robots[0].getLocation();
                Direction toEnemy = myLocation.directionTo(enemyLocation);

                Utilities.tryMove(rc, toEnemy);
            } else {
                // Move Randomly
                Utilities.tryMove(rc, Utilities.randomDirection());
            }
        }
    }
}
