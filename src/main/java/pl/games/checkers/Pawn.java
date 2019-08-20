package pl.games.checkers;

public interface Pawn extends Copier<Pawn> {

    PawnType getType();

    void setType(PawnType pawnType);

    Position currentPosition();

    Position nextPosition();

    Pawn nextPosition(Position position);

    boolean isKing();

    void setKing();

    void move(Position position);

    Pawn setMove(Move move);

    boolean hasBeating();

    void abortMove();

    @Override
    Pawn copy();

}
