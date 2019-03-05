package BikiniNinjas;
import battlecode.common.*;

public strictfp class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        switch (rc.getType()) {
            case ARCHON:
                ArchonPlayer.run(rc);
                break;
            case GARDENER:
                GardenerPlayer.run(rc);
                break;
            case SOLDIER:
                SoldierPlayer.run(rc);
                break;
            case LUMBERJACK:
                LumberjackPlayer.run(rc);
                break;
            case SCOUT:
                ScoutPlayer.run(rc);
                break;
        }
	}
}
