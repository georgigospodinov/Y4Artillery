package ai;

import processing.core.PApplet;
import processing.core.PVector;

class PreviousShot {
    // Direction and location of the collision of the last shell.
    private float direction;
    private PVector location;

    PreviousShot(float direction, PVector location) {
        this.direction = direction;
        this.location = location;
    }

    // Shot exploded when going up
    boolean gotInTerrain() {
        return (PApplet.PI * 9 / 8 >= direction && direction >= 0) || (direction >= PApplet.PI * 15 / 8);
    }

    boolean overshot(float ax, float bx) {
        if (bx > ax)
            return location.x > bx;
        else return location.x < bx;
    }

    boolean undershot(float ax, float bx) {
        if (bx > ax)
            return location.x < bx;
        else return location.x > bx;
    }
}
