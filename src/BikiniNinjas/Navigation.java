package BikiniNinjas;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Map;

public class Navigation {

    enum Avoidance {
        LEFT,
        RIGHT,
        NONE
    }

    enum StopFlag {
        SUCCESS,
        UNREACHABLE,
        OUT_OF_PATIENCE,
        MANUALLY_STOPPED
    }

    private RobotController rc;

    private MapLocation targetLocation;
    private Avoidance avoidance;
    private Direction orientation;

    public StopFlag stopFlag;

    private final int DEG_RESOLUTION = 7;

    public Navigation(RobotController rc) {
        this.rc = rc;
        this.targetLocation = null;
        this.avoidance = Avoidance.NONE;
        this.stopFlag = StopFlag.SUCCESS;
    }

    public boolean isNavigating() {
        return targetLocation != null;
    }

    public void stopNavigation() {
        targetLocation = null;
        stopFlag = StopFlag.MANUALLY_STOPPED;
    }

    private void stopNavigation(StopFlag flag) {
        targetLocation = null;
        stopFlag = flag;
    }

    public void navigateTo(MapLocation location) throws GameActionException {
        if (location == targetLocation) return;
        targetLocation = location;
        avoidance = Avoidance.NONE;
        orientation = location.directionTo(targetLocation);

        step();
    }

    protected void step() throws GameActionException {
        if(!isNavigating()) return;

        Direction dirToTarget = rc.getLocation().directionTo(targetLocation);
        if (rc.getLocation().distanceSquaredTo(targetLocation) < 0.001 || dirToTarget == null) {
            stopNavigation(StopFlag.SUCCESS);
            return;
        }

        if (moveDirectly()) return;

        if (avoidance == Avoidance.NONE) {
            orientation = dirToTarget;
            avoidance = (Math.random() < 0.5 ? Avoidance.LEFT : Avoidance.RIGHT);

            for(int i = 0; i <= 360 / DEG_RESOLUTION; i++) {

                Direction newOrientation;
                if (avoidance == Avoidance.LEFT) {
                    newOrientation = orientation.rotateLeftDegrees(DEG_RESOLUTION * i);
                }
                else {
                    newOrientation = orientation.rotateRightDegrees(DEG_RESOLUTION * i);
                }

                if(rc.canMove(newOrientation)) {
                    rc.move(newOrientation);
                    orientation = newOrientation;
                    rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newOrientation, 5), 255, 255, 255);
                    return;
                }

                rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newOrientation, 5), 0, 0, 0);
            }

            stopNavigation(StopFlag.UNREACHABLE);
        }

        if (!moveAroundObstacle()) {
            stopNavigation(StopFlag.UNREACHABLE);
        }
    }

    private boolean moveDirectly() throws GameActionException {

        Direction dir = rc.getLocation().directionTo(targetLocation);

        if (!rc.canMove(dir)) return false;
        rc.move(targetLocation);
        avoidance = Avoidance.NONE;

        return true;
    }

    private boolean moveAroundObstacle() throws GameActionException {

        for (int i = 0; i <= 360 / DEG_RESOLUTION; i++) {

            Direction newOrientation;
            if (avoidance == Avoidance.LEFT) {
                newOrientation = orientation.rotateLeftDegrees(DEG_RESOLUTION * i - 90);
            } else {
                newOrientation = orientation.rotateRightDegrees(DEG_RESOLUTION * i - 90);
            }

            if (rc.canMove(newOrientation)) {
                rc.move(newOrientation);
                orientation = newOrientation;
                rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newOrientation, 5), 255, 255, 255);

                return true;
            }

            rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newOrientation, 5), 0, 0, 0);
        }

        return false;
    }
}
