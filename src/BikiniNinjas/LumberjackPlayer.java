package BikiniNinjas;

import battlecode.common.*;

public class LumberjackPlayer extends AbstractPlayer {

    public LumberjackPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {

    }

    @Override
    protected void step() throws GameActionException {
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
