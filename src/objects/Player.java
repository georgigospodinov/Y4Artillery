package objects;

import ai.AI;
import main.Area;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import util.Props;

import java.util.HashMap;
import java.util.HashSet;

import static main.ScreenManager.instance;
import static processing.core.PConstants.RECT;
import static screen.AbstractScreen.COORDINATE_FIX;

public class Player extends PhysicalObject {

    private static final float TEXT_Y = instance.getHeight() * Props.getLong("text y") / 100f;
    private static final boolean AIM_RIGHT = true;
    private static final float ELEVATION_DELTA = PApplet.radians((float) Props.getDouble("elevation delta"));
    private static final float ELEVATION_START = PApplet.radians((float) Props.getDouble("elevation start"));
    protected static final float ELEVATION_MAX = PApplet.radians((float) Props.getDouble("elevation max"));
    protected static final float ELEVATION_MIN = PApplet.radians((float) Props.getDouble("elevation min"));
    private static final float STRENGTH_DELTA = (float) Props.getDouble("strength delta");
    private static final float STRENGTH_START = (float) Props.getDouble("strength start");
    protected static final float STRENGTH_MAX = (float) Props.getDouble("strength max");
    protected static final float STRENGTH_MIN = (float) Props.getDouble("strength min");
    private static final float MOVE_FORCE = (float) Props.getDouble("move force");
    private static final float IMPULSE_FACTOR = (float) Props.getDouble("impulse factor");
    private static final float CLIMB_DAMP = (float) Props.getDouble("climb factor");
    private static final float STOP_RANGE = (float) Props.getDouble("stop range");
    static final int BARREL_LENGTH = (int) Props.getLong("barrel length");
    private static final int BARREL_WIDTH = (int) Props.getLong("barrel width");
    static final int WIDTH = (int) Props.getLong("tank width");
    private static final int TEXT_LINE_SPACING = (int) Props.getLong("text line spacing");
    private static final float SCORE_SIZE = instance.getHeight() * Props.getLong("text score size") / 100f;

    private static boolean p1isAI = false, p2isAI = false;
    private static boolean p1usesBarrel = false, p2usesBarrel = false;
    private static boolean p1usesImage = false, p2usesImage = false;

    public static void setP1isAI(boolean p1isAI) {
        Player.p1isAI = p1isAI;
    }

    public static void setP2isAI(boolean p2isAI) {
        Player.p2isAI = p2isAI;
    }

    public static void setP1usesBarrel(boolean p1usesBarrel) {
        Player.p1usesBarrel = p1usesBarrel;
    }

    public static void setP2usesBarrel(boolean p2usesBarrel) {
        Player.p2usesBarrel = p2usesBarrel;
    }

    public static void setP1usesImage(boolean p1usesImage) {
        Player.p1usesImage = p1usesImage;
    }

    public static void setP2usesImage(boolean p2usesImage) {
        Player.p2usesImage = p2usesImage;
    }

    public static Player createPlayer(String id) {
        Player p;
        if (id.equals("1")) {
            if (p1isAI)
                p = new AI(true);
            else p = new Player(true);
        }
        else {
            if (p2isAI)
                p = new AI(false);
            else p = new Player(false);
        }
        return p;
    }

    private final boolean direction;
    protected final int MOVE_LEFT_BUTTON;
    protected final int MOVE_RIGHT_BUTTON;
    protected final int INCREASE_ELEVATION_BUTTON;
    protected final int DECREASE_ELEVATION_BUTTON;
    protected final int INCREASE_STRENGTH_BUTTON;
    protected final int DECREASE_STRENGTH_BUTTON;
    private final int SHOOT_BUTTON;
    private final HashMap<Integer, Runnable> buttonReactions = new HashMap<>();
    protected final HashSet<Integer> pressedButtons = new HashSet<>();
    private float textX;
    protected float elevation;  // In radians, as measure from the x-axis.
    protected float strength = STRENGTH_START;  // In newtons.
    private int score = 0;  // How many tanks this player has destroyed.
    private PImage image = null;
    private PShape tank = null;
    private boolean useBarrel;
    private PShape barrel = null;
    private String tankID;
    private int color;
    private PVector prevVel = new PVector(0, 0);


    public int getScore() {
        return score;
    }

    public int getColor() {
        return color;
    }

    public void scorePoint() {
        score++;
    }

    public String getID() {
        return tankID;
    }

    public void resetPosition() {
        this.setY(0);
        strength = STRENGTH_START;
        if (direction == AIM_RIGHT) {
            elevation = ELEVATION_START;
            this.setX(instance.random(instance.getWidth() / 3f));
            this.setX(this.getX() + this.getWidth());  // Make sure the tank is on the screen.
        }
        else {
            elevation = ELEVATION_MAX - ELEVATION_START;
            this.setX(2 * instance.getWidth() / 3f + instance.random(instance.getWidth() / 3f));
            this.setX(this.getX() - this.getWidth());  // Make sure the tank is on the screen.
        }
    }

