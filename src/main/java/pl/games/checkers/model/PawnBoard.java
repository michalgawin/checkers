package pl.games.checkers.model;

public class PawnBoard extends Board<Pawn> {

    private final Pawn[][] pawns;

    public PawnBoard(int height, int width) {
        super(height, width);
        this.pawns = new Pawn[height][width];
    }

    public PawnBoard(Board pawns) {
        super(pawns.getHeight(), pawns.getWidth());
        this.pawns = pawns.getPawnsCopy();
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
