package BikiniNinjas;

import battlecode.common.*;

public class ScoutPlayer {
    public static void run(RobotController rc) throws GameActionException {
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

    private static void innerLoop() {

    }
}