    protected Player(boolean direction) {
        this.direction = direction;
        this.setWidth(WIDTH);

        if (direction == AIM_RIGHT) {
            elevation = ELEVATION_START;
            tankID = "1";
            useBarrel = p1usesBarrel;
            // Put the first tank in the left part of the screen.
            setX(instance.random(instance.getWidth() / 3f));
            setX(getX() + getWidth());  // Make sure the tank is on the screen.
            if (p1usesImage) loadImage();
        }
        else {
            elevation = ELEVATION_MAX - ELEVATION_START;
            tankID = "2";
            useBarrel = p2usesBarrel;
            // Put the second tank in the right part of the screen.
            setX(2 * instance.getWidth() / 3f + instance.random(instance.getWidth() / 3f));
            setX(getX() - getWidth());  // Make sure the tank is on the screen.
            if (p2usesImage) loadImage();
        }

        textX = instance.getWidth() * Props.getLong("text tank " + tankID + " x") / 100f;
        setMass((float) Props.getDouble("tank " + tankID + " mass"));
        INCREASE_ELEVATION_BUTTON = (int) Props.getLong("tank " + tankID + " increase elevation");
        DECREASE_ELEVATION_BUTTON = (int) Props.getLong("tank " + tankID + " decrease elevation");
        INCREASE_STRENGTH_BUTTON = (int) Props.getLong("tank " + tankID + " increase strength");
        DECREASE_STRENGTH_BUTTON = (int) Props.getLong("tank " + tankID + " decrease strength");
        MOVE_LEFT_BUTTON = (int) Props.getLong("tank " + tankID + " move left");
        MOVE_RIGHT_BUTTON = (int) Props.getLong("tank " + tankID + " move right");
        SHOOT_BUTTON = (int) Props.getLong("tank " + tankID + " shoot");
        buttonReactions.put(INCREASE_ELEVATION_BUTTON, this::increaseElevation);
        buttonReactions.put(DECREASE_ELEVATION_BUTTON, this::decreaseElevation);
        buttonReactions.put(INCREASE_STRENGTH_BUTTON, this::increaseStrength);
        buttonReactions.put(DECREASE_STRENGTH_BUTTON, this::decreaseStrength);
        buttonReactions.put(MOVE_LEFT_BUTTON, this::applyMoveLeft);
        buttonReactions.put(MOVE_RIGHT_BUTTON, this::applyMoveRight);
        buttonReactions.put(SHOOT_BUTTON, this::shoot);

        int red = (int) Props.getLong("tank " + tankID + " color red");
        int green = (int) Props.getLong("tank " + tankID + " color green");
        int blue = (int) Props.getLong("tank " + tankID + " color blue");
        color = instance.color(red, green, blue);

        if (image == null) createDefaultShape();
    }

    private void createDefaultShape() {
        setHeight((int) Props.getLong("tank height"));
        int stroke = (int) Props.getLong("tank " + tankID + " stroke");

        tank = instance.createShape(RECT, 0, 0, WIDTH, getHeight());
        tank.setFill(true);
        tank.setFill(color);
        tank.setStroke(stroke);
    }

    private void drawBarrel() {
        float x = getX() + getWidth() / 2f;
        float y = getY() + getHeight() / 2f;
        float alpha = -elevation - PApplet.PI / 2;
        // Shape rotation starts with 0 pointing down, addition is clockwise.
        if (barrel != null) {
            barrel.rotate(alpha);
            instance.shape(barrel, x, y);
            barrel.rotate(-alpha);
            return;
        }

        int stroke = (int) Props.getLong("tank " + tankID + " stroke");
        barrel = instance.createShape(RECT, 0, 0, BARREL_WIDTH, BARREL_LENGTH);
        barrel.setFill(true);
        barrel.setFill(color);
        barrel.setStroke(stroke);

        barrel.rotate(alpha);
        instance.shape(barrel, x, y);
        barrel.rotate(-alpha);
    }

    private void loadImage() {
        image = instance.loadImageProp("tank " + tankID + " image");
        if (image != null) {
            image.resize(WIDTH, 0);
            setHeight(image.height);
        }
    }

    public void drawSelf() {
        if (image != null)
            instance.image(image, getX(), getY());
        else instance.shape(tank, getX(), getY());

        if (useBarrel)
            drawBarrel();

        instance.textSize(SCORE_SIZE);

        // Score, elevation, strength.
        String e = "Elevation: " + PApplet.nf(PApplet.degrees(elevation), 3, 2) + DEGREES_SYMBOL;
        String s = "Strength: " + PApplet.nf(strength, 2, 1) + "N";  // kilo newtons

        float w = instance.textWidth(e);
        if (textX + w > instance.getWidth()) {
            textX = instance.getWidth() - w - COORDINATE_FIX;
        }

        instance.fill(0);
        instance.text("Score: " + score, textX, TEXT_Y);
        instance.text(e, textX, TEXT_Y + TEXT_LINE_SPACING);
        instance.text(s, textX, TEXT_Y + TEXT_LINE_SPACING * 2);
    }

