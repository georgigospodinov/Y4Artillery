package screen.components;

import main.ScreenManager;

import java.util.LinkedHashSet;

import static main.ScreenManager.instance;
import static processing.core.PConstants.ARROW;
import static screen.AbstractScreen.COORDINATE_FIX;

public class Button extends Label implements Clickable {

    private LinkedHashSet<Runnable> actions = new LinkedHashSet<>();

    public Button(String text, float x, float y, float height) {
        super(text, x, y, height);
    }

    @Override
    public void render() {
        ScreenManager i = instance;
        if (within(i.mouseX, i.mouseY)) {
            i.stroke(0);
            i.fill(HIGHLIGHT_COLOR);
        }
        else {
            i.stroke(255);
            i.noFill();
        }

        if (x + width > instance.getWidth()) {
            x = instance.getWidth() - width - COORDINATE_FIX;
        }
        if (y + height > instance.getHeight()) {
            y = instance.getHeight() - height - COORDINATE_FIX;
        }
        i.rect(x, y, width, height);

        super.drawSelf();

    }

    public void onClick(Runnable action) {
        actions.add(action);
    }

    @Override
    public void click() {
        for (Runnable a : actions) a.run();
        instance.cursor(ARROW);
    }

    @Override
    public boolean within(float x, float y) {
        return this.x <= x && x <= this.x + this.width &&
                this.y <= y && y <= this.y + this.height;
    }
}
