package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;;
import java.util.List;

public class SoldierPlayer extends AbstractPlayer {
    MapLocation[] archonLocs;
    int archonIndex;
    int searchTimeRemaining;
    State state;

    boolean movingToFirstArchon;

    enum State {
        REGROUP,
        SEEK_AND_DESTROY
    }

    public SoldierPlayer(RobotController rc) throws GameActionException {
        super(rc);

    }

    @Override
    protected void initialize() throws GameActionException {

        archonLocs = rc.getInitialArchonLocations(enemy);
        state = State.REGROUP;
        searchTimeRemaining = 0;
        movingToFirstArchon = false;
    }

    @Override
    protected void step() throws GameActionException {

        if (!movingToFirstArchon) {
            moveToInitArchon();
            movingToFirstArchon = true;
        }

        MapLocation myLocation = rc.getLocation();

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
        double thresholdAngle = 15;
        BulletInfo[] bullets = rc.senseNearbyBullets();
        List<BulletInfo> list = new ArrayList<>();
        for (BulletInfo b : bullets) {
            if (Math.abs(b.dir.degreesBetween(b.location.directionTo(myLocation))) <= thresholdAngle) {
                list.add(b);
            }
        }
        List<BulletInfo> dangerousBullets = list;
        int bulletCountThreshold = 15;
        if (bulletCountThreshold <= dangerousBullets.size() && rc.canFirePentadShot()) {
            Float acc = 0f;
            for (BulletInfo b : dangerousBullets) {
                Direction d = myLocation.directionTo(b.location);
                Float radians = d.radians;
                acc = acc + radians;
            }
            float shootDirRads = acc / dangerousBullets.size();
            rc.firePentadShot(new Direction(shootDirRads));
        }

        switch (state) {
            case REGROUP:
                if (rc.getLocation().distanceTo(archonLocs[archonIndex]) < 15) {
                    searchTimeRemaining = 300;
                    seekAndDestroy();
                }
                if (robotToShoot != null) {
                    rc.setIndicatorDot(robotToShoot.location,255,0,0);
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
        if(rc.getLocation().distanceTo(robotToShoot.location)<4.5f){
           dirToMoveGeneral=  dirToMoveGeneral.opposite();
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
        int index =bc.getCurretArchonIndex();
        archonIndex = index;
        navigation.navigateTo(archonLocs[archonIndex]);
     }

}
