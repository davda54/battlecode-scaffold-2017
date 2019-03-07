package BikiniNinjas;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ScoutPlayer extends AbstractPlayer {

    private Direction direction;
    private HashMap<Integer, MapLocation> fruitfulTrees;

    public ScoutPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        direction = Utilities.randomDirection();;
        fruitfulTrees = new HashMap<>();
    }

    @Override
    protected void step() throws GameActionException {
        boolean hasMoved = false;
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] robots = rc.senseNearbyRobots();
        MapLocation[] dangLocs = (MapLocation[]) Arrays.stream(robots).filter(ri->ri.getType().canAttack()).map(ri->ri.location).toArray();
        if(dangLocs.length > 2) {
            float dx = Arrays.stream(dangLocs).map(l -> myLoc.x - l.x).reduce(0f, (f1, f2) -> f1 + f2);
            float dy = Arrays.stream(dangLocs).map(l -> myLoc.y - l.y).reduce(0f, (f1, f2) -> f1 + f2);
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
