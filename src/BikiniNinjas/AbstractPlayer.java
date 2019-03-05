package BikiniNinjas;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;

public abstract class AbstractPlayer {

    protected RobotController rc;
    protected Team enemy;
    protected Broadcast bc;

    public AbstractPlayer(RobotController rc) {
        this.rc = rc;
        this.enemy = rc.getTeam().opponent();
        this.bc = new Broadcast(rc);
    }

    public void run() throws GameActionException {

        initialize();

        while (true) {
            try {
                step();
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Robot Exception");
                e.printStackTrace();
            }
        }
    }

    protected abstract void initialize() throws GameActionException;
    protected abstract void step() throws GameActionException;
}