    public void startReacting(int keyCode) {
        if (instance.game.shouldNotReact()) return;

        if (buttonReactions.containsKey(keyCode))
            pressedButtons.add(keyCode);
    }

    public void stopReacting(int keyCode) {
        pressedButtons.remove(keyCode);
    }

    // Return whether or not the player has shot.
    public void react() {
        if (instance.game.shouldNotReact()) return;

        // Shooting stops all other actions.
        if (pressedButtons.contains(SHOOT_BUTTON))
            shoot();
        else pressedButtons.forEach((key) -> buttonReactions.get(key).run());
    }

    private void restrictElevation() {
        if (elevation > ELEVATION_MAX) elevation = ELEVATION_MAX;
        if (elevation < ELEVATION_MIN) elevation = ELEVATION_MIN;
    }

    private void increaseElevation() {
        elevation += (direction == AIM_RIGHT ? ELEVATION_DELTA : -ELEVATION_DELTA) / instance.frameRate;
        restrictElevation();
    }

    private void decreaseElevation() {
        elevation += (direction == AIM_RIGHT ? -ELEVATION_DELTA : ELEVATION_DELTA) / instance.frameRate;
        restrictElevation();
    }

    private void restrictStrength() {
        if (strength > STRENGTH_MAX) strength = STRENGTH_MAX;
        if (strength < STRENGTH_MIN) strength = STRENGTH_MIN;
    }

    private void increaseStrength() {
        strength += STRENGTH_DELTA / instance.frameRate;
        restrictStrength();
    }

    private void decreaseStrength() {
        strength -= STRENGTH_DELTA / instance.frameRate;
        restrictStrength();
    }

    protected void shoot() {
        Shell.launch(getPosition(), strength, elevation);
        pressedButtons.clear();
    }

    private boolean stopWhenSlowed() {
        // If speed is less than STOP_RANGE and we are not moving, than the tank should stop.
        if (getVelocity().mag() <= STOP_RANGE)
            if (!pressedButtons.contains(MOVE_LEFT_BUTTON) && !pressedButtons.contains(MOVE_RIGHT_BUTTON)) {
                super.halt();
                return true;
            }

        return false;
    }

    @Override
    public void halt() {
        super.halt();
        if (prevVel.x != 0 || prevVel.y != 0) {
            setVelocity(prevVel);
            prevVel = new PVector(0, 0);
            startMoving();
        }
    }

    private void applyMoveRight() {
        startMoving();
        float acceleration = MOVE_FORCE / getMass();
        applyAcceleration(new PVector(acceleration, 0));
    }

    private void applyMoveLeft() {
        startMoving();
        float acceleration = -MOVE_FORCE / getMass();
        applyAcceleration(new PVector(acceleration, 0));
    }

    private void bump() {
        PVector vel = getVelocity();
        prevVel = new PVector(-vel.x, -vel.y);
        prevVel.mult(IMPULSE_FACTOR);
        halt();
    }

    @Override
    public void move() {
        if (!isMoving()) {
            if (isFalling()) super.move();
            return;
        }
        if (stopWhenSlowed()) return;

        PVector next = nextPosition();
        boolean offScreen = (next.x < 0) || (next.x > instance.getWidth() - this.getWidth());
        if (offScreen) {
            bump();
            return;
        }

        PVector temp = getPosition();
        float currentHeight = Area.getHeightUnder(this);
        setPosition(next);
        float nextHeight = Area.getHeightUnder(this);
        setPosition(temp);

        // Can't climb above half height
        if (nextHeight < currentHeight - getHeight() / 2) {
            bump();
            super.move();
            super.halt();
            return;
        }

        Player opp = instance.game.getOpponent(this);
        if (willIntersect(opp)) {
            bump();
            do super.move();
            while (willIntersect(opp));

            return;
        }

        boolean saveVelocity = false;
        // Save velocity if climbing
        if (nextHeight < currentHeight) {
            saveVelocity = true;
            applyClimbFactor();
        }
        // of if falling less than this.height.
        else if (nextHeight - currentHeight <= getHeight()) saveVelocity = true;

        super.move();

        applyAirResistance();
        applyWind();
        applyGroundFriction();

        if (saveVelocity) prevVel = this.getVelocity();
    }

    private void applyClimbFactor() {
        PVector vel = getVelocity();
        vel.mult(CLIMB_DAMP);
        setVelocity(vel);
    }

}
