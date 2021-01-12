package pl.games.checkers.ui;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.games.checkers.model.Position;
import pl.games.checkers.model.Board;
import pl.games.checkers.model.PawnType;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class TileBoard extends Board<PawnFigure> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TileBoard.class);

    private final Tile[][] tiles;
    private final Group tileGroup;
    private final Group pawnGroup;
    private final BiFunction<TileBoard, PawnFigure, EventHandler<? super MouseEvent>> mouseRelease;

    public TileBoard(int height, int width, BiFunction<TileBoard, PawnFigure, EventHandler<? super MouseEvent>> mouseRelease) {
        super(height, width);
        this.tiles = new Tile[height][width];
        this.mouseRelease = mouseRelease;
        this.tileGroup = new Group();
        this.pawnGroup = new Group();

        for (int row = 0; row < getHeight(); row++) {
            for (int column = 0; column < getWidth(); column++) {
                Tile tile = new Tile(row, column);
                this.setTile(row, column, tile);
                if (tile.isAllowed()) {
                    tile.setPawn(createPawn(row, column));
                }

                tileGroup.getChildren().add(tile);
                if (tile.getPawn() != null) {
                    pawnGroup.getChildren().add(tile.getPawn());
                }
            }
        }

        LOGGER.info("***Game started***");
    }

    @Override
    public PawnFigure getPawn(int y, int x) {
        try {
            Tile tile = getTile(y, x);
            return tile == null ? null : tile.getPawn();
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public TileBoard setPawn(int y, int x, PawnFigure pawn) {
        tiles[y][x].setPawn(pawn);
        return this;
    }

    @Override
    public Board removePawn(Position currentPosition) {
        pawnGroup.getChildren().remove(getPawn(currentPosition));
        return super.setPawn(currentPosition, null);
    }

    public List<Group> getGroups() {
        return Arrays.asList(tileGroup, pawnGroup);
    }

    public Tile getTile(int y, int x) {
        try {
            return tiles[y][x];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public TileBoard setTile(int y, int x, Tile tile) {
        tiles[y][x] = tile;
        return this;
    }

    @Override
    public TileBoard movePawn(PawnFigure pawn, Position currentPosition, Position nextPosition) {
        super.movePawn(pawn, currentPosition, nextPosition);
        LOGGER.info(this.toString());
        return this;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\n');
        for (int i = 0; i < tiles.length; i++) {
            stringBuilder.append('|');
            for (int j = 0; j < tiles[i].length; j++) {
                if (!tiles[i][j].isAllowed()) {
                    stringBuilder.append(' ');
                } else if (tiles[i][j].getPawn() != null) {
                    if (tiles[i][j].getPawn().getType() == PawnType.WHITE) {
                        stringBuilder.append('W');
                    } else {
                        stringBuilder.append('B');
                    }
                } else {
                    stringBuilder.append('/');
                }
                stringBuilder.append('|');
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    private PawnFigure createPawn(int row, int column) {
        PawnFigure pawn = null;
        PawnType pawnType = null;

        if (row <= 2) {
            pawnType = PawnType.BLACK;
        } else if (row >= 5) {
            pawnType = PawnType.WHITE;
        }

        if (pawnType != null) {
            pawn = createPawn(pawnType, row, column);
        }

        return pawn;
    }

    private PawnFigure createPawn(PawnType type, int row, int column) {
        PawnFigure pawn = new PawnFigure(type, row, column);

        pawn.setOnMouseReleased(mouseRelease.apply(this, pawn));

        return pawn;
    }

}
