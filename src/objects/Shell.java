package objects;

import main.Area;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import util.Props;

import static main.ScreenManager.instance;
import static processing.core.PConstants.ELLIPSE;

public class Shell extends PhysicalObject {

    // Only one shell accessed via static methods, so that we do not go through constructors on every shot.
    public static final Shell S = new Shell();
    private static PImage shellImage;
    private static PShape shellShape;
    private static boolean inAir = false;

    private static final int EXPLOSION_DURATION = (int) Props.getLong("explosion duration");
    private static final double EXPLOSION_RADIUS_PER_SECOND = Props.getDouble("explosion radius per second");
    private static int explosionStart;
    private static PImage explosionImage;
    private static boolean exploding = false;

    static {
        S.setMass((float) Props.getDouble("shell mass"));
        S.setWidth((int) Props.getLong("shell width"));
        S.setHeight((int) Props.getLong("shell height"));

        explosionImage = instance.loadImageProp("explosion image");
        shellImage = instance.loadImageProp("shell image");
        if (shellImage != null) shellImage.resize(S.getWidth(), S.getHeight());
        else createShellShape();
    }

    public static boolean isInAir() {
        return inAir;
    }

    static void launch(PVector launchingTankPosition, float force, float elevation) {
        S.halt();
        S.setPosition(launchingTankPosition);
        S.setX(S.getX() + Player.WIDTH / 2f);  // assuming barrel starts at the middle.
        S.setX(S.getX() + Player.BARREL_LENGTH * PApplet.cos(elevation));
        S.setY(S.getY() - (Player.BARREL_LENGTH * PApplet.sin(elevation)));
        PVector initialAcceleration = new PVector(PApplet.cos(elevation), -PApplet.sin(elevation));
        initialAcceleration.mult(force / S.getMass());  // a = F/m
        // multiply by frameRate to make up for single acceleration rather than continuous.
        initialAcceleration.mult(instance.frameRate);
        S.applyAcceleration(initialAcceleration);
        S.startMoving();
        inAir = true;
    }

    private static void stopExploding(int diameter) {
        exploding = false;
        inAir = false;

        PVector origin = S.getPosition();
        origin.x += S.getWidth() / 2f;
        origin.y += S.getHeight() / 2f;
        float radius = diameter / 2f;
        Area.destroyBlocks(origin, radius);

        if (instance.game.player1.centerDistance(origin) <= radius) {
            instance.game.score(instance.game.player2);
        }

        if (instance.game.player2.centerDistance(origin) <= radius) {
            instance.game.score(instance.game.player1);
        }

        instance.game.changeTurn();
    }

    private static void drawExplosion() {
        int duration = instance.millis() - explosionStart;
        int diameter = 2 * (int) (EXPLOSION_RADIUS_PER_SECOND * duration / 1000);

        if (explosionImage != null && diameter > 0) {
            PImage temp = explosionImage.copy();
            temp.resize(diameter, diameter);  // Resizing the same instance multiple times causes loss of quality.
            instance.image(temp, S.getX() - diameter / 2f, S.getY() - diameter / 2f);
        }
        else {
            PShape explosionShape = instance.createShape(ELLIPSE, 0, 0, diameter, diameter);
            explosionShape.setFill(instance.game.getTurnColor());
            instance.shape(explosionShape, S.getX(), S.getY());
        }
        if (duration >= EXPLOSION_DURATION)
            stopExploding(diameter);
    }

    private static void createShellShape() {
        int red = (int) Props.getLong("shell color red");
        int green = (int) Props.getLong("shell color green");
        int blue = (int) Props.getLong("shell color blue");
        int stroke = (int) Props.getLong("shell stroke");
        shellShape = instance.createShape(ELLIPSE, 0, 0, S.getWidth(), S.getHeight());
        shellShape.setFill(true);
        shellShape.setFill(instance.color(red, green, blue));
        shellShape.setStroke(stroke);
    }

    public static void draw() {
        if (!inAir) return;  // No need to draw it.

        if (exploding) {
            drawExplosion();
            return;
        }

        if (shellImage != null)
            instance.image(shellImage, S.getX(), S.getY());
        else instance.shape(shellShape, S.getX(), S.getY());
    }

    public static void moveS() {
        if (!inAir) return;

        if (exploding) return;

        S.move();
        S.applyGravity();
        S.applyAirResistance();
        S.applyWind();
        outAir();
        hitTank();
        Area.detectShellCollision();
    }

    /**
     * Checks if the Shell is out of the screen and cannot come back.
     * That is, it has left from the west, south or east end.
     * If shoot out from the north, gravity will eventually pull it back down.
     */
    private static void outAir() {
        if (S.getY() >= instance.getHeight() || S.getX() < 0 || S.getX() >= instance.getWidth()) {
            inAir = false;
            instance.game.changeTurn();
        }
    }

    /**
     * Checks if the Shell has hit a tank.
     */
    private static void hitTank() {
        if (S.intersects(instance.game.player1) || S.intersects(instance.game.player2))
            explode();
    }

    public static void explode() {
        explosionStart = instance.millis();
        exploding = true;
    }

    public static void remove() {
        inAir = false;
    }
}
