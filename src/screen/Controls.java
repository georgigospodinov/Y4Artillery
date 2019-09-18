package screen;

import screen.components.Button;
import screen.components.Label;
import util.Props;

import static main.ScreenManager.instance;

public class Controls extends AbstractScreen {

    private static final float CONTROLS_SIZE = instance.getHeight() * Props.getLong("controls size") / 100f;
    private static final float CONTROLS_LEFT_X = instance.getWidth() * Props.getLong("controls left x") / 100f;
    private static final float CONTROLS_RIGHT_X = instance.getWidth() * Props.getLong("controls right x") / 100f;
    private static final float CONTROLS_Y = instance.getHeight() * Props.getLong("controls y") / 100f;

    public Controls() {
        Button back = new Button("Back to Menu", BACK_X, BACK_Y, BACK_SIZE);
        back.onClick(instance::backToMenu);
        clickables.add(back);

        float x = CONTROLS_LEFT_X;
        float y = CONTROLS_Y;
        Label incStr = new Label("Left mouse - Increase Strength", x, y, CONTROLS_SIZE);
        y += DELTA_Y;
        Label decStr = new Label("Right mouse - Decrease Strength", x, y, CONTROLS_SIZE);
        y += DELTA_Y;
        Label shoot = new Label("Space - shoot", x, y, CONTROLS_SIZE);

        x = CONTROLS_RIGHT_X;
        y = CONTROLS_Y;
        Label incEle = new Label("W - Increase Elevation", x, y, CONTROLS_SIZE);
        y += DELTA_Y;
        Label decEle = new Label("S - Decrease Elevation", x, y, CONTROLS_SIZE);
        y += DELTA_Y;
        Label movLef = new Label("A - Move Left", x, y, CONTROLS_SIZE);
        y += DELTA_Y;
        Label movRig = new Label("D - Move Right", x, y, CONTROLS_SIZE);

        labels.add(incStr);
        labels.add(decStr);
        labels.add(incEle);
        labels.add(decEle);
        labels.add(movLef);
        labels.add(movRig);
        labels.add(shoot);
    }
}
