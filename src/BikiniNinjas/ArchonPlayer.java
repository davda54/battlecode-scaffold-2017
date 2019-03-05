package BikiniNinjas;

import battlecode.common.*;

public class ArchonPlayer extends AbstractPlayer {

    public ArchonPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        Direction direction = Utilities.randomDirection();
        while(!rc.canHireGardener(direction)) {
            direction = Utilities.randomDirection();
        }
        rc.hireGardener(direction);
    }

    @Override
    protected void step() throws GameActionException {
        // Generate a random direction
        Direction dir = Utilities.randomDirection();

        // Randomly attempt to build a gardener in this direction
        if (rc.canHireGardener(dir) && Math.random() < .01) {
            rc.hireGardener(dir);
        }

        // Move randomly
        Utilities.tryMove(rc, Utilities.randomDirection());

        // Broadcast archon's location for other robots on the team to know
        MapLocation myLocation = rc.getLocation();
        rc.broadcast(0,(int)myLocation.x);
        rc.broadcast(1,(int)myLocation.y);
    }
}
