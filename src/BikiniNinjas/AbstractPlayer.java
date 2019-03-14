package BikiniNinjas;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public abstract class AbstractPlayer {

    protected RobotController rc;
    protected Team enemy;
    protected Broadcast bc;
    protected BuildManager bm;
    protected Navigation navigation;
    protected Random rnd;
    private final int BULLET_RESERVE = 500;
    private int[] bytecodeExecuted = new int[4];

    public AbstractPlayer(RobotController rc) throws GameActionException {
        this.rc = rc;
        this.enemy = rc.getTeam().opponent();
        this.bc = new Broadcast(rc);
        this.bm = new BuildManager(rc);
        this.navigation = new Navigation(rc);
        this.rnd = new Random();
    }

    public void run() throws GameActionException {

        initialize();

        while (true) {
            try {

                bm.update();
                bytecodeExecuted[0] = Clock.getBytecodeNum();

                bc.takeIn(bm.getInactiveRobots());
                bytecodeExecuted[1] = Clock.getBytecodeNum();

                navigation.step();
                bytecodeExecuted[2] = Clock.getBytecodeNum();

                donate();
                step();
                bytecodeExecuted[3] = Clock.getBytecodeNum();

                if(tooMuchBytecode(Clock.getBytecodeNum())) {
                    System.out.println("WARNING: TOO MANY BYTECODES USED!!!");
                    printState();
                }
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Robot Exception");
                e.printStackTrace();
            }
        }


    }

    protected abstract void initialize() throws GameActionException;
    protected abstract void step() throws GameActionException;

    protected boolean tooMuchBytecode(int bytecodeCount) { return bytecodeCount > 10000; }

    protected void printState() {
        System.out.println("BUILD MANAGER EXECUTED: " + bytecodeExecuted[0]);
        System.out.println("BROADCAST EXECUTED: " + bytecodeExecuted[1]);
        System.out.println("NAVIGATION EXECUTED: " + bytecodeExecuted[2]);

        System.out.println("ALL BYTECODE EXECUTED: " + bytecodeExecuted[3]);
    }

    private void donate() throws GameActionException {
        if (rc.getTeamBullets() > BULLET_RESERVE ) {
            float bulletDiff = rc.getTeamBullets() - BULLET_RESERVE;
            int victoryPointCount = (int) (bulletDiff/rc.getVictoryPointCost());
            rc.donate(victoryPointCount * rc.getVictoryPointCost());
        }
    }

    protected Direction directionTowardEnemy() throws GameActionException {
        MapLocation[] archonLocations = rc.getInitialArchonLocations(enemy);
        int mostDistantArchonIndex = Utilities.argMaxDistance(rc.getLocation(), Arrays.asList(archonLocations));

        return rc.getLocation().directionTo(archonLocations[mostDistantArchonIndex]);
    }

    protected ArrayList<RobotInfo> getNearbyGardeners() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        ArrayList<RobotInfo> gardeners = new ArrayList<>();

        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.GARDENER) gardeners.add(robot);
        }

        return gardeners;
    }

}
