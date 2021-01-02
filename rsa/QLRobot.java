package rsa;

import com.github.chen0040.rl.learning.qlearn.QAgent;
import robocode.*;

import java.util.LinkedList;
import java.util.List;

public class QLRobot extends Robot {

    /*
        states:
            • x position (5)
            • y position (5)
            • distance between QLRobot and Enemy (3)
            • energy (3)

        actions:
            • circling clockwise
            • circling counterclockwise
            • moving toward enemy
            • moving away from enemy

        rewards:
            • QLRobot being hit by enemy robot: reward=-2
            • QLRobot hits enemy robot: reward=+3
            • QLRobot being hit by enemy bullet: reward=-3
            • QLRobot hits wall: reward=-2
     */

    public enum Movement {
        CIRCLE_CLOCKWISE(0),
        CIRCLE_COUNTERCLOCKWISE(1),
        MOVE_TOWARD_ENEMY(2),
        MOVE_AWAY_ENEMY(3);

        private int actionId;

        Movement(int actionId) {
            this.actionId = actionId;
        }
    }

    // setup
    private int xStatesCount = 5;
    private int yStatesCount = 5;
    private int enemyDistanceStatesCount = 3;
    private int energyStatesCount = 3;

    private int totalStatesCount = xStatesCount * yStatesCount * enemyDistanceStatesCount * energyStatesCount;
    private List<Integer> statesMapping = new LinkedList<>();

    private int actionsCount = 4;

    // robot properties
    private int energy = -1;

    // battle properties
    private double enemyDistance = -1;

    // q-learning properties
    private int currentAction = -1;
    private double currentReward = 0;
    private int currentState = -1;


    private QAgent qAgent = new QAgent(totalStatesCount, actionsCount);

    public QLRobot() {
    }

    public void run() {

        initializeStatesMapping();

        while (true) {
            currentAction = qAgent.selectAction().getIndex();
            performAction(currentAction);
            currentState = getState();
            out.println(String.format("Current state: %d", currentState));
            out.println(String.format("Current reward: %f", currentReward));
            out.println(String.format("Current action: %d", currentAction));
            qAgent.update(currentAction, currentState, currentReward);
            currentReward = 0;
        }
    }

    private void performAction(int actionId) {
        Movement movement = qActionToMovement(actionId);
        switch (movement) {
            case CIRCLE_CLOCKWISE:
                this.ahead(100.0D);
                this.turnRight(90);
                this.ahead(100.0D);
                this.turnRight(90);
                break;
            case CIRCLE_COUNTERCLOCKWISE:
                this.ahead(100.0D);
                this.turnLeft(90);
                this.ahead(100.0D);
                this.turnLeft(90);
                break;
            case MOVE_TOWARD_ENEMY:
                this.ahead(100.0D);
                break;
            case MOVE_AWAY_ENEMY:
                this.back(100.0D);
                break;
        }
    }

    private Movement qActionToMovement(int actionId) {
        for (Movement movement : Movement.values()) if (movement.actionId == actionId) return movement;
        return Movement.CIRCLE_CLOCKWISE;
    }

    private int getState() {
        this.scan();
        int stateValue = getXBucket() * 1000 + getYBucket() * 100 + getEnergyBucket() * 10 + getEnemyDistanceBucket();
        return this.statesMapping.indexOf(stateValue);
    }

    private int getXBucket() {
        final int X_MAX_POS = 782;
        int bucketSize = X_MAX_POS / xStatesCount;
        return Double.valueOf((this.getX() - 1) / bucketSize).intValue();
    }

    private int getYBucket() {
        final int Y_MAX_POS = 582;
        int bucketSize = Y_MAX_POS / yStatesCount;
        return Double.valueOf((this.getY() - 1) / bucketSize).intValue();
    }

    private int getEnemyDistanceBucket() {
        final int ENEMY_DISTANCE_MAX = 1200;
        int bucketSize = ENEMY_DISTANCE_MAX / enemyDistanceStatesCount;
        return Double.valueOf((this.enemyDistance - 1) / bucketSize).intValue();
    }

    private int getEnergyBucket() {
        final int ENERGY_MAX = 100;
        int bucketSize = ENERGY_MAX / energyStatesCount;
        return Double.valueOf((this.getEnergy() - 1) / bucketSize).intValue();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        this.enemyDistance = event.getDistance();
        this.fire(1.0D);
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if (event.isMyFault()) this.currentReward += 3;
        else this.currentReward -= 2;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        this.currentReward -= 3;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        this.currentReward -= 2;
    }

    private void initializeStatesMapping() {
        for (int a = 0; a < this.xStatesCount; a++) {
            for (int b = 0; b < this.yStatesCount; b++) {
                for (int c = 0; c < this.energyStatesCount; c++) {
                    for (int d = 0; d < this.enemyDistanceStatesCount; d++) {
                        this.statesMapping.add(a * 1000 + b * 100 + c * 10 + d);
                    }
                }
            }
        }
    }
}
