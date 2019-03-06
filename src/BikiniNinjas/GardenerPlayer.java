package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GardenerPlayer extends AbstractPlayer {

    private enum State {
        BUILDING_FIRST_SCOUT,
        FINDING_SPOT,
        PLANTING_TREES
    }

    private static final int START_PATIENCE = 200;

    private State state;
    private ArrayList<Direction> treeDirections;
    private ArrayList<Tuple<Integer, Integer>> treesToBeBorn;
    private ArrayList<Integer> plantedTreeIds;

    private Direction moveDirection;
    private int patience;
    private MapLocation favouriteGardenerLocation;
    private boolean orbitClockwise;

    public GardenerPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {

        state = State.BUILDING_FIRST_SCOUT;
        moveDirection = Utilities.randomDirection();
        patience = START_PATIENCE;
        favouriteGardenerLocation = null;
        orbitClockwise = Math.random() > 0.5f;

        treesToBeBorn = new ArrayList<>();
        treeDirections = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            treeDirections.add(Direction.NORTH.rotateLeftDegrees(i*60));
        }

        plantedTreeIds = new ArrayList<>();
    }

    @Override
    protected void step() throws GameActionException {

        switch (state) {
            case BUILDING_FIRST_SCOUT:
                buildFirstScout();
            case FINDING_SPOT:
                findSpot();
                break;
            case PLANTING_TREES:
                plantTrees();
                break;
        }

        growTrees();
        waterTrees();
    }

    private void buildFirstScout() throws GameActionException {
        //TODO: check if scout has not been built yet

        Direction direction = Utilities.randomDirection();
        for(int i = 0; i < 36; i++) {
            if(rc.canBuildRobot(RobotType.SCOUT, direction)) {
                rc.buildRobot(RobotType.SCOUT, direction);
                break;
            }
            direction = direction.rotateLeftDegrees(10);
        }

        state = State.FINDING_SPOT;
    }

    private void findSpot() throws GameActionException {
        if(patience-- < 0) {
            searchRandomly();
            return;
        }

        if(favouriteGardenerLocation != null && rc.canSenseLocation(favouriteGardenerLocation) && rc.senseRobotAtLocation(favouriteGardenerLocation) != null) {
            orbitFavouriteGardener();
            return;
        }

        ArrayList<RobotInfo> nearbyGardeners = getNearbyGardeners();
        if(nearbyGardeners.size() > 0) {
            favouriteGardenerLocation = nearbyGardeners.get(Utilities.randInt(nearbyGardeners.size())).location;
            orbitFavouriteGardener();
            return;
        }

        searchRandomly();
    }

    private void plantTrees() throws GameActionException {
        for(Direction direction: treeDirections) {
            if(rc.canPlantTree(direction)) {
                rc.plantTree(direction);
                int plantedTreeId = rc.senseNearbyTrees(rc.getLocation().add(direction, 2.0f), 0.5f, rc.getTeam())[0].ID;
                treesToBeBorn.add(new Tuple<>(plantedTreeId, 80));

                return;
            }
        }
    }

    private void waterTrees() throws GameActionException {
        float minHealth = Float.POSITIVE_INFINITY;
        int dryTreeId = -1;

        for(int i = 0; i < plantedTreeIds.size(); i++) {
            int treeId = plantedTreeIds.get(i);

            if(!rc.canSenseTree(treeId)) {
                plantedTreeIds.remove(i);
                i--; continue;
            }

            if(!rc.canWater(treeId)) {
                continue;
            }

            float health = rc.senseTree(treeId).health;
            if(health < minHealth) {
                minHealth = health;
                dryTreeId = treeId;
            }

        }

        if(dryTreeId != -1) {
            rc.water(dryTreeId);
        }
    }

    private int possibleTreesCount() throws GameActionException {
        int counter = 0;

        for(Direction direction: treeDirections) {
            if(rc.canPlantTree(direction)) {
                counter++;
            }
        }

        return counter;
    }

    private void searchRandomly() throws GameActionException {
        if(possibleTreesCount() >= 4 - patience / START_PATIENCE) {
            state = State.PLANTING_TREES;
            step();
            return;
        }

        moveDirection = Utilities.moveRandomly(rc, moveDirection);
    }

    private void orbitFavouriteGardener() throws GameActionException {
        Direction toFavouriteGardener = rc.getLocation().directionTo(favouriteGardenerLocation);
        float distance = favouriteGardenerLocation.distanceTo(rc.getLocation());

        if(distance > 4.9 && distance < 5.1) {
            if(possibleTreesCount() >= 4 - patience / START_PATIENCE) {
                state = State.PLANTING_TREES;
                step();
                return;
            }

            Direction perpendicular = orbitClockwise
                    ? toFavouriteGardener.rotateLeftDegrees(90)
                    : toFavouriteGardener.rotateRightDegrees(90);

            Utilities.tryMove(rc, perpendicular);
            return;
        }

        if(rc.canMove(toFavouriteGardener, distance - 5.0f)) {
            rc.move(toFavouriteGardener, distance - 5.0f);
            return;
        }

        searchRandomly();
    }

    private ArrayList<RobotInfo> getNearbyGardeners() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        ArrayList<RobotInfo> gardeners = new ArrayList<>();

        for(RobotInfo robot: robots) {
            if(robot.getType() == RobotType.GARDENER) gardeners.add(robot);
        }

        return gardeners;
    }

     private void growTrees() {
        for(int i = 0; i < treesToBeBorn.size(); i++) {
            Tuple<Integer, Integer> idFrameTuple = treesToBeBorn.get(i);
            idFrameTuple.item2--;

            if(idFrameTuple.item2 <= 0) {
                plantedTreeIds.add(idFrameTuple.item1);
                treesToBeBorn.remove(i);
                i--; continue;
            }
        }
     }
}
