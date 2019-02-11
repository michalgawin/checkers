package pl.games.checkers;

public class Move {

    private final MoveType type;
    private Pawn pawn;

    public Move(MoveType type) {
        this.type = type;
    }

    public Pawn capturedPawn() {
        return pawn;
    }

    public Move capturedPawn(Pawn pawn) {
        this.pawn = pawn;
        return this;
    }

    public MoveType type() {
        return type;
    }

}
