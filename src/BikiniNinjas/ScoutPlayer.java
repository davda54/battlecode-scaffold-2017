package BikiniNinjas;

import battlecode.common.*;

import javax.rmi.CORBA.Util;
import java.util.*;

public class ScoutPlayer extends AbstractPlayer {

    private Direction direction;
    private HashMap<Integer, MapLocation> fruitfulTrees;

    private final int treeDensityUpdateRounds = 5;

    public ScoutPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        direction = directionTowardEnemy();
        fruitfulTrees = new HashMap<>();
    }

    @Override
    protected void step() throws GameActionException {
        boolean hasMoved = false;
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] robots = rc.senseNearbyRobots();
        ArrayList<MapLocation> dangLocs = new ArrayList<>();
        for (RobotInfo ri : robots) {
            if (ri.getType().canAttack()) {
                MapLocation location = ri.location;
                dangLocs.add(location);
            }
        }

        if(dangLocs.size() > 2) {
            float acc = 0f;
            for (MapLocation l : dangLocs) {
                float aFloat = myLoc.x - l.x;
                acc = acc + aFloat;
            }
            float dx = acc;
            float result = 0f;
            for (MapLocation l : dangLocs) {
                float aFloat = myLoc.y - l.y;
                result = result + aFloat;
            }
            float dy = result;
            Utilities.tryMove(rc, new Direction(dx,dy));
            hasMoved = true;
        }


        updateTreeInfo(rc.senseNearbyTrees(-1, Team.NEUTRAL));
        Tuple<Integer, MapLocation> tree = nearestFruitfulTree();

        if(tree == null) {
            if (!hasMoved)direction = Utilities.moveRandomly(rc, direction);
        }
        else if(rc.canInteractWithTree(tree.item1)) {
            rc.shake(tree.item1);
        }
        else {
            if(!hasMoved) Utilities.tryMove(rc, rc.getLocation().directionTo(tree.item2));
        }
    }

    private void updateTreeInfo(TreeInfo[] newlySensedTrees) throws GameActionException {

        double treeCoverage = 0.0;
        for(TreeInfo tree: newlySensedTrees) {
            treeCoverage += tree.radius * tree.radius;
            if(tree.getContainedBullets() == 0) {
                if(fruitfulTrees.containsKey(tree.ID)) {
                    fruitfulTrees.remove(tree.ID);
                }
                continue;
            }
            if(!fruitfulTrees.containsKey(tree.ID)) {
                fruitfulTrees.put(tree.ID, tree.location);
            }
        }

        if (rc.getRoundNum() % treeDensityUpdateRounds == 0) {
            float localTreeDensity = (float) treeCoverage / (RobotType.SCOUT.sensorRadius * RobotType.SCOUT.sensorRadius);
            bc.addTreeDensitySample(localTreeDensity);
        }
    }

    private Tuple<Integer, MapLocation> nearestFruitfulTree() {
        float minDistance = Float.POSITIVE_INFINITY;
        int closestTreeId = -1;

        for (Map.Entry<Integer, MapLocation> tree : fruitfulTrees.entrySet()) {
            int id = tree.getKey();
            MapLocation location = tree.getValue();

            float distance = location.distanceSquaredTo(rc.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                closestTreeId = id;
            }
        }

        if(closestTreeId == -1) return null;
        return new Tuple<>(closestTreeId, fruitfulTrees.get(closestTreeId));
    }
}
