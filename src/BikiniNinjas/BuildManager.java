package BikiniNinjas;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.ArrayList;
import java.util.HashMap;


public class BuildManager {

    private RobotController rc;
    private HashMap<RobotType, Integer> inactiveRobots;
    private ArrayList<Tuple<RobotType, Integer>> activationTimes;

    private final int TURNS_INACTIVE = 20;


    public BuildManager(RobotController rc) {
        this.rc = rc;
        this.inactiveRobots = new HashMap<>();
        this.activationTimes = new ArrayList<>();
    }

    public HashMap<RobotType, Integer> getInactiveRobots() {
        return inactiveRobots;
    }

    public void update() {
        for (int i = 0; i < activationTimes.size(); i++) {
            Tuple<RobotType, Integer> item = activationTimes.get(i);
            item.item2--;
            if (item.item2 >= 0) continue;
            inactiveRobots.merge(item.item1, -1, Integer::sum);
            activationTimes.remove(i);
            i--;
        }
    }

    public void build(RobotType type, Direction dir) throws GameActionException {
        if(RobotType.ARCHON == type) return;
        if(RobotType.GARDENER == type) rc.hireGardener(dir);
        else {
            activationTimes.add(new Tuple<>(type, TURNS_INACTIVE));
            rc.buildRobot(type, dir);
            inactiveRobots.merge(type, 1, Integer::sum);
        }
    }
}
