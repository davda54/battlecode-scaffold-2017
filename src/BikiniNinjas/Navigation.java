package BikiniNinjas;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.HashSet;
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
    private int avoidanceCooldown;
    private ArrayList<Tuple<MapLocation, Avoidance>> collisionMemory;

    public StopFlag stopFlag;

    private final int AVOIDANCE_COOLDOWN = 50;
    private final int DEG_RESOLUTION = 7;

    public Navigation(RobotController rc) {
        this.rc = rc;
        this.targetLocation = null;
        this.avoidance = Avoidance.NONE;
        this.avoidanceCooldown = 0;
        this.collisionMemory = new ArrayList<>();
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
        avoidanceCooldown = 0;
        collisionMemory.clear();
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

        if (/*avoidanceCooldown <= 0 &&*/ avoidance == Avoidance.NONE) {
            avoidanceCooldown = AVOIDANCE_COOLDOWN;
            orientation = dirToTarget;

            Direction newLeftOrientation = null;
            for(int i = 0; i <= 360 / DEG_RESOLUTION; i++) {
                newLeftOrientation = orientation.rotateLeftDegrees(DEG_RESOLUTION * i);
                if(rc.canMove(newLeftOrientation)) break;
                else newLeftOrientation = null;
            }

            Direction newRightOrientation = null;
            for(int i = 0; i <= 360 / DEG_RESOLUTION; i++) {
                newRightOrientation = orientation.rotateRightDegrees(DEG_RESOLUTION * i);
                if(rc.canMove(newRightOrientation)) break;
                else newRightOrientation = null;
            }

            if (newLeftOrientation != null || newRightOrientation != null) {

                if (newLeftOrientation == null) {
                    orientation = newRightOrientation;
                    avoidance = Avoidance.LEFT;
                } else if (newRightOrientation == null) {
                    orientation = newLeftOrientation;
                    avoidance = Avoidance.RIGHT;
                } else {
                    Avoidance memory = locationInMemory(rc.getLocation());
                    if (memory == Avoidance.NONE) {
                        if (Math.abs(newLeftOrientation.degreesBetween(dirToTarget)) > Math.abs(newRightOrientation.degreesBetween(dirToTarget))) {
                            orientation = newRightOrientation;
                            avoidance = Avoidance.RIGHT;
                        } else {
                            orientation = newLeftOrientation;
                            avoidance = Avoidance.LEFT;
                        }
                    } else {
                        avoidance = memory == Avoidance.LEFT ? Avoidance.RIGHT : Avoidance.LEFT;
                        orientation = memory == Avoidance.LEFT ? newRightOrientation : newLeftOrientation;
                    }
                }

                collisionMemory.add(new Tuple<>(rc.getLocation(), avoidance));
                rc.move(orientation);
                rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(orientation, 4), 255, 255, 255);
                return;
            }

            stopNavigation(StopFlag.UNREACHABLE);
        }

        avoidanceCooldown--;
        if (!moveAroundObstacle()) {
            stopNavigation(StopFlag.UNREACHABLE);
        }
    }

    private Avoidance locationInMemory(MapLocation location) {

        for (Tuple<MapLocation, Avoidance> l : collisionMemory) {
            if (location.distanceSquaredTo(l.item1) > 0.25) continue;
            return l.item2;
        }

        return Avoidance.NONE;
    }

    private boolean moveDirectly() throws GameActionException {

        Direction dir = rc.getLocation().directionTo(targetLocation);

        if (orientation != null && Math.abs(orientation.degreesBetween(dir)) > 120) return false;

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
