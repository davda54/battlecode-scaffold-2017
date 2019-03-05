package BikiniNinjas;

import battlecode.common.*;

public class ArchonPlayer {

    private static RobotController rc;

    public static void run(RobotController rc) throws GameActionException {

        ArchonPlayer.rc = rc;

        while (true) {
            try {
                innerLoop();
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

    private static void innerLoop() throws GameActionException {

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
