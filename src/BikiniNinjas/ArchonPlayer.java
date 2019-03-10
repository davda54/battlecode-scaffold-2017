package BikiniNinjas;

import battlecode.common.*;

import java.util.Locale;

public class ArchonPlayer extends AbstractPlayer {

    private MapLocation target;

    private final int TREE_SENSE_RADIUS = 5;
    private final int TARGET_DISTANCE = 3;

    public ArchonPlayer(RobotController rc) throws GameActionException {
        super(rc);
        target = null;
    }

    @Override
    protected void initialize() throws GameActionException {
        Direction direction = directionTowardEnemy();
        for (int i = 0; i < 36; i++) {
            if (rc.canHireGardener(direction)) {
                bm.build(RobotType.GARDENER, direction);
                break;
            }
            direction = direction.rotateLeftDegrees(10);
        }
    }

    @Override
    protected void step() throws GameActionException {

        Team myTeam = rc.getTeam();
        TreeInfo[] myTrees = rc.senseNearbyTrees(TREE_SENSE_RADIUS, myTeam);
        MapLocation myLocation = rc.getLocation();

        if (myTrees.length != 0) {
            if (!navigation.isNavigating()) setRandomTarget(myLocation);
            return;
        }

        if (navigation.isNavigating()) navigation.stopNavigation();
        for (int c = 0; c < 10; c++) {
            Direction spawnDir = Utilities.randomDirection();
            if (rc.canHireGardener(spawnDir) && 3 * RobotType.GARDENER.bulletCost <= rc.getTeamBullets()  &&
                    rc.getTreeCount() >= (bc.getCountOf(RobotType.GARDENER) - 1) * 6) {
                bm.build(RobotType.GARDENER, spawnDir);
                setRandomTarget(myLocation);
                break;
            }
        }
    }

    private void setRandomTarget(MapLocation location) throws GameActionException {
        target = null;
        for (int c = 0; c < 50; c++) {

            Direction dir = Utilities.randomDirection();
            float newCircleRadius = 3;
            MapLocation newLocation = rc.getLocation().add(dir, (10.0f - newCircleRadius) * (float)Math.random());

            if (rc.canSenseAllOfCircle(newLocation, newCircleRadius) &&
                    rc.onTheMap(newLocation, newCircleRadius) &&
                    !rc.isCircleOccupiedExceptByThisRobot(newLocation, newCircleRadius)) {
                target = newLocation;
                break;
		    }
        }
        if (target != null) navigation.navigateTo(target);
    }
}
