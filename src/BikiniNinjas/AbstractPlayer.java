package BikiniNinjas;

import battlecode.common.*;

public abstract class AbstractPlayer {

    protected RobotController rc;
    protected Team enemy;
    protected Broadcast bc;
    protected BuildManager bm;
    protected Navigation navigation;

    private int[] bytecodeExecuted = new int[4];

    public AbstractPlayer(RobotController rc) throws GameActionException {
        this.rc = rc;
        this.enemy = rc.getTeam().opponent();
        this.bc = new Broadcast(rc);
        this.bm = new BuildManager(rc);
        this.navigation = new Navigation(rc);
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

                step();
                bytecodeExecuted[3] = Clock.getBytecodeNum();

                if(Clock.getBytecodeNum() > 10000) {
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

    protected void printState() {
        System.out.println("BUILD MANAGER EXECUTED: " + bytecodeExecuted[0]);
        System.out.println("BROADCAST EXECUTED: " + bytecodeExecuted[1]);
        System.out.println("NAVIGATION EXECUTED: " + bytecodeExecuted[2]);

        System.out.println("ALL BYTECODE EXECUTED: " + bytecodeExecuted[3]);
    }
}
