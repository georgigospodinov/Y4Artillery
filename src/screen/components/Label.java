package screen.components;

import util.Props;

import static main.ScreenManager.instance;

public class Label {
    private static final float TEXT_SIZE_FACTOR = 0.8f;
    static final int HIGHLIGHT_COLOR;

    static {
        int red = (int) Props.getLong("button color red");
        int green = (int) Props.getLong("button color green");
        int blue = (int) Props.getLong("button color blue");
        HIGHLIGHT_COLOR = instance.color(red, green, blue);
    }

    private String text;
    protected float x, y;
    protected float width, height;

    public Label(String text, float x, float y, float height) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.height = height;
        instance.textSize(height * TEXT_SIZE_FACTOR);
        this.width = instance.textWidth(text);
    }

    public void drawSelf() {
        instance.textSize(height * TEXT_SIZE_FACTOR);
        instance.fill(0);
        instance.text(text, x, y + height * TEXT_SIZE_FACTOR);
    }
}
