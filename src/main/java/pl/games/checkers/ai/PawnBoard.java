package pl.games.checkers.ai;

import pl.games.checkers.Pawn;

public class PawnBoard extends Board<Pawn> {

    private final Pawn[][] pawns;

    public PawnBoard(int height, int width) {
        super(height, width);
        this.pawns = new Pawn[height][width];
    }

    public PawnBoard(PawnBoard pawns) {
        super(pawns.getHeight(), pawns.getWidth());
        this.pawns = pawns.copy();
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

    @Override
    public Pawn[][] copy() {
        Pawn[][] pawnsCopy = new Pawn[getHeight()][getWidth()];

        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (pawns[row][col] != null) {
                    pawnsCopy[row][col] = pawns[row][col].copy();
                } else {
                    pawnsCopy[row][col] = null;
                }
            }
        }

        return pawnsCopy;
    }

}