package screen;

import ai.AI;
import main.Area;
import objects.PhysicalObject;
import objects.Player;
import objects.Shell;
import processing.core.PImage;
import screen.components.Button;
import util.Props;

import static main.ScreenManager.instance;
import static objects.PhysicalObject.printWind;
import static objects.Player.createPlayer;

public class Game extends AbstractScreen {

    private static final float TURN_SIZE = instance.getHeight() * Props.getLong("text turn size") / 100f;
    private static final float TEXT_TURN_X = instance.getWidth() * Props.getLong("text turn x") / 100f;
    private static final float TEXT_TURN_Y = instance.getHeight() * Props.getLong("text turn y") / 100f;
    public Player player1, player2, turn;
    private PImage background;

    private void loadBackground() {
        background = instance.loadImageProp("background image");
        background.resize(instance.getWidth(), instance.getHeight());
    }

    Game() {
        player1 = createPlayer("1");
        player2 = createPlayer("2");
        Area.generateTerrain(player1.getHeight());
        turn = player1;
        loadBackground();

        Button back = new Button("Exit to Menu", BACK_X, BACK_Y, BACK_SIZE);
        back.onClick(instance::backToMenu);
        clickables.add(back);
    }

    @Override
    public void drawBackground() {
        if (background != null)
            instance.image(background, 0, 0);
        else super.drawBackground();
    }

    private void drawTurn() {
        String who = "Player " + (turn == player1 ? "1" : "2") + "'s turn";
        instance.textSize(TURN_SIZE);
        instance.fill(turn.getColor());
        instance.text(who, TEXT_TURN_X, TEXT_TURN_Y);
    }

    private void forces() {
        Area.gravitation();
        PhysicalObject.changeWind();
    }

    private void moveAndDrawObjects() {
        turn.react();

        player1.move();
        player2.move();
        Shell.moveS();

        player1.drawSelf();
        player2.drawSelf();
        Shell.draw();

        drawTurn();
        printWind();
    }

    @Override
    public void drawAll() {
        drawBackground();
        forces();
        moveAndDrawObjects();
        drawComponents();
    }

    public void score(Player p) {
        p.scorePoint();

        if (p.getScore() == WIN_SCORE) {
            instance.victory = new Victory(p.getID(), p.getScore(), getOpponent(p).getScore());
            instance.setState(State.VICTORY);
            return;
        }

        // Move players to top.
        player1.resetPosition();
        player2.resetPosition();

        // Drop them on a new terrain.
        Area.resetTerrain();
    }

    public void changeTurn() {
        if (turn instanceof AI) {
            AI a = (AI) turn;
            a.setShot();
        }
        turn = turn == player1 ? player2 : player1;
    }

    /**
     * No actions while the shell is in the air, or the ground is falling, or the tank is falling.
     */
    public boolean shouldNotReact() {
        return Shell.isInAir() || Area.isFalling() || player1.isFalling() || player2.isFalling();
    }

    public Player getOpponent(Player current) {
        if (current == player1) return player2;
        else return player1;
    }

    public int getTurnColor() {
        return turn.getColor();
    }
}
