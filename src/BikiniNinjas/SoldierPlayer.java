package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;;
import java.util.List;

public class SoldierPlayer extends AbstractPlayer {
    MapLocation[] archonLocs;
    int archonIndex;
    int searchTimeRemaining;
    State state;
    private final int BATCH_SIZE = 5;
    boolean movingToFirstArchon;
    int activationSoldierCount;

    enum State {
        REGROUP,
        SEEK_AND_DESTROY,
        WAITING_FOR_BATCH
    }

    public SoldierPlayer(RobotController rc) throws GameActionException {
        super(rc);

    }

    private boolean isCircleOccupiedByTrees(MapLocation location, float radius) {
        TreeInfo[] ourTrees = rc.senseNearbyTrees(location, radius, rc.getTeam());
        if (ourTrees.length != 0) return true;
        TreeInfo[] neutralTrees = rc.senseNearbyTrees(location, radius, Team.NEUTRAL);
        return neutralTrees.length != 0;
    }

    @Override
    protected void initialize() throws GameActionException {

        archonLocs = rc.getInitialArchonLocations(enemy);
        state = State.WAITING_FOR_BATCH;
        searchTimeRemaining = 0;
        movingToFirstArchon = false;
        int soldierCount = bc.registerSoldier();
        activationSoldierCount = soldierCount + BATCH_SIZE;
        activationSoldierCount = activationSoldierCount - (activationSoldierCount % BATCH_SIZE);
    }

    @Override
    protected void step() throws GameActionException {

        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
        ArrayList<RobotInfo> gardeners = new ArrayList<>();
        for (RobotInfo r : robots) {
            if (r.type.equals(RobotType.GARDENER)) {
                gardeners.add(r);
            }
        }
        RobotInfo robotToShoot = null;
        if (robots.length > 0) {
            robotToShoot = robots[0];
            if (gardeners.size() > 0) {
                robotToShoot = gardeners.get(0);
            }
            if (rc.canFireSingleShot()) {
                rc.fireSingleShot(rc.getLocation().directionTo(robotToShoot.location));
            }
        }

        if (state == State.WAITING_FOR_BATCH) {
            if (activationSoldierCount <= bc.getSoldierCount()) {
                moveToInitArchon();
            } else {
                if (robotToShoot != null) combat(robotToShoot);
                else if (!navigation.isNavigating() && isCircleOccupiedByTrees(rc.getLocation(), 3)) {
                    // Utilities.moveRandomly(rc,new Direction(rnd.nextFloat()*2*(float) Math.PI));

                    for (int c = 0; c < 50; c++) {
                        Direction dir = Utilities.randomDirection();
                        float newCircleRadius = 2;
                        MapLocation newLocation = rc.getLocation().add(dir, (7.0f - newCircleRadius) * (float) Math.random());

                        if (rc.canSenseAllOfCircle(newLocation, newCircleRadius) &&
                                rc.onTheMap(newLocation, newCircleRadius) &&
                                !isCircleOccupiedByTrees(newLocation, newCircleRadius)) {
                            navigation.navigateTo(newLocation);
                            break;
                        }
                    }
                }
            }
        }

        //       MapLocation myLocation = rc.getLocation();

//        double thresholdAngle = 15;
//        BulletInfo[] bullets = rc.senseNearbyBullets();
//        List<BulletInfo> list = new ArrayList<>();
//        for (BulletInfo b : bullets) {
//            if (Math.abs(b.dir.degreesBetween(b.location.directionTo(myLocation))) <= thresholdAngle) {
//                list.add(b);
//            }
//        }
//        List<BulletInfo> dangerousBullets = list;
//        int bulletCountThreshold = 15;
//        if (bulletCountThreshold <= dangerousBullets.size() && rc.canFirePentadShot()) {
//            Float acc = 0f;
//            for (BulletInfo b : dangerousBullets) {
//                Direction d = myLocation.directionTo(b.location);
//                Float radians = d.radians;
//                acc = acc + radians;
//            }
//            float shootDirRads = acc / dangerousBullets.size();
//            rc.firePentadShot(new Direction(shootDirRads));
//        }

        switch (state) {
            case REGROUP:
                if (rc.getLocation().distanceTo(archonLocs[archonIndex]) < 15) {
                    searchTimeRemaining = 100;
                    seekAndDestroy();
                }
                if (robotToShoot != null) {
                    rc.setIndicatorDot(robotToShoot.location, 255, 0, 0);
                    navigation.stopNavigation();
                    combat(robotToShoot);
                } else if
                (!navigation.isNavigating()) {
                    navigation.navigateTo(archonLocs[archonIndex]);
                }

                break;
            case SEEK_AND_DESTROY:
                searchTimeRemaining--;
                if (searchTimeRemaining == 0) {
                    if (robotToShoot == null) {
                        moveToNextArchon();
                    } else {
                        combat(robotToShoot);
                    }

                } else {

                    if (robotToShoot != null) {
                        navigation.stopNavigation();
                        combat(robotToShoot);
                    } else {
                        if (!navigation.isNavigating())
                            seekAndDestroy();
                    }

                }

                break;
        }
        //TODO komunikace? (jeden soldier zaveli, jdeme na dalsiho archona, ostatni nasleduji...)
        //TODO attack/defense mod?
        //TODO Archon request defense?
    }

    private void seekAndDestroy() throws GameActionException {

        state = State.SEEK_AND_DESTROY;
        double searchRadius = 40D;
        MapLocation archonLoc = archonLocs[archonIndex];
        float angle = (float) (rnd.nextDouble() * Math.PI * 2);
        float dist = (float) (rnd.nextDouble() * searchRadius);
        MapLocation loc = archonLoc.add(angle, dist);
        navigation.navigateTo(loc);
    }

    private void combat(RobotInfo robotToShoot) throws GameActionException {
        int coneAngle = 100;
        int deviation = rnd.nextInt(2 * coneAngle + 1) - coneAngle;
        Direction dirToMoveGeneral = rc.getLocation().directionTo(robotToShoot.location);
        if (rc.getLocation().distanceTo(robotToShoot.location) < 4.5f) {
            dirToMoveGeneral = dirToMoveGeneral.opposite();
        }
        Direction direction = dirToMoveGeneral.rotateLeftDegrees((float) deviation);
        if (rc.canMove(direction) && !rc.hasMoved()) rc.move(direction);
    }

    private void moveToNextArchon() throws GameActionException {
        state = State.REGROUP;
        int currentTarget = bc.getCurretArchonIndex();
        if (currentTarget == archonIndex) {
            archonIndex = (archonIndex + 1) % archonLocs.length;
            bc.setCurrentArchonIndex(archonIndex);
        } else {
            archonIndex = currentTarget;
        }
        navigation.navigateTo(archonLocs[archonIndex]);

    }

    private void moveToInitArchon() throws GameActionException {
        state = State.REGROUP;
        int index = bc.getCurretArchonIndex();
        archonIndex = index;
        navigation.navigateTo(archonLocs[archonIndex]);
    }

}
