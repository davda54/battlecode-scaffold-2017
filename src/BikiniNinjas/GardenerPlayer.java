package BikiniNinjas;

import battlecode.common.*;

import java.awt.*;
import java.util.ArrayList;

public class GardenerPlayer extends AbstractPlayer {

    private enum State {
        BUILDING_FIRST_SCOUT,
        FINDING_SPOT,
        PLANTING_TREES
    }

    private static final int MAX_PATIENCE = 200;

    private State state;
    private ArrayList<Direction> treeDirections;
    private ArrayList<Tuple<Integer, Integer>> treesToBeBorn;
    private ArrayList<Integer> plantedTreeIds;

    private Direction moveDirection;
    private int patience;
    private MapLocation favouriteOrchardLocation;
    private boolean orbitClockwise;
    private boolean haveNotified;

    public GardenerPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {

        state = State.BUILDING_FIRST_SCOUT;
        moveDirection = Utilities.randomDirection();
        patience = 0;
        favouriteOrchardLocation = null;
        orbitClockwise = Math.random() > 0.5f;
        haveNotified = false;

        treesToBeBorn = new ArrayList<>();
        treeDirections = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            treeDirections.add(Direction.NORTH.rotateLeftDegrees(i * 60));
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
                growTrees();
                waterTrees();
                if(!haveNotified) notifyPotentialLocations();
                break;
        }
    }

    private void buildFirstScout() throws GameActionException {
        if (bc.getCountOf(RobotType.SCOUT) == 0) {
            Direction direction = Utilities.randomDirection();
            for (int i = 0; i < 36; i++) {
                if (rc.canBuildRobot(RobotType.SCOUT, direction)) {
                    bm.build(RobotType.SCOUT, direction);
                    break;
                }
                direction = direction.rotateLeftDegrees(10);
            }
        }

        state = State.FINDING_SPOT;
    }

    private void findSpot() throws GameActionException {
        if (patience++ > MAX_PATIENCE) {
            if (navigation.isNavigating()) {
                navigation.stopNavigation();
                return;
            }
            searchRandomly();
            rc.setIndicatorDot(rc.getLocation(), 255, 255, 0);
            return;
        }

        if (favouriteOrchardLocation != null) {
            goToFavouriteOrchard();
            rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
            return;
        }

        favouriteOrchardLocation = selectFavouriteOrchardLocation();
        if (favouriteOrchardLocation != null) {
            goToFavouriteOrchard();
            rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
            return;
        }

        searchRandomly();
        rc.setIndicatorDot(rc.getLocation(), 0, 0, 255);
    }

    private void plantTrees() throws GameActionException {
        for (Direction direction : treeDirections) {
            if (rc.canPlantTree(direction)) {
                rc.plantTree(direction);
                int plantedTreeId = rc.senseNearbyTrees(rc.getLocation().add(direction, 2.0f), 0.1f, rc.getTeam())[0].ID;
                treesToBeBorn.add(new Tuple<>(plantedTreeId, 80));

                return;
            }
        }
    }

    private void waterTrees() throws GameActionException {
        float minHealth = Float.POSITIVE_INFINITY;
        int dryTreeId = -1;

        for (int i = 0; i < plantedTreeIds.size(); i++) {
            int treeId = plantedTreeIds.get(i);

            if (!rc.canSenseTree(treeId)) {
                plantedTreeIds.remove(i);
                i--;
                continue;
            }

            if (!rc.canWater(treeId)) {
                continue;
            }

            TreeInfo tree = rc.senseTree(treeId);
            if (tree.health < minHealth) {
                minHealth = tree.health;
                dryTreeId = treeId;
            }

            rc.setIndicatorDot(tree.location, 0, 255, 0);
        }

        if (dryTreeId != -1) {
            rc.water(dryTreeId);
        }
    }

    private int possibleTreesCount(MapLocation center) throws GameActionException {
        int counter = 0;

        for (Direction direction : treeDirections) {
            MapLocation treeLocation = center.add(direction, 2.0f);

            if (rc.canSenseAllOfCircle(treeLocation, 1.0f) && !rc.isCircleOccupiedExceptByThisRobot(treeLocation, 1.0f)) {
                rc.setIndicatorDot(treeLocation, 255, 0, 255);
                counter++;
            }
        }

        return counter;
    }

    private void searchRandomly() throws GameActionException {
        if (possibleTreesCount(rc.getLocation()) >= 5 - patience / MAX_PATIENCE) {
            state = State.PLANTING_TREES;
            step();
            return;
        }

        moveDirection = Utilities.moveRandomly(rc, moveDirection);
    }

    private MapLocation selectFavouriteOrchardLocation() throws GameActionException {
        ArrayList<MapLocation> orchardLocations = bc.getOrchardLocations();
        ArrayList<RobotInfo> gardeners = getNearbyGardeners();

        for(MapLocation l: orchardLocations) {
            rc.setIndicatorDot(l, 255, 255, 255);
        }

        int orchardId = gardeners.size() > 2
                ? Utilities.argMaxDistance(rc.getLocation(), orchardLocations)
                : Utilities.argMinDistance(rc.getLocation(), orchardLocations);

        if(orchardId == -1) return null;

        bc.removeOrchardLocation(orchardId);
        return orchardLocations.get(orchardId);
    }

    private void goToFavouriteOrchard() throws GameActionException {
        // TODO: navigation
        rc.setIndicatorLine(rc.getLocation(), favouriteOrchardLocation, 0, 255, 0);

        if (navigation.isNavigating()) return;

        if (rc.getLocation().distanceSquaredTo(favouriteOrchardLocation) > 0.1) {
            navigation.navigateTo(favouriteOrchardLocation);
            return;
        }

        if (possibleTreesCount(rc.getLocation()) >= 5) {
            state = State.PLANTING_TREES;
        } else {
            favouriteOrchardLocation = null;
        }

        step();
    }

    private ArrayList<RobotInfo> getNearbyGardeners() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        ArrayList<RobotInfo> gardeners = new ArrayList<>();

        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.GARDENER) gardeners.add(robot);
        }

        return gardeners;
    }

    private void growTrees() {
        for (int i = 0; i < treesToBeBorn.size(); i++) {
            Tuple<Integer, Integer> idFrameTuple = treesToBeBorn.get(i);
            idFrameTuple.item2--;

            if (idFrameTuple.item2 <= 0) {
                plantedTreeIds.add(idFrameTuple.item1);
                treesToBeBorn.remove(i);
                i--;
                continue;
            }
        }
    }

    private void notifyPotentialLocations() throws GameActionException {
        if(plantedTreeIds.size() < 5) return;
        haveNotified = true;

        ArrayList<MapLocation> potentialLocations = new ArrayList<>();

        for (Direction direction : treeDirections) {
            direction = direction.rotateRightDegrees(30);
            MapLocation center = rc.getLocation().add(direction, 5.5f);

            if (possibleTreesCount(center) >= 4) {
                potentialLocations.add(center);
                rc.setIndicatorDot(center, 255, 255, 255);
            }
        }

        bc.addOrchardLocations(potentialLocations);
    }
}
