package BikiniNinjas;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

public class ScoutPlayer extends AbstractPlayer {

    private Direction direction;
    private HashMap<Integer, MapLocation> fruitfulTrees;

    public ScoutPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        direction = Utilities.randomDirection();;
        fruitfulTrees = new HashMap<>();
    }

    @Override
    protected void step() throws GameActionException {
        updateTreeInfo(rc.senseNearbyTrees(-1, Team.NEUTRAL));
        Tuple<Integer, MapLocation> tree = nearestFruitfulTree();

        if(tree == null) {
            while(!rc.canMove(direction)/*rc.getLocation().add(direction, 2*RobotType.SCOUT.strideRadius))*/) {
                direction = Utilities.randomDirection();
            }
            rc.move(direction);
        }
        else if(rc.canInteractWithTree(tree.item1)) {
            rc.shake(tree.item1);
        }
        else {
            Utilities.tryMove(rc, rc.getLocation().directionTo(tree.item2));
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

    private void moveRandomly() {

    }
}
