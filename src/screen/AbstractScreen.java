package screen;

import main.ScreenManager;
import screen.components.Clickable;
import screen.components.Label;
import util.Props;

import java.util.HashSet;

import static main.ScreenManager.instance;
import static processing.core.PConstants.ARROW;
import static processing.core.PConstants.HAND;

public abstract class AbstractScreen {
    public static final float COORDINATE_FIX = 3;
    static final int WIN_SCORE = (int) Props.getLong("win score");
    static final float BACK_X = instance.getWidth() * Props.getLong("back x") / 100f;
    static final float BACK_Y = instance.getHeight() * Props.getLong("back y") / 100f;
    static final float BACK_SIZE = instance.getHeight() * Props.getLong("back size") / 100f;
    static final float DELTA_Y = instance.getHeight() * Props.getLong("delta y") / 100f;
    private static final int DEFAULT_BACKGROUND = (int) Props.getLong("default background greyscale");

    HashSet<Clickable> clickables = new HashSet<>();
    HashSet<Label> labels = new HashSet<>();

    private void transformCursor() {
        ScreenManager i = instance;
        for (Clickable c : clickables) {
            if (c.within(i.mouseX, i.mouseY)) {
                i.cursor(HAND);
                return;
            }
        }
        i.cursor(ARROW);
    }

    public void click() {
        ScreenManager i = instance;
        for (Clickable c : clickables) {
            if (c.within(i.mouseX, i.mouseY)) {
                c.click();
                return;
            }
        }
    }

    public void drawBackground() {
        instance.background(DEFAULT_BACKGROUND);
    }

    void drawComponents() {
        for (Label l : labels) l.drawSelf();
        for (Clickable c : clickables) c.render();
        transformCursor();
    }

    public void drawAll() {
        drawBackground();
        drawComponents();
    }
}
