package objects;

import processing.core.PApplet;
import processing.core.PVector;
import util.Props;

import static main.ScreenManager.instance;

/**
 * Apply physics to objects of this class.
 * This is essentially a Particle.
 */
public abstract class PhysicalObject {
    private static final long PIXELS_PER_METER = Props.getLong("pixels per meter");
    private static final PVector GRAVITY = new PVector();
    private static final float FRICTION_DAMPING = (float) Props.getDouble("friction damping");
    private static final float DRAG_K1 = (float) Props.getDouble("drag k1");
    private static final float DRAG_K2 = (float) Props.getDouble("drag k2");
    private static final PVector WIND = new PVector();
    private static final float MIN_WIND = (float) Props.getDouble("min wind speed");
    private static final float WIND_CHANGE_CHANCE = (float) Props.getDouble("wind change chance");
    private static final float MAX_WIND = (float) Props.getDouble("max wind speed");
    private static final float WIND_SIZE = instance.getHeight() * Props.getLong("wind text size") / 100f;
    private static final float WIND_X = instance.getWidth() * Props.getLong("wind text x") / 100f;
    private static final float WIND_Y = instance.getHeight() * Props.getLong("wind text y") / 100f;
    private static final float AIR_DENSITY = (float) (Props.getDouble("air density"));
    private static final float WIND_DRAG = (float) Props.getDouble("wind drag");
    static final String DEGREES_SYMBOL = "\u00b0";
    private static Integer windColor = null;

    static {
        setGravity();
        changeWind();
    }

    private static void setGravity() {
        float acceleration = -(float) Props.getDouble("gravity acceleration");
        float direction = PApplet.radians((float) Props.getDouble("gravity direction"));
        float gravityX = acceleration * PApplet.cos(direction);
        float gravityY = acceleration * PApplet.sin(direction);
        GRAVITY.set(gravityX, gravityY);
    }

    private static String windDescription() {
        float m = WIND.mag();
        if (m < 0.5) return "Calm";
        if (m < 1.5) return "Light air";
        if (m < 3.3) return "Light breeze";
        if (m < 5.5) return "Gentle breeze";
        if (m < 7.9) return "Moderate breeze";
        if (m < 10.7) return "Fresh breeze";
        if (m < 13.8) return "Strong breeze";
        if (m < 17.1) return "High wind";
        if (m < 20.7) return "Gale";
        if (m < 24.4) return "Strong gale";
        if (m < 28.4) return "Storm";
        if (m < 32.6) return "Violent storm";
        return "Hurricane";
    }

    public static void printWind() {
        if (windColor == null) {
            int red = (int) Props.getLong("wind red");
            int green = (int) Props.getLong("wind green");
            int blue = (int) Props.getLong("wind blue");
            windColor = instance.color(red, green, blue);
        }
        float direction = PApplet.degrees(WIND.heading());
        instance.fill(windColor);
        instance.textSize(WIND_SIZE);
        String mag = String.format("%s: %.1f m/s  at %.2f" + DEGREES_SYMBOL, windDescription(), WIND.mag(), direction);
        instance.text(mag, WIND_X, WIND_Y);
    }

    public static void changeWind() {
        if (instance.random(1) > WIND_CHANGE_CHANCE) return;

        float x = instance.random(-1, 1);
        float y = instance.random(-1, 1);
        PVector randomDir = new PVector(x, y);
        float str = instance.random(MIN_WIND, MAX_WIND);

        randomDir.mult(str);
        WIND.set(randomDir);
    }

    private PVector position = new PVector(0, 0);
    private PVector velocity = new PVector(0, 0);  // pixels per second
    private int width;
    private Integer height = null;
    private Float mass = null;
    private boolean moving;
    private boolean falling;

    PVector getPosition() {
        return position.copy();
    }

    void setPosition(PVector p) {
        position.set(p);
    }

    public float getX() {
        return position.x;
    }

    public void setX(float x) {
        position.x = x;
    }

    public float getY() {
        return position.y;
    }

    public void setY(float y) {
        position.y = y;
    }

    public PVector getVelocity() {
        return velocity.copy();
    }

    void setVelocity(PVector v) {
        velocity.set(v);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int h) {
        if (height == null) height = h;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    void startMoving() {
        moving = true;
    }

    boolean isMoving() {
        return moving;
    }

    public void startFalling() {
        falling = true;
    }

    public boolean isFalling() {
        return falling;
    }

    float getMass() {
        return mass;
    }

    void setMass(float m) {
        if (mass == null) mass = m;
    }

    public void halt() {
        velocity.set(0, 0);
        moving = false;
        falling = false;
    }

    void applyAcceleration(PVector acceleration) {
        PVector accelerationAtThisFrameRate = PVector.mult(acceleration, 1 / instance.frameRate);
        accelerationAtThisFrameRate.mult(PIXELS_PER_METER);
        // acceleration is measured in 'pixels per second squared'
        velocity.add(accelerationAtThisFrameRate);
    }

    // All of the methods below take affect in a single frame. Therefore most 'per second' physics is divided by frameRate.
    public void applyGravity() {
        applyAcceleration(GRAVITY);
        falling = true;
    }

    void applyWind() {
        float windSpeed = WIND.mag();
        float area = (1.0f * this.height / PIXELS_PER_METER) * (1.0f * this.width / PIXELS_PER_METER);
        float mag = 0.5f * AIR_DENSITY * windSpeed * windSpeed * WIND_DRAG * area / this.mass;
        PVector acceleration = WIND.copy().normalize().mult(mag);
        applyAcceleration(acceleration);
        moving = true;  // wind moves objects
    }

    void applyAirResistance() {
        float velMag = velocity.mag();
        float drag = DRAG_K1 * velMag + DRAG_K2 * velMag * velMag;
        PVector dragAcceleration = velocity.copy().normalize();
        dragAcceleration.mult(-drag);
        applyAcceleration(dragAcceleration);
    }

    void applyGroundFriction() {
        velocity.mult(FRICTION_DAMPING);
    }

    public void move() {
        if (!moving && !falling) return;

        PVector velocityAtThisFrameRate = PVector.mult(velocity, 1 / instance.frameRate);
        position.add(velocityAtThisFrameRate);
    }

    /**
     * Calculate and return the position this object will be in.
     * This is the predicted position for the next frame.
     *
     * @return predicted position
     */
    public PVector nextPosition() {
        PVector vel = PVector.mult(velocity, 1 / instance.frameRate);
        return PVector.add(position, vel);
    }

    public boolean intersects(PhysicalObject other) {
        if (this.position.x < other.position.x) {  // other is to the right
            if (other.position.x - this.position.x > this.width)
                return false;
        }
        else {  // other is to the left
            if (this.position.x - other.position.x > other.width)
                return false;
        }

        if (other.position.y > this.position.y) {  // other is below
            if (other.position.y - this.position.y > this.height)
                return false;
        }
        else {  // other is above
            if (this.position.y - other.position.y > other.height)
                return false;
        }

        return true;
    }

    protected boolean willIntersect(PhysicalObject other) {
        PVector current = position.copy();
        position = nextPosition();
        boolean will = intersects(other);
        position = current;
        return will;
    }

    public float centerDistance(PVector point) {
        PVector center = this.position.copy();
        center.x += this.width / 2f;
        center.y += this.height / 2f;
        return center.dist(point);
    }

}
