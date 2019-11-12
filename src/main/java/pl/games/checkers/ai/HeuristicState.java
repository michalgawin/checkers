package pl.games.checkers.ai;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnType;

import java.util.Optional;

public class HeuristicState implements State {

	private final int BEATING_SCORE = 10;
	private final int KING_SCORE = 6;
	private final int SCORE_FOR_PAWN = 5;
	private final int POSITION_SCORE = 1;

	private final Board board;
	private Optional<Integer> score = Optional.empty();

	public HeuristicState(final Board board) {
		this.board = board.copy();
	}

	@Override public Integer apply(final PawnType pawnType) {
		return score.orElseGet(() -> summarizeAll(pawnType));
	}

	private int summarizeAll(final PawnType pawnType) {
		int sum = 0;
		for (int y = 0; y < board.getHeight(); y++) {
			for (int x = 0; x < board.getWidth(); x++) {
				Pawn pawn = board.getPawn(y, x);
				int s = SCORE_FOR_PAWN;
				s += scoreForPosition(pawn);
				s += scoreForKing(pawn);
				s += hasBeating(board, pawn);
				s *= pawn == null ? 0 : pawnType == pawn.getType() ? 1 : -1;
				sum += s;
			}
		}
		this.score = Optional.of(sum);
		return sum;
	}

	private int scoreForPosition(final Pawn pawn) {
		int scoreForRow = 0;
		if (pawn == null) {
			return scoreForRow;
		}
		int scoreForColumn = (pawn.currentPosition().column() > 1 && pawn.currentPosition().column() < 6) ? 2 : 1;
		if (pawn.getType().getDirection() > 0) {
			scoreForRow = (pawn.currentPosition().row() + 1) * POSITION_SCORE;
		} else {
			scoreForRow = (8 - pawn.currentPosition().row()) * POSITION_SCORE;
		}
		return scoreForColumn + scoreForRow;
	}

	private int scoreForKing(Pawn pawn) {
		if (pawn == null) {
			return 0;
		}
		return pawn.isKing() ? KING_SCORE : 0;
	}

	private int hasBeating(final Board board, final Pawn pawn) {
		if (pawn == null) {
			return 0;
		}
		MoveValue bestMove = PawnMoveRecursive.getNextMove(board, pawn);
		if (bestMove == null) {
			return 0;
		}
		return bestMove.getPawn().hasBeating() ? BEATING_SCORE : 0;
	}
}
