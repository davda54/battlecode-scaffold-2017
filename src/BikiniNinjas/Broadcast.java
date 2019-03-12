package BikiniNinjas;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Broadcast {

    public RobotController rc;

    // java enum is nahovno, values 0, 1 and -1 for not_set state used
    private int lastWriteState;
    private final int PART_A = 0;
    private final int PART_B = 1;
    private final int NOT_SET = -1;

    private final int COUNTS_GLOBAL_OFFSET = 0;
    private final int COUNTS_DATA_WHICH = 0;
    private final int COUNTS_DATA_INTS = 1;
    // ARCHON, GARDENER, LUMBERJACK, SOLDIER, TANK, SCOUT
    private final int[] COUNTS_BITS = {0, 8, 20, 32, 48, 56, 64};

    private final int TREE_DENSITY_OFFSET = 6;
    private final float TREE_GAMMA = 0.9f;

    private final int ORCHARD_LOCATIONS_OFFSET = 7;


    public Broadcast(RobotController rc) throws GameActionException {
        this.rc = rc;
        this.lastWriteState = NOT_SET;
    }

    public int getCountOf(RobotType type) throws GameActionException {

        int currentWriteState = rc.readBroadcastInt(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_WHICH);
        int startIdx = type.ordinal();
        int data = rc.readBroadcast(getWritePartIdx(currentWriteState == PART_A ? PART_B : PART_A) + (COUNTS_BITS[startIdx] < 32 ? 0 : 1));

        return intFromBits(data, COUNTS_BITS[startIdx], COUNTS_BITS[startIdx  + 1]);
    }

    public MapLocation[] getLocations() {
        // TODO: implement
        return null;
    }

    public float getTreeDensity() throws GameActionException {
        return rc.readBroadcastFloat(TREE_DENSITY_OFFSET);
    }

    public void addTreeDensitySample(float sampledDensity) throws GameActionException {
        float currentDensity = rc.readBroadcastFloat(TREE_DENSITY_OFFSET);
        float newDensity;
        if (currentDensity == 0.0) newDensity = sampledDensity;
        else newDensity = TREE_GAMMA * currentDensity + (1.0f - TREE_GAMMA) * sampledDensity;
        rc.broadcastFloat(TREE_DENSITY_OFFSET, newDensity);
    }

    public void addOrchardLocations(ArrayList<MapLocation> locations) throws GameActionException {

        int start = ORCHARD_LOCATIONS_OFFSET + 1;
        int length = rc.readBroadcastInt(ORCHARD_LOCATIONS_OFFSET);

        for(int i = start; i < start + length; i+=2) {
            MapLocation location = getOrchardLocation(i);
            if (location == null) break; // may be redundant

            for (int j = 0; j < locations.size(); ++j) {
                float dist = locations.get(j).distanceSquaredTo(location);
                if (dist > 4) continue;
                locations.remove(j);
                j--;
            }

            if (locations.isEmpty()) return;
        }

        for (MapLocation l : locations) {
            rc.broadcastFloat(start + length, l.x);
            rc.broadcastFloat(start + length + 1, l.y);
            length += 2;
        }
        rc.broadcastInt(ORCHARD_LOCATIONS_OFFSET, length);
    }

    public void removeOrchardLocation(int idx) throws GameActionException {

        int start = ORCHARD_LOCATIONS_OFFSET + 1;
        int length = rc.readBroadcastInt(ORCHARD_LOCATIONS_OFFSET);

        float last_x = rc.readBroadcastFloat(start + length - 2);
        float last_y = rc.readBroadcastFloat(start + length - 1);

        rc.broadcastFloat(start + 2*idx, last_x);
        rc.broadcastFloat(start + 2*idx + 1, last_y);

        rc.broadcastFloat(start + length - 2, 0);
        rc.broadcastFloat(start + length - 1, 0);

        rc.broadcastInt(ORCHARD_LOCATIONS_OFFSET, length-2);
    }

    public ArrayList<MapLocation> getOrchardLocations() throws GameActionException {

        ArrayList<MapLocation> locations = new ArrayList<>();

        int start = ORCHARD_LOCATIONS_OFFSET + 1;
        int length = rc.readBroadcastInt(ORCHARD_LOCATIONS_OFFSET);

        for(int i = start; i < start + length; i+=2) {
            locations.add(getOrchardLocation(i));
        }

        return locations;
    }

    private MapLocation getOrchardLocation(int idx) throws GameActionException {

        float x = rc.readBroadcastFloat(idx);
        float y = rc.readBroadcastFloat(idx + 1);

        if (x == 0.0f && y == 0.0f) return null;

        return new MapLocation(x, y);
    }

    public void takeIn(HashMap<RobotType, Integer> inactiveChildren) throws GameActionException {

        int currentWriteState = rc.readBroadcastInt(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_WHICH);

        if (lastWriteState == NOT_SET) {
            lastWriteState = (currentWriteState == PART_A ? PART_B : PART_A);
        }

        if (lastWriteState == currentWriteState) {
            currentWriteState = (currentWriteState == PART_A ? PART_B : PART_A);
            rc.broadcastInt(COUNTS_GLOBAL_OFFSET + COUNTS_DATA_WHICH, currentWriteState);
            rc.broadcast(getWritePartIdx(currentWriteState), 0);
            rc.broadcast(getWritePartIdx(currentWriteState) + 1, 0);
        }
        lastWriteState = currentWriteState;

        int startIdx = COUNTS_BITS[rc.getType().ordinal()];
        int dataIdx = getWritePartIdx(currentWriteState);
        int bufferA = rc.readBroadcast(dataIdx);
        int bufferB = rc.readBroadcast(dataIdx + 1);

        if (startIdx < 32) bufferA = incrementBits(bufferA, startIdx, 1);
        else bufferB = incrementBits(bufferB, startIdx, 1);

        for (Map.Entry<RobotType, Integer> entry : inactiveChildren.entrySet()) {
            RobotType key = entry.getKey();
            Integer value = entry.getValue();
            if (COUNTS_BITS[key.ordinal()] < 32) bufferA = incrementBits(bufferA, COUNTS_BITS[key.ordinal()], value);
            else bufferB = incrementBits(bufferB, COUNTS_BITS[key.ordinal()], value);
        }

        rc.broadcast(dataIdx, bufferA);
        rc.broadcast(dataIdx + 1, bufferB);
    }

    private int intFromBits(int buffer, int startIdx, int endIdx) {
        return (buffer >> startIdx) & (2 * (endIdx - startIdx) - 1);
    }

    private int incrementBits(int buffer, int startIdx, int value) {
        return buffer + (value << startIdx);
    }

    private int getWritePartIdx(int currentWriteState) {
        return COUNTS_GLOBAL_OFFSET + COUNTS_DATA_INTS + (currentWriteState == PART_A ? 0 : 2);
    }
}
