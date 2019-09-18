package screen.components;

import main.ScreenManager;
import util.Props;

import static main.ScreenManager.instance;
import static screen.components.Label.HIGHLIGHT_COLOR;

public class Checkbox implements Clickable {

    private static final float CHECKBOX_SPACE = Props.getLong("checkbox space") / 100f;
    private static final float FILLBOX_SPACE = Props.getLong("fillbox space") / 100f;

    private Label text;
    private float checkSpace, fillSpace;
    private boolean checked = false;

    public boolean isChecked() {
        return checked;
    }

    @Override
    public void click() {
        checked = !checked;
    }

    public Checkbox(String text, float x, float y, float height) {
        this.text = new Label(text, x, y, height);
        checkSpace = height * CHECKBOX_SPACE;
        fillSpace = height * FILLBOX_SPACE;
    }

    private void drawFillSquare() {
        if (!checked) return;

        float s = text.height;
        float x = text.x - checkSpace - s + fillSpace;
        float y = text.y + fillSpace;
        s -= 2 * FILLBOX_SPACE * s;

        instance.fill(0);
        instance.rect(x, y, s, s);
    }

    @Override
    public void render() {
        text.drawSelf();
        ScreenManager i = instance;

        i.stroke(255);
        i.noFill();

        float s = text.height;
        float x = text.x - checkSpace - s;
        float y = text.y;
        if (x <= i.mouseX && i.mouseX <= x + s + checkSpace + text.width)
            if (y <= i.mouseY && i.mouseY <= y + s) {
                i.stroke(0);
                i.fill(HIGHLIGHT_COLOR);
            }

        i.rect(x, y, s, s);

        drawFillSquare();
    }

    public boolean within(float x, float y) {
        float s = text.height;
        float tx = text.x - checkSpace - s;
        float ty = text.y;
        return tx <= x && x <= tx + s + checkSpace + text.width && ty <= y && y <= ty + s;
    }
}
