package BikiniNinjas;

import battlecode.common.*;

import java.util.*;

public class ScoutPlayer extends AbstractPlayer {

    private Direction direction;
    private HashMap<Integer, MapLocation> fruitfulTrees;

    public ScoutPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        direction = Utilities.randomDirection();
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
            rc.move(new Direction(dx,dy));
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

    private void updateTreeInfo(TreeInfo[] newlySensedTrees) {
        for(TreeInfo tree: newlySensedTrees) {
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
