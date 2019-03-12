package BikiniNinjas;

import battlecode.common.*;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class LumberjackPlayer extends AbstractPlayer {

    enum Role {
        SPREADING, // just take nearest tree in order to make place for our economy
        REACHING   // try to cut trees in order to make a treeless way to enemy archons
    }

    private final int navigatingPatiency = 300;
    private int navigatingRounds;

    private TreeInfo targetTree;

    private Role role;

    public LumberjackPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        // role = (Math.random() < 0.5f ? Role.SPREADING : Role.REACHING);
        role = Role.SPREADING;
        targetTree = null;
        navigatingRounds = 0;
    }

    @Override
    protected void step() throws GameActionException {

        RobotInfo nearestAttackingEnemy = nearestEnemy(true);
        if (nearestAttackingEnemy != null) {
            rc.setIndicatorDot(nearestAttackingEnemy.location, 0, 255, 255);
            float dist = rc.getLocation().distanceSquaredTo(nearestAttackingEnemy.location);
            if (dist > 9) navigation.navigateToMoving(nearestAttackingEnemy.location);
            else {
                navigation.stopNavigation();
                if (rc.canStrike()) {
                    rc.strike();
                    rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                }
            }
            return;
        }

        if (chopTargetTree()) {
            if (navigation.isNavigating()) navigation.stopNavigation();
            return;
        }

        if (role == Role.REACHING) {
            MapLocation nearestEnemySpot = nearestEnemySpot();
            navigation.navigateToMoving(nearestEnemySpot);
            MapLocation l = rc.getLocation();
            MapLocation treeLocation = l.add(l.directionTo(nearestEnemySpot), 2);
            if (rc.isLocationOccupiedByTree(treeLocation)) {
                if (navigation.isNavigating()) navigation.stopNavigation();
                targetTree = rc.senseTreeAtLocation(treeLocation);
            }
            return;
        }

        if (role == Role.SPREADING) {
            TreeInfo nearestTree = nearestTree();
            if (nearestTree == null) {
                if (navigation.isNavigating() && navigatingRounds < navigatingPatiency) {
                    navigatingRounds++;
                    return;
                }
                navigation.navigateTo(rc.getLocation().add(Utilities.randomDirection(), 10));
                return;
            }
            rc.setIndicatorDot(nearestTree.location, 0, 255, 0);
            if (rc.canInteractWithTree(nearestTree.location)) {
                targetTree = nearestTree;
            } else {
                navigation.navigateTo(nearestTree.location);
            }
        }
    }

    private boolean chopTargetTree() throws GameActionException {
        if (targetTree == null) return false;
        rc.setIndicatorDot(targetTree.location, 0, 0, 255);
        if (rc.canInteractWithTree(targetTree.ID)) {
            if (rc.canChop(targetTree.ID)) rc.chop(targetTree.ID);
            return true;
        } else {
            targetTree = null;
            return false;
        }
    }

    private TreeInfo nearestTree() {

        float minDist = Float.POSITIVE_INFINITY;
        TreeInfo minTree = null;

        for (TreeInfo r : rc.senseNearbyTrees(-1, Team.NEUTRAL)) {
            float distance = r.getLocation().distanceSquaredTo(rc.getLocation());
            if (distance < minDist) {
                minDist = distance;
                minTree = r;
            }
        }

        for (TreeInfo r : rc.senseNearbyTrees(-1, Utilities.opponentTeam(rc))) {
            float distance = r.getLocation().distanceSquaredTo(rc.getLocation());
            if (distance < minDist) {
                minDist = distance;
                minTree = r;
            }
        }

        return minTree;
    }

    private RobotInfo nearestEnemy(boolean attacking) {

        float minDist = Float.POSITIVE_INFINITY;
        RobotInfo minRobot = null;

        for (RobotInfo r : rc.senseNearbyRobots(-1, Utilities.opponentTeam(rc))) {
            if ((attacking && (!r.getType().canAttack() || !rc.canMove(r.location))) ||
                 r.getType() == RobotType.SCOUT &&
                 r.getType() != RobotType.GARDENER) continue;
            float distance = r.getLocation().distanceSquaredTo(rc.getLocation());
            if (distance < minDist) {
                minDist = distance;
                minRobot = r;
            }
        }
        return minRobot;
    }

    private MapLocation nearestEnemySpot() {

        MapLocation[] archons = rc.getInitialArchonLocations(Utilities.opponentTeam(rc));
        RobotInfo nearestEnemy = nearestEnemy(false);

        MapLocation minLocation = (nearestEnemy == null ? archons[0] : nearestEnemy.location);
        float minDist = rc.getLocation().distanceSquaredTo(archons[0]);

        for (MapLocation archon : archons) {
            float d = rc.getLocation().distanceSquaredTo(archon);
            if (d < minDist) {
                minDist = d;
                minLocation = archon;
            }
        }
        return minLocation;
    }
}
