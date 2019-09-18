package main;

import ai.AI;
import objects.Shell;
import processing.core.PApplet;
import processing.core.PImage;
import screen.*;
import util.PrintFormatting;
import util.Props;

import static main.Run.L;

public class ScreenManager extends PApplet {

    public static ScreenManager instance;

    private int width, height;
    private State state;
    private Menu menu;
    public Game game;
    private Controls controls;
    public Victory victory;
    private boolean screensCreated = false;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void backToMenu() {
        setState(State.MENU);
        Area.clearTerrain();
        Shell.remove();
    }

    public PImage loadImageProp(String imageProperty) {
        String imageFileName;
        try {
            imageFileName = Props.getString(imageProperty);
        }
        catch (NullPointerException npe) {
            L.log(npe);
            PrintFormatting.print("No image provided for " + imageProperty + ". Using default shape instead.");
            return null;
        }

        try {
            return instance.loadImage(imageFileName);
        }
        catch (NullPointerException npe) {
            L.log(npe);
            PrintFormatting.print("Failed to load \"" + imageFileName + "\". Using default shape instead.");
            return null;
        }
    }

    private void createScreens() {
        if (screensCreated) return;

        menu = new Menu();
        controls = new Controls();
        screensCreated = true;
    }

    @Override
    public void settings() {
        instance = this;
        try {
            String w = Props.getString("game width");
            switch (w) {
                case "displayWidth":
                    width = displayWidth;
                    break;
                case "displayHeight":
                    width = displayHeight;
                    break;
                default:
                    throw new Exception();
            }
        }
        catch (Exception e) {
            width = (int) Props.getLong("game width");
        }
        try {
            String h = Props.getString("game height");
            switch (h) {
                case "displayHeight":
                    height = displayHeight;
                    break;
                case "displayWidth":
                    height = displayWidth;
                    break;
                default:
                    throw new Exception();
            }
        }
        catch (Exception e) {
            height = (int) Props.getLong("game height");
        }
        size(width, height);
        state = State.MENU;
    }

    @Override
    public void draw() {
        createScreens();
        surface.setTitle("Artillery");
        switch (state) {
            case CONTROLS:
                controls.drawAll();
                break;
            case GAME:
                game.drawAll();
                break;
            case MENU:
                menu.drawAll();
                break;
            case VICTORY:
                victory.drawAll();
                break;
        }
    }

    @Override
    public void keyPressed() {
        if (game == null) return;
        if (game.turn instanceof AI) return;
        game.turn.startReacting(keyCode);
    }

    @Override
    public void keyReleased() {
        if (game == null) return;
        if (game.turn instanceof AI) return;
        game.turn.stopReacting(keyCode);
    }

    @Override
    public void mousePressed() {
        if (game == null) return;
        if (game.turn instanceof AI) return;
        game.turn.startReacting(mouseButton);
    }

    @Override
    public void mouseReleased() {
        if (game == null) return;
        if (game.turn instanceof AI) return;
        game.turn.stopReacting(mouseButton);
    }

    @Override
    public void mouseClicked() {
        switch (state) {
            case MENU:
                menu.click();
                break;
            case GAME:
                game.click();
                break;
            case VICTORY:
                victory.click();
                break;
            case CONTROLS:
                controls.click();
                break;
        }
    }

}
