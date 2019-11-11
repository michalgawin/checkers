package pl.games.checkers.ai.algorithm;

import pl.games.checkers.ai.GameTree;
import pl.games.checkers.ai.MoveValue;
import pl.games.checkers.ai.PawnMoveRecursive;
import pl.games.checkers.model.Pawn;

public class SimplyBest implements NextMove {

    /**
     * Get the best move on the board for specified color
     * @return the best move of pawnType on the board
     */
    @Override
    public Pawn nextMove(final GameTree gameTree) {
        MoveValue bestMove = MoveValue.create(null, Integer.MIN_VALUE);

        for (int y = 0; y < gameTree.getBoard().getHeight(); y++) {
            for (int x = 0; x < gameTree.getBoard().getWidth(); x++) {
                Pawn pawn = gameTree.getBoard().getPawn(y, x);
                if (pawn != null && pawn.getType() == gameTree.getPawnType()) {
                    MoveValue entry = PawnMoveRecursive.getNextMove(gameTree.getBoard(), pawn);
                    if (entry != null && entry.getScore() > bestMove.getScore()) {
                        bestMove = entry;
                    }
                }
            }
        }

        return bestMove.getPawn();
    }

}
