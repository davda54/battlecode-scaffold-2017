package BikiniNinjas;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Broadcast {

    public RobotController rc;

    private boolean lastWrittenPart;

    private final int COUNTS_GLOBAL_OFFSET = 0;
    private final int COUNTS_DATA_WHICH = 0;
    private final int COUNTS_DATA_INTS = 1;
    private final int[] COUNTS_BITS = {0, 8, 20, 32, 48, 56, 64}; // ARCHON, GARDENER, LUMBERJACK, SOLDIER, TANK, SCOUT

    public Broadcast(RobotController rc) {
        this.rc = rc;
    }

    public int getCountOf() throws GameActionException {

        boolean currentlyWrittenPart = rc.readBroadcastBoolean(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_WHICH);
        int startIdx = rc.getType().ordinal();
        int data = rc.readBroadcast(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_INTS + (!currentlyWrittenPart ? 0 : 2) + (COUNTS_BITS[startIdx] < 32 ? 0 : 1));

        return intFromBits(data, COUNTS_BITS[startIdx], COUNTS_BITS[startIdx  + 1]);
    }

    public MapLocation[] getLocations() {
        throw new NotImplementedException();
    }

    public void takeIn() throws GameActionException {

        boolean currentlyWrittenPart = rc.readBroadcastBoolean(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_WHICH);

        if (lastWrittenPart == currentlyWrittenPart) {
            currentlyWrittenPart = !currentlyWrittenPart;
            lastWrittenPart = currentlyWrittenPart;
            rc.broadcastBoolean(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_WHICH, currentlyWrittenPart);
            rc.broadcast(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_INTS + (currentlyWrittenPart ? 0 : 2), 0);
            rc.broadcast(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_INTS + (currentlyWrittenPart ? 0 : 2) + 1, 0);
        }
        lastWrittenPart = currentlyWrittenPart;

        int startIdx = COUNTS_BITS[rc.getType().ordinal()];
        int dataIdx = COUNTS_GLOBAL_OFFSET + COUNTS_DATA_INTS + (currentlyWrittenPart ? 0 : 2) + (startIdx < 32 ? 0 : 1);
        int buffer = rc.readBroadcast(dataIdx);
        int newBuffer = incrementBits(buffer, startIdx);
        rc.broadcast(dataIdx, newBuffer);
    }

    private int intFromBits(int buffer, int startIdx, int endIdx) {
        return (buffer >> startIdx) & (2 * (endIdx - startIdx) - 1);
    }

    private int incrementBits(int buffer, int startIdx) {
        return buffer + 1 << startIdx;
    }
}
