package BikiniNinjas;
import battlecode.common.*;

public strictfp class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        getPlayer(rc).run();
    }

    private static AbstractPlayer getPlayer(RobotController rc) throws GameActionException {
        switch (rc.getType()) {
            case ARCHON:
                return new ArchonPlayer(rc);
            case GARDENER:
                return new GardenerPlayer(rc);
            case LUMBERJACK:
                return new LumberjackPlayer(rc);
            case SOLDIER:
                return new SoldierPlayer(rc);
            case TANK:
                return new TankPlayer(rc);
            case SCOUT:
                return new ScoutPlayer(rc);
            default:
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "no suitable robot type found");
        }
    }
}
