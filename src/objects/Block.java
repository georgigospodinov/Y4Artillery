package objects;

import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import util.Props;

import static main.ScreenManager.instance;
import static processing.core.PConstants.RECT;

public class Block extends PhysicalObject {

    public static final int WIDTH = (int) Props.getLong("block width");

    private static int height;
    private static PImage image = null;
    private static PShape block = null;

    public static int height() {
        return height;
    }

    private static void createDefaultShape() {
        block = instance.createShape(RECT, 0, 0, WIDTH, height);
        int stroke = (int) Props.getLong("block stroke");
        block.setStroke(stroke);

        int red = (int) Props.getLong("block color red");
        int green = (int) Props.getLong("block color green");
        int blue = (int) Props.getLong("block color blue");
        int color = instance.color(red, green, blue);
        block.setFill(true);
        block.setFill(color);
    }

    public static void loadImage() {
        image = instance.loadImageProp("block image");
        if (image != null) {
            image.resize(WIDTH, 0);
            height = image.height;
        }
        else {
            height = (int) Props.getLong("block height");
            createDefaultShape();
        }
    }

    /**
     * Return true if this block is higher than the other.
     * Remember that the top is 0, so we need the one with lower y-coordinate.
     *
     * @param other the block to compare to
     * @return true if this block is higher than the other
     */
    public boolean isHigher(Block other) {
        return this.getY() < other.getY();
    }

    public void drawSelf() {
        if (image != null)
            instance.image(image, getX(), getY());
        else instance.shape(block, getX(), getY());
    }

    public Block(float x, float y) {
        setX((int) x);
        setY((int) y);
        setWidth(WIDTH);
        setHeight(height);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Block))
            return false;

        Block other = (Block) obj;
        return this.getPosition().equals(other.getPosition());
    }

    @Override
    public String toString() {
        return getPosition().toString();
    }


}
