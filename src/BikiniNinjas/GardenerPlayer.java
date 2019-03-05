package BikiniNinjas;

import battlecode.common.*;

public class GardenerPlayer {

    private static RobotController rc;

    public static void run(RobotController rc) throws GameActionException {

        GardenerPlayer.rc = rc;

        while (true) {
            try {
                innerLoop();
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    private static void innerLoop() throws GameActionException {

        // Listen for home archon's location
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos, yPos);

        // Generate a random direction
        Direction dir = Utilities.randomDirection();

        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
            rc.buildRobot(RobotType.SOLDIER, dir);
        } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
            rc.buildRobot(RobotType.LUMBERJACK, dir);
        }

        // Move randomly
        Utilities.tryMove(rc, Utilities.randomDirection());
    }
}
