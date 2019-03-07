package BikiniNinjas;

import battlecode.common.*;

import java.util.Locale;

public class ArchonPlayer extends AbstractPlayer {

    private MapLocation target;

    private final int TREE_SENSE_RADIUS = 5;
    private final int TARGET_DISTANCE = 3;
    private final int BULLET_RESERVE= 500;
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
            if (!navigation.isNavigating()) setRandomTarget(myLocation);
            return;
        }

        if (navigation.isNavigating()) navigation.stopNavigation();
        for (int c = 0; c < 100; c++) {
            Direction spawnDir = Utilities.randomDirection();
            if (rc.canHireGardener(spawnDir) && 3 * RobotType.GARDENER.bulletCost <= rc.getTeamBullets()  &&
                    rc.getTreeCount() >= (bc.getCountOf(RobotType.GARDENER) - 1) * 6) {
                bm.build(RobotType.GARDENER, spawnDir);
                setRandomTarget(myLocation);
                return;
            }
        }

        if (rc.getTeamBullets() > BULLET_RESERVE){
            float bulletDiff = rc.getTeamBullets() - BULLET_RESERVE;
            int victoryPointCount = (int) (bulletDiff/rc.getVictoryPointCost());
            rc.donate(victoryPointCount * rc.getVictoryPointCost());
        }

    }

    private void setRandomTarget(MapLocation location) throws GameActionException {
        target = null;
        for (int c = 0; c < 50; c++) {

            Direction dir = Utilities.randomDirection();
            float newCircleRadius = 3;
            MapLocation newLocation = rc.getLocation().add(dir, (10.0f - newCircleRadius) * (float)Math.random());

            if (rc.canSenseAllOfCircle(newLocation, newCircleRadius) &&
                    !rc.isCircleOccupiedExceptByThisRobot(newLocation, newCircleRadius)) {
                target = newLocation;
                break;
		    }
        }
        if (target != null) navigation.navigateTo(target);
    }
}
