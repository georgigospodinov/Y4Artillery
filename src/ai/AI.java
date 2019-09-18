package ai;

import main.Area;
import objects.Block;
import objects.Player;
import processing.core.PApplet;
import processing.core.PVector;
import util.Props;

import static main.ScreenManager.instance;
import static objects.Shell.S;

public class AI extends Player {

    private static final int OBSTACLE_VIEW_RANGE = (int) Props.getLong("obstacle view range");
    private static final float AI_REDUCE_FACTOR = ((float) Props.getDouble("ai reduce factor"));
    private static final float AI_ELEVATION_CHANGE_INIT = PApplet.radians(((float) Props.getDouble("ai elevation change init")));
    private static final float AI_STRENGTH_CHANGE_INIT = ((float) Props.getDouble("ai strength change init"));
    private static final float AI_CHANGE_CLOSE_TO_ZERO = ((float) Props.getDouble("ai change close to zero"));
    private static final int AI_MAX_MOVE_TIME = (int) Props.getLong("ai max move time");

    private Player opponent;
    private PreviousShot shot;
    private boolean approachOpponent;
    private boolean backAway;
    private Float targetElevation = null, targetStrength = null;
    private float elevationChange = AI_ELEVATION_CHANGE_INIT;
    private float strengthChange = AI_STRENGTH_CHANGE_INIT;
    private int startMoveTime;
    private boolean timerStarted;

    public void setShot() {
        float h = -S.getVelocity().heading();
        if (h < 0) h += PApplet.PI * 2;
        shot = new PreviousShot(h, new PVector(S.getX(), S.getY()));
    }

    public AI(boolean direction) {
        super(direction);
        resetTargets();
    }

    @Override
    public void resetPosition() {
        super.resetPosition();
        resetTargets();
    }

    private void resetTargets() {
        approachOpponent = true;
        backAway = true;
        targetElevation = null;
        targetStrength = null;
        shot = null;
        startMoveTime = 0;
        timerStarted = false;
    }

    private void startTimer() {
        if (timerStarted) return;

        startMoveTime = instance.millis();
        timerStarted = true;
    }

    private float obstacleInView() {
        PVector currentHighPoint = Area.getHighPointUnder(this);
        int tempWidth = getWidth();
        float tempX = getX();
        this.setWidth(getWidth() + OBSTACLE_VIEW_RANGE * Block.WIDTH);
        if (opponent.getX() < this.getX()) {
            this.setX(tempX - OBSTACLE_VIEW_RANGE * Block.WIDTH);
        }
        PVector obstacleHighPoint = Area.getHighPointUnder(this);
        this.setWidth(tempWidth);
        this.setX(tempX);

        float dir = -PVector.sub(obstacleHighPoint, currentHighPoint).heading();
        if (dir < 0) dir += PApplet.PI * 2;

        return dir;
    }

    private boolean canMove() {
        PVector next = nextPosition();
        PVector temp = new PVector(getX(), getY());
        float currentHeight = Area.getHeightUnder(this);
        setX(next.x);
        setY(next.y);
        float nextHeight = Area.getHeightUnder(this);
        setX(temp.x);
        setY(temp.y);

        if (nextHeight < currentHeight - this.getHeight() / 2)
            return false;
        if (willIntersect(opponent)) return false;

        return instance.millis() - startMoveTime < AI_MAX_MOVE_TIME;
    }

    private void considerMove() {
        boolean dir = opponent.getX() > this.getX();
        if (approachOpponent) {
            if (dir) startReacting(MOVE_RIGHT_BUTTON);
            else startReacting(MOVE_LEFT_BUTTON);
            approachOpponent = canMove();
        }
        else if (backAway) {
            if (dir) {
                stopReacting(MOVE_RIGHT_BUTTON);
                startReacting(MOVE_LEFT_BUTTON);
            }
            else {
                stopReacting(MOVE_LEFT_BUTTON);
                startReacting(MOVE_RIGHT_BUTTON);
            }
            backAway = canMove();
        }
        else {
            stopReacting(MOVE_RIGHT_BUTTON);
            stopReacting(MOVE_LEFT_BUTTON);
        }
    }

    private void reduceElevationChange() {
        if (elevationChange < AI_CHANGE_CLOSE_TO_ZERO * AI_ELEVATION_CHANGE_INIT)
            elevationChange = AI_ELEVATION_CHANGE_INIT;
        else elevationChange *= AI_REDUCE_FACTOR;
    }

    private void reduceStrengthChange() {
        if (strengthChange < AI_CHANGE_CLOSE_TO_ZERO * AI_STRENGTH_CHANGE_INIT)
            strengthChange = AI_STRENGTH_CHANGE_INIT;
        else strengthChange *= AI_REDUCE_FACTOR;
    }

    private void calculateElevation() {
        if (targetElevation != null) return;

        targetElevation = this.elevation;
        if (shot.gotInTerrain()) {
            if (opponent.getX() > this.getX()) {
                targetElevation += elevationChange;
                while (obstacleInView() > targetElevation)
                    targetElevation += elevationChange;
            }
            else targetElevation -= elevationChange;

            startReacting(INCREASE_ELEVATION_BUTTON);
        }
        else {
            if (opponent.getX() > this.getX())
                targetElevation -= elevationChange;
            else {
                targetElevation += elevationChange;
                while (obstacleInView() > targetElevation)
                    targetElevation += elevationChange;
            }

            startReacting(DECREASE_ELEVATION_BUTTON);
        }

        if (targetElevation > ELEVATION_MAX)
            targetElevation = ELEVATION_MAX;
        if (targetElevation < ELEVATION_MIN)
            targetElevation = ELEVATION_MIN;

        reduceElevationChange();
    }

    private void calculateStrength() {
        if (targetStrength != null) return;

        targetStrength = this.strength;
        if (shot.overshot(this.getX(), opponent.getX())) {
            targetStrength -= strengthChange;
            startReacting(DECREASE_STRENGTH_BUTTON);
        }
        else if (shot.undershot(this.getX(), opponent.getX())) {
            targetStrength += strengthChange;
            startReacting(INCREASE_STRENGTH_BUTTON);
        }

        if (targetStrength > STRENGTH_MAX)
            targetStrength = STRENGTH_MAX;
        if (targetStrength < STRENGTH_MIN)
            targetStrength = STRENGTH_MIN;
        reduceStrengthChange();
    }

    private void think() {
        // No information to consider.
        if (shot == null) return;
        opponent = instance.game.getOpponent(this);

        considerMove();
        calculateElevation();
        calculateStrength();
    }

    private boolean check() {
        if (shot == null) return true;

        if (strength >= targetStrength)
            stopReacting(INCREASE_STRENGTH_BUTTON);
        if (strength <= targetStrength)
            stopReacting(DECREASE_STRENGTH_BUTTON);
        if (elevation >= targetElevation) {
            if (opponent.getX() > this.getX())
                stopReacting(INCREASE_ELEVATION_BUTTON);
            else stopReacting(DECREASE_ELEVATION_BUTTON);
        }
        if (elevation <= targetElevation) {
            if (opponent.getX() > this.getX())
                stopReacting(DECREASE_ELEVATION_BUTTON);
            else stopReacting(INCREASE_ELEVATION_BUTTON);
        }

        return pressedButtons.isEmpty();
    }

    @Override
    public void react() {
        if (instance.game.shouldNotReact()) return;
        startTimer();

        think();
        super.react();
        if (check()) {
            resetTargets();
            shoot();
        }
    }
}
