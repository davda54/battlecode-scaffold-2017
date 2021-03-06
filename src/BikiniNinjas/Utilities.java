package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utilities {

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param rc Robot
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        return tryMove(rc, dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param rc Robot
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param rc Robot
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(RobotController rc, BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    static Direction moveRandomly(RobotController rc, Direction initialDirection) throws GameActionException {
        Direction direction = initialDirection;
        float distance = rc.getType().strideRadius;

        for(int c = 0; c < 15; c++) {
            if (rc.canMove(direction, distance)) break;
            if (Math.random() > 0.5f) direction = direction.rotateLeftDegrees(45 + (float)Math.random() * 90);
            else direction = direction.rotateRightDegrees(45 + (float)Math.random() * 90);

            if(rc.canMove(direction, distance)) break;

            direction = direction.rotateRightDegrees(180);
            if(rc.canMove(direction, distance)) break;

            direction = randomDirection();
            distance *= 0.9;
        }

        if(rc.canMove(direction, distance)) rc.move(direction, distance);
        return direction;
    }

    private static Random rand = new Random();
    public static int randInt(int min, int max) {
        return rand.nextInt((max - min)) + min;
    }

    public static int randInt(int max) {
        return rand.nextInt(max);
    }

    public static int argMaxDistance(MapLocation from, List<MapLocation> to) {
        int maxIndex = -1;
        float maxDistance = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < to.size(); i++) {
            if(to.get(i) == null) continue;

            float distance = to.get(i).distanceSquaredTo(from);
            if(distance > maxDistance) {
                maxDistance = distance;
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    public static int argMinDistance(MapLocation from, List<MapLocation> to) {
        int minIndex = -1;
        float minDistance = Float.POSITIVE_INFINITY;

        for (int i = 0; i < to.size(); i++) {
            if(to.get(i) == null) continue;

            float distance = to.get(i).distanceSquaredTo(from);
            if(distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public static ArrayList<MapLocation> selectLocationsCloserThan(MapLocation center, float distance, ArrayList<MapLocation> locations) {
        ArrayList<MapLocation> output = new ArrayList<>();
        float distanceSquared = distance*distance;

        for(MapLocation location: locations) {
            if(location.distanceSquaredTo(center) <= distanceSquared) {
                output.add(location);
            }
        }

        return output;
    }

    public static Team opponentTeam(RobotController rc) {
        return rc.getTeam() == Team.A ? Team.B : Team.A;
    }

    public static boolean isSomeoneStandingInLocation(RobotController rc, MapLocation location, float radius) {
        return rc.senseNearbyRobots(location, radius, rc.getTeam()).length > 0;
    }

    public static MapLocation scale(MapLocation v, float scale) {
        return new MapLocation(v.x * scale, v.y * scale);
    }

    public static MapLocation add(MapLocation v1, MapLocation v2) {
        return new MapLocation(v1.x + v2.x, v1.y + v2.y);
    }
}
