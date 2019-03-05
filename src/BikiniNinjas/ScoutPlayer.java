package BikiniNinjas;

import battlecode.common.*;

public class ScoutPlayer {

    private static RobotController rc;

    public static void run(RobotController rc) throws GameActionException {

        ScoutPlayer.rc = rc;

        while (true) {
            try {
                innerLoop();
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }

    private static void innerLoop() throws GameActionException {

    }
}
