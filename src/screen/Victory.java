package screen;

import screen.components.Button;
import screen.components.Label;
import util.Props;

import static main.ScreenManager.instance;

public class Victory extends AbstractScreen {

    private static final float VICTORY_X = instance.getWidth() * Props.getLong("victory x") / 100f;
    private static final float VICTORY_Y = instance.getHeight() * Props.getLong("victory y") / 100f;
    private static final float VICTORY_SIZE = instance.getHeight() * Props.getLong("victory size") / 100f;
    private static final float OK_X = instance.getWidth() * Props.getLong("ok x") / 100f;
    private static final float OK_Y = instance.getHeight() * Props.getLong("ok y") / 100f;
    private static final float OK_SIZE = instance.getHeight() * Props.getLong("ok size") / 100f;

    Victory(String id, int winScore, int loseScore) {
        String win = "Congratulations!\nPlayer " + id + " wins with " + winScore + " to " + loseScore + ".";
        Label winner = new Label(win, VICTORY_X, VICTORY_Y, VICTORY_SIZE);
        labels.add(winner);
        Button ok = new Button("OK", OK_X, OK_Y, OK_SIZE);
        ok.onClick(instance::backToMenu);
        clickables.add(ok);
    }
}
