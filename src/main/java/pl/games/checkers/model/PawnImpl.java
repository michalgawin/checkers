package pl.games.checkers.model;

import pl.games.checkers.ui.PawnFigure;

public class PawnImpl implements Pawn {

    private PawnType type;
    private boolean king = false;

    private final PawnFigure reference;

    private Position currentPosition = new Position(-1, -1);
    private Position nextPosition = new Position(-1, -1);
    private Move move;

    public PawnImpl(PawnType type, Position position, boolean isKing, PawnFigure reference) {
        this(type, position, null, isKing, reference);
    }

    public PawnImpl(PawnType type, Position position, Move move, boolean isKing, PawnFigure reference) {
        this(type, position, new Position(-1, -1), move, isKing, reference);
    }

    public PawnImpl(PawnType type, Position current, Position next, Move move, boolean isKing, PawnFigure reference) {
        move(current);
        this.nextPosition = next;

        this.type = type;
        this.reference = reference;
        this.move = move;

        if (isKing) {
            setKing();
        }
    }

    private PawnImpl(PawnImpl pawn) {
        this(pawn.type, pawn.currentPosition(), pawn.nextPosition(), pawn.move, pawn.isKing(), pawn.reference);
    }

    @Override
    public PawnType getType() {
        return type;
    }

    @Override
    public void setType(PawnType pawnType) {
        this.type = pawnType;
    }

    @Override
    public Position currentPosition() {
        return currentPosition.copy();
    }

    @Override
    public Position nextPosition() {
        return nextPosition.copy();
    }

    @Override
    public Pawn nextPosition(Position position) {
        nextPosition = position;
        return this;
    }

    @Override
    public boolean isKing() {
        return king;
    }

    @Override
    public void setKing() {
        king = true;
    }

    @Override
    public void move(Position position) {
        currentPosition = position.copy();
    }

    @Override
    public Pawn setMove(Move move) {
        this.move = move;
        return this;
    }

    @Override
    public boolean hasBeating() {
        return move != null && move.type() == MoveType.KILL;
    }

    @Override
    public Pawn killedPawn() {
        return hasBeating() ? move.killedPawn() : null;
    }

    @Override
    public void abortMove() {
    }

    @Override
    public PawnImpl copy() {
        return new PawnImpl(this);
    }

    public void apply() {
        reference.move(currentPosition());
    }

    @Override
    public String toString() {
        return String.format("(%d, %d) -> (%d, %d)",
                this.currentPosition.column(), this.currentPosition.row(),
                this.nextPosition.column(), this.nextPosition.row());
    }

}
