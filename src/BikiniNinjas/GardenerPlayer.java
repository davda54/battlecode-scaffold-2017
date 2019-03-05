package BikiniNinjas;

import battlecode.common.*;

public class GardenerPlayer extends AbstractPlayer {

    public GardenerPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        Direction direction = Utilities.randomDirection();
        while(!rc.canBuildRobot(RobotType.SCOUT, direction)) {
            direction = Utilities.randomDirection();
        }
        rc.buildRobot(RobotType.SCOUT, direction);
    }

    @Override
    protected void step() throws GameActionException {
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
