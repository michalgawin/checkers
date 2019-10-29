package pl.games.checkers.model;

import javafx.scene.paint.Color;

public enum PawnType {

    BLACK(1, Color.valueOf("#002439")),
    WHITE(-1, Color.valueOf("#e4eff0"));

    final int direction;
    final Color color;

    PawnType(int direction, Color color) {
        this.direction = direction;
        this.color = color;
    }

    public int getDirection() {
        return direction;
    }

    public Color getColor() {
        return color;
    }

}
