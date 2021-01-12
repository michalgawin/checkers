package pl.games.checkers.model;

import java.util.List;

public class PawnBoard extends Board<Pawn> {

    private final Pawn[][] pawns;

    public static Board create(Board board) {
        return new PawnBoard(board);
    }

    private PawnBoard(Board pawns) {
        this(pawns.getHeight(), pawns.getWidth(), pawns.pawnsAsList());
    }

    public PawnBoard(int height, int width, List<Pawn> pawnList) {
        super(height, width);
        this.pawns = new Pawn[height][width];
        pawnList.stream()
                .map(p -> p.copy())
                .forEach(pawn -> setPawn(pawn.currentPosition(), pawn));
    }

    @Override
    public PawnBoard setPawn(int y, int x, Pawn pawn) {
        pawns[y][x] = pawn;
        return this;
    }

    @Override
    public Pawn getPawn(int y, int x) {
        try {
            return pawns[y][x];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

}
