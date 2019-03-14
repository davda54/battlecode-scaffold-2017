package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;
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
        Direction direction = directionTowardEnemy().opposite();
        for (int i = 0; i < 36; i++) {
            if (rc.canHireGardener(direction)) {
                bm.build(RobotType.GARDENER, direction);
                break;
            }
            direction = direction.rotateLeftDegrees(70);
        }
    }

    @Override
    protected void step() throws GameActionException {

        Team myTeam = rc.getTeam();
        int treeCount = rc.senseNearbyTrees(TREE_SENSE_RADIUS, myTeam).length;
        int gardenerCount = getNearbyGardeners().size();
        MapLocation myLocation = rc.getLocation();
        if(target != null) rc.setIndicatorDot(target, 125, 125, 125);

        if (treeCount + gardenerCount > 0) {
            if (!navigation.isNavigating()) setRandomTarget(myLocation);
        }

        for (int c = 0; c < 10; c++) {
            Direction spawnDir = Utilities.randomDirection();
            if (rc.canHireGardener(spawnDir) && 3 * RobotType.GARDENER.bulletCost <= rc.getTeamBullets() &&
                    rc.getTreeCount() >= (bc.getCountOf(RobotType.GARDENER) - 1) * 6) {
                bm.build(RobotType.GARDENER, spawnDir);
                break;
            }
        }
    }

    private void setRandomTarget(MapLocation location) throws GameActionException {
        target = null;
        for (int c = 0; c < 20; c++) {

            Direction dir = Utilities.randomDirection();
            float newCircleRadius = 3;
            MapLocation newLocation = rc.getLocation().add(dir, (9.0f - newCircleRadius) * (float)(Math.random()*0.5+0.5));

            if (!isCircleOccupiedExceptByMyTreesAndThisRobot(newLocation, newCircleRadius)) {
                target = newLocation;
                break;
		    }
        }
        if (target != null) {
            navigation.navigateTo(target);
            return;
        }

        if(!rc.hasMoved() && rc.canMove(directionTowardEnemy())) {
            rc.move(directionTowardEnemy());
        }
    }

    @Override
    protected boolean tooMuchBytecode(int bytecodeCount) { return bytecodeCount > 20000; }

    private boolean isCircleOccupiedExceptByMyTreesAndThisRobot(MapLocation location, float radius) throws GameActionException {
        if(!rc.canSenseAllOfCircle(location, radius)) return true;
        if(!rc.onTheMap(location, radius)) return true;

        return rc.isCircleOccupiedExceptByThisRobot(location, radius);
        //if(rc.senseNearbyRobots(location, radius, rc.getTeam()).length > 0) return false;
        //if(rc.senseNearbyTrees(location, radius, rc.getTeam()).length > 0) return false;
        //return true;
    }
}
