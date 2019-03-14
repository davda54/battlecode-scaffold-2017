package BikiniNinjas;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class TankPlayer extends AbstractPlayer {

    public TankPlayer(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void initialize() throws GameActionException {

    }

    @Override
    protected void step() throws GameActionException {
        if(!navigation.isNavigating()) {
            navigation.navigateTo(rc.getInitialArchonLocations(enemy)[0]);
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemy);
        if(enemies.length > 0) {
            rc.fireSingleShot(rc.getLocation().directionTo(enemies[0].getLocation()));
        }
    }
}
