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
    private final int DEG_RESOLUTION = 5;

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

        if (!rc.hasMoved()) step();
    }

    public void navigateToMoving(MapLocation location) throws GameActionException {
        if (location == targetLocation) return;
        targetLocation = location;
        orientation = location.directionTo(targetLocation);

        if (!rc.hasMoved()) step();
    }

    protected void step() throws GameActionException {
        if(!isNavigating()) return;

        Direction dirToTarget = rc.getLocation().directionTo(targetLocation);
        if (rc.getLocation().distanceSquaredTo(targetLocation) < 0.001 || dirToTarget == null) {
            stopNavigation(StopFlag.SUCCESS);
            return;
        }

        if (moveDirectly()) return;

        if(avoidanceCooldown < 0) {
            stopNavigation(StopFlag.OUT_OF_PATIENCE);
            return;
        }

        if (/*avoidanceCooldown <= 0 &&*/ avoidance == Avoidance.NONE) {
            avoidanceCooldown = AVOIDANCE_COOLDOWN;
            orientation = dirToTarget;

            ArrayList<Direction> leftOrientations = new ArrayList<>();
            ArrayList<Direction> rightOrientations = new ArrayList<>();
            boolean couldMove = false;
            Direction lastOrientation = null;
            for(int i = 0; i <= 360 / DEG_RESOLUTION + 1; i++) {
                Direction o = orientation.rotateRightDegrees(DEG_RESOLUTION * i);
                boolean canMove = rc.canMove(o);
                if (i == 0) {
                    lastOrientation = o;
                    couldMove = canMove;
                    continue;
                }
                if((canMove && !couldMove) ||
                   (!canMove && couldMove)) {
                    couldMove = canMove;
                    if (canMove) {
                        rightOrientations.add(o);
                    }
                    else {
                        leftOrientations.add(lastOrientation);
                    }
                }
                lastOrientation = o;
            }

            if (leftOrientations.isEmpty() && rightOrientations.isEmpty()) {
                stopNavigation(StopFlag.UNREACHABLE);
                return;
            }

            float minAngle = 361;
            Direction minOrientation = null;
            Avoidance memory = locationInMemory(rc.getLocation());

            if (memory != Avoidance.RIGHT) {
                for (Direction o : leftOrientations) {
                    float angle = Math.abs(o.radiansBetween(orientation));
                    if (angle < minAngle) {
                        minAngle = angle;
                        minOrientation = o;
                        avoidance = Avoidance.RIGHT;
                    }
                }
            }

            if (memory != Avoidance.LEFT) {
                for (Direction o : rightOrientations) {
                    float angle = Math.abs(o.radiansBetween(orientation));
                    if (angle < minAngle) {
                        minAngle = angle;
                        minOrientation = o;
                        avoidance = Avoidance.LEFT;
                    }
                }
            }

            // if (avoidance == Avoidance.NONE) {
            //  TODO: handle this weird case, for example take a random orientation from one of the lists
            // }

            orientation = minOrientation;
            collisionMemory.add(new Tuple<>(rc.getLocation(), avoidance));
            rc.move(orientation);
            rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(orientation, 4), 255, 255, 255);
            return;
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

        if (!rc.canMove(targetLocation)) return false;
        rc.move(targetLocation);
        avoidance = Avoidance.NONE;

        return true;
    }

    private boolean moveAroundObstacle() throws GameActionException {

        if (orientation == null) return false;

        for (int i = 0; i <= 360 / DEG_RESOLUTION; i++) {

            Direction newOrientation;
            if (avoidance == Avoidance.LEFT) {
                newOrientation = orientation.rotateRightDegrees(DEG_RESOLUTION * i - 90);
            } else {
                newOrientation = orientation.rotateLeftDegrees(DEG_RESOLUTION * i - 90);
            }

            if (rc.canMove(newOrientation)) {
                rc.move(newOrientation);
                orientation = newOrientation;
                rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newOrientation, 5.0f), 255, 255, 255);

                return true;
            }

            if(i%4 == 0) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(newOrientation, 2f), 0, 0, 0);
        }

        return false;
    }
}
