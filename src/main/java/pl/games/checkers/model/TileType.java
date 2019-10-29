package pl.games.checkers.model;

import javafx.scene.paint.Color;

public enum TileType {

    ALLOWED(Color.valueOf("#005066")),
    DISALLOWED(Color.valueOf("#78cce2"));

    final Color background;

    TileType(Color background) {
        this.background = background;
    }

    public Color getBackground() {
        return background;
    }

}
