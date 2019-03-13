package BikiniNinjas;

import battlecode.common.*;

import java.util.*;

public class ScoutPlayer extends AbstractPlayer {

    private Direction direction;
    private HashMap<Integer, MapLocation> fruitfulTrees;

    private final int treeDensityUpdateRounds = 5;
    private boolean isScared;
    private final int MAX_CYCLES = 30;

    public ScoutPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {
        direction = directionTowardEnemy();
        fruitfulTrees = new HashMap<>();
    }

    private int[] bytecodes = new int[5];

    @Override
    protected void step() throws GameActionException {

        List<MapLocation> dangerousLocations = getDangerousLocations();


        isScared = !dangerousLocations.isEmpty();
        if(isScared) {
            moveAwayFromEnemies(dangerousLocations);
        }

        bytecodes[0] = Clock.getBytecodeNum();

        updateTreeInfo(rc.senseNearbyTrees(-1, Team.NEUTRAL));

        bytecodes[1] = Clock.getBytecodeNum();

        Tuple<Integer, MapLocation> tree = nearestFruitfulTree();

        bytecodes[2] = Clock.getBytecodeNum();

        if(tree == null) {
            if(!rc.hasMoved()) direction = Utilities.moveRandomly(rc, direction);
        }
        else if(rc.canInteractWithTree(tree.item1)) {
            rc.shake(tree.item1);
        }
        else {
            if(!rc.hasMoved()) Utilities.tryMove(rc, rc.getLocation().directionTo(tree.item2));
        }
    }

    private void updateTreeInfo(TreeInfo[] newlySensedTrees) throws GameActionException {

        double treeCoverage = 0.0;
        for(int i = 0; i < Math.min(MAX_CYCLES, newlySensedTrees.length); i++) {
            TreeInfo tree = newlySensedTrees[i];

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

            if(tree.getID() == newlySensedTrees[0].getID()) {
                bytecodes[3] = Clock.getBytecodeNum();
            }
        }

        if(newlySensedTrees.length > MAX_CYCLES) {
            treeCoverage = treeCoverage / MAX_CYCLES * newlySensedTrees.length;
        }

        bytecodes[4] = Clock.getBytecodeNum();

        if (rc.getRoundNum() % treeDensityUpdateRounds == 0) {
            float localTreeDensity = (float) treeCoverage / (RobotType.SCOUT.sensorRadius * RobotType.SCOUT.sensorRadius);
            bc.addTreeDensitySample(localTreeDensity);
        }
    }

    private List<MapLocation> getDangerousLocations() {
        RobotInfo[] robots = rc.senseNearbyRobots(7.0f, enemy);
        ArrayList<MapLocation> dangLocs = new ArrayList<>();
        for (RobotInfo ri : robots) {
            if (ri.getType() == RobotType.SOLDIER || ri.getType() == RobotType.TANK) {
                dangLocs.add(ri.location);
            }
        }

        return dangLocs;
    }

    private void moveAwayFromEnemies(List<MapLocation> enemyLocations) throws GameActionException {
        MapLocation away = new MapLocation(0.0f, 0.0f);
        for (MapLocation l : enemyLocations) {
            away = away.add(rc.getLocation().directionTo(l), 1 / rc.getLocation().distanceTo(l));

        }

        direction = new Direction(-away.x, -away.y);
        Utilities.tryMove(rc, direction);
    }

    private Tuple<Integer, MapLocation> nearestFruitfulTree() {
        float minDistance = Float.POSITIVE_INFINITY;
        int closestTreeId = -1;

        int i = 0;
        for (Map.Entry<Integer, MapLocation> tree : fruitfulTrees.entrySet()) {
            int id = tree.getKey();
            MapLocation location = tree.getValue();

            float distance = location.distanceSquaredTo(rc.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                closestTreeId = id;
            }

            if(i++ > MAX_CYCLES) break;
        }

        if(closestTreeId == -1) return null;
        return new Tuple<>(closestTreeId, fruitfulTrees.get(closestTreeId));
    }

    @Override
    protected void printState() {
        super.printState();
        System.out.println("IS SCARED: " + isScared);
        System.out.println(bytecodes[0] + ", " + bytecodes[1] + ", " + bytecodes[2] + ", " + bytecodes[3] + ", " + bytecodes[4]);
    }
}
