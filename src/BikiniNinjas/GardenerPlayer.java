package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GardenerPlayer extends AbstractPlayer {

    private enum State {
        BUILDING_FIRST_SCOUT,
        FINDING_SPOT,
        PLANTING_TREES
    }

    private static final int START_PATIENCY = 200;

    private State state;
    private ArrayList<Direction> futureTrees;
    private ArrayList<Integer> plantedTreeIds;
    private int wateredTreeIndex;
    private Direction moveDirection;
    private int patiency;
    private MapLocation favouriteGardenerLocation;
    private boolean orbitClockwise;

    public GardenerPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {

        state = State.BUILDING_FIRST_SCOUT;
        wateredTreeIndex = 0;
        moveDirection = Utilities.randomDirection();
        patiency = START_PATIENCY;
        favouriteGardenerLocation = null;
        orbitClockwise = Math.random() > 0.5f;

        futureTrees = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            futureTrees.add(Direction.NORTH.rotateLeftDegrees(i*60));
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
        if(patiency-- < 0) {
            searchRandomly();
            return;
        }

        if(favouriteGardenerLocation != null && rc.senseRobotAtLocation(favouriteGardenerLocation) != null) {
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
        for(Direction direction: futureTrees) {
            if(rc.canPlantTree(direction)) {
                rc.plantTree(direction);
                plantedTreeIds.add(rc.senseNearbyTrees(rc.getLocation().add(direction, 1.1f), 0.1f, rc.getTeam())[0].ID);

                return;
            }
        }
    }

    private void waterTrees() throws GameActionException {
        if(plantedTreeIds.size() == 0) return;

        if(!rc.canWater(plantedTreeIds.get(wateredTreeIndex))) {
            plantedTreeIds.remove(wateredTreeIndex);
            wateredTreeIndex = wateredTreeIndex % plantedTreeIds.size();
            waterTrees();
        }

        rc.water(wateredTreeIndex);
        wateredTreeIndex = (wateredTreeIndex + 1) % plantedTreeIds.size();
    }

    private int possibleTreesCount() throws GameActionException {
        int counter = 0;

        for(Direction direction: futureTrees) {
            if(rc.canPlantTree(direction)) {
                counter++;
            }
        }

        return counter;
    }

    private void searchRandomly() throws GameActionException {
        if(possibleTreesCount() >= 4 - patiency / START_PATIENCY) {
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
            if(possibleTreesCount() >= 4 - patiency / START_PATIENCY) {
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

    private ArrayList<RobotInfo> getNearbyGardeners() {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        ArrayList<RobotInfo> gardeners = new ArrayList<>();

        for(RobotInfo robot: robots) {
            if(robot.getType() == RobotType.GARDENER) gardeners.add(robot);
        }

        return gardeners;
    }
}
