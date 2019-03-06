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

    private RobotController rc;

    private MapLocation targetLocation;
    private Avoidance avoidance;
    Direction orientation;

    private final int DEG_RESOLUTION = 7;

    public Navigation(RobotController rc) {
        this.rc = rc;
        this.targetLocation = null;
        this.avoidance = Avoidance.NONE;
    }

    public boolean isNavigating() {
        return targetLocation != null;
    }

    public void stopNavigation() {
        targetLocation = null;
    }

    public void navigateTo(MapLocation location) throws GameActionException {
        if (location == targetLocation) return;
        targetLocation = location;
        avoidance = Avoidance.NONE;
        orientation = location.directionTo(targetLocation);
        step();
    }

    protected void step() throws GameActionException {

        if (rc.getLocation().distanceSquaredTo(targetLocation) < 0.001) {
            stopNavigation();
        }

        if (moveDirectly()) return;

        if (avoidance == Avoidance.NONE) {
            avoidance = (Math.random() < 0.5 ? Avoidance.LEFT : Avoidance.RIGHT);

            int i = 0;
            while(i <= 360 / DEG_RESOLUTION) {

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
                    return;
                }

                i++;
            }

            stopNavigation();
        }

        if (!moveAroundObstacle()) {
            stopNavigation();
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

        int i = 0;
        while(i <= 360 / DEG_RESOLUTION) {

            Direction newOrientation;
            if (avoidance == Avoidance.LEFT) {
                newOrientation = orientation.rotateLeftDegrees(DEG_RESOLUTION * i - 90);
            }
            else {
                newOrientation = orientation.rotateRightDegrees(DEG_RESOLUTION * i - 90);
            }

            if(rc.canMove(newOrientation)) {
                rc.move(newOrientation);
                orientation = newOrientation;
                return true;
            }

            i++;
        }
        return false;
    }
}
