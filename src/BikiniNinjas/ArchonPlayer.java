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
        Direction direction = Utilities.randomDirection();
        while(!rc.canHireGardener(direction)) {
            direction = Utilities.randomDirection();
        }
        rc.hireGardener(direction);
    }

    @Override
    protected void step() throws GameActionException {

        Team myTeam = rc.getTeam();
        TreeInfo[] myTrees = rc.senseNearbyTrees(TREE_SENSE_RADIUS, myTeam);
        MapLocation myLocation = rc.getLocation();

        if (myTrees.length != 0) {
            if (target == null) setRandomTarget(myLocation);
        }
        else {
            Direction spawnDir = (isAtTarget(myLocation) ? Utilities.randomDirection() : myLocation.directionTo(target).opposite());
            if (rc.canHireGardener(spawnDir) && 3 * RobotType.GARDENER.bulletCost <= rc.getTeamBullets()  &&
                rc.getTreeCount() >= (bc.getCountOf(RobotType.GARDENER) - 1) * 6) {
                bm.build(RobotType.GARDENER, spawnDir);
                setRandomTarget(myLocation);
            }
        }

        if (!isAtTarget(myLocation)) {
            while (!Utilities.tryMove(rc, myLocation.directionTo(target))) {
                setRandomTarget(myLocation);
            }
            rc.setIndicatorLine(myLocation, target, 0, 0, 255);
        }
        else {
            target = null;
        }
    }

    private boolean isAtTarget(MapLocation location) {
        return target == null || location.distanceTo(target) < 0.5;
    }

    private void setRandomTarget(MapLocation location) {
        target = location.add(Utilities.randomDirection(), TARGET_DISTANCE);
    }
}
