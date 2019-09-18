package screen;

import objects.Player;
import screen.components.Button;
import screen.components.Checkbox;
import screen.components.Label;
import util.Props;

import static main.ScreenManager.instance;

public class Menu extends AbstractScreen {
    private static final float PLAY_X = instance.getWidth() * Props.getLong("button play x") / 100f;
    private static final float PLAY_Y = instance.getHeight() * Props.getLong("button play y") / 100f;
    private static final float PLAY_SIZE = instance.getHeight() * Props.getLong("button play size") / 100f;

    private static final float CONTROLS_X = instance.getWidth() * Props.getLong("button controls x") / 100f;
    private static final float CONTROLS_Y = instance.getHeight() * Props.getLong("button controls y") / 100f;
    private static final float P1_X = instance.getWidth() * Props.getLong("player 1 x") / 100f;
    private static final float P2_X = instance.getWidth() * Props.getLong("player 2 x") / 100f;
    private static final float PLAYER_Y = instance.getHeight() * Props.getLong("player y") / 100f;
    private static final float PLAYER_SIZE = instance.getHeight() * Props.getLong("player size") / 100f;

    private Checkbox p1AI, p1Barrel, p1Image;
    private Checkbox p2AI, p2Barrel, p2Image;

    public Menu() {

        Button play = new Button("Play", PLAY_X, PLAY_Y, PLAY_SIZE);

        Button controls = new Button("Controls", CONTROLS_X, CONTROLS_Y, PLAY_SIZE / 2f);
        controls.onClick(() -> instance.setState(State.CONTROLS));

        float y = PLAYER_Y;
        float x = P1_X;
        Label player1 = new Label("Player 1 ", x, y, PLAYER_SIZE);
        y += 2 * DELTA_Y;
        p1AI = new Checkbox("AI", x, y, PLAYER_SIZE);
        y += DELTA_Y;
        p1Barrel = new Checkbox("Barrel", x, y, PLAYER_SIZE);
        y += DELTA_Y;
        p1Image = new Checkbox("Image", x, y, PLAYER_SIZE);

        y = PLAYER_Y;
        x = P2_X;
        Label player2 = new Label("Player 2", x, y, PLAYER_SIZE);
        y += 2 * DELTA_Y;
        p2AI = new Checkbox("AI", x, y, PLAYER_SIZE);
        y += DELTA_Y;
        p2Barrel = new Checkbox("Barrel", x, y, PLAYER_SIZE);
        y += DELTA_Y;
        p2Image = new Checkbox("Image", x, y, PLAYER_SIZE);


        play.onClick(this::transitionToGameScreen);

        clickables.add(play);
        clickables.add(controls);
        clickables.add(p1AI);
        clickables.add(p1Barrel);
        clickables.add(p1Image);
        clickables.add(p2AI);
        clickables.add(p2Barrel);
        clickables.add(p2Image);

        labels.add(player1);
        labels.add(player2);
    }

    private void transitionToGameScreen() {
        Player.setP1isAI(p1AI.isChecked());
        Player.setP2isAI(p2AI.isChecked());
        Player.setP1usesBarrel(p1Barrel.isChecked());
        Player.setP2usesBarrel(p2Barrel.isChecked());
        Player.setP1usesImage(p1Image.isChecked());
        Player.setP2usesImage(p2Image.isChecked());
        instance.game = new Game();
        instance.setState(State.GAME);
    }

}
