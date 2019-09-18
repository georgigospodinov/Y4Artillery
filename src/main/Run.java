package main;

import processing.core.PApplet;
import util.Logger;
import util.Props;

import java.io.FileNotFoundException;

import static util.PrintFormatting.print;

public class Run {

    public static final Logger L = new Logger("log.txt");

    public static void main(String[] args) {

        try {
            Props.load("assets/config/game.props");
            Props.load("assets/config/screen.props");
            Props.load("assets/config/controls.props");
        }
        catch (FileNotFoundException e) {
            L.log(e);
            L.close();
            print("Could not load game configuration file.", "See log.txt for more info.");
            return;
        }

        PApplet.main(new String[]{"main.ScreenManager"});
    }

}
