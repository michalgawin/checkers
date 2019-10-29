package pl.games.checkers.model;

public class Move {

    private final MoveType type;
    private final Pawn killedPawn;

    public Move(MoveType type) {
        this(type, null);
    }

    public Move(MoveType type, Pawn killedPawn) {
        this.type = type;
        this.killedPawn = killedPawn;
    }

    public MoveType type() {
        return type;
    }

    public Pawn killedPawn() {
        return killedPawn;
    }

}
