package pl.games.checkers.ai;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnBoard;
import pl.games.checkers.model.PawnType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HeuristicRate implements Rate {

	private final int BEATING_SCORE = 10;
	private final int KING_SCORE = 10;
	private final int SCORE_FOR_PAWN = 5;
	private final int POSITION_SCORE = 1;

	private final Board board;
	private final PawnType pawnType;
	private Optional<Long> rate = Optional.empty();

	public HeuristicRate(final Board board, final PawnType pawnType) {
		this.board = PawnBoard.create(board);
		this.pawnType = pawnType;
	}

	@Override public Long rate() {
		return rate.orElseGet(() -> summarizeAll(pawnType));
	}

	public PawnType getPawnType() {
		return pawnType;
	}

	private long summarizeAll(final PawnType pawnType) {
		List<Pawn> pawns = (List<Pawn>) board.pawnsAsList();

		List<Pawn> pawns1 = pawns.stream().filter(p -> p.getType() == PawnType.BLACK).collect(Collectors.toList());
		long count1 = pawns1.stream().count();
		long scorePosition1 = pawns1.stream().mapToInt(this::scoreForPosition).sum();
		long scoreForKing1 = pawns1.stream().mapToInt(this::scoreForKing).sum();
		long scoreBeating1 = pawns1.stream().mapToInt(p -> hasBeating(board, p)).sum();
		long sum1 = count1 + scorePosition1 + scoreForKing1 + scoreBeating1;

		List<Pawn> pawns2 = pawns.stream().filter(p -> p.getType() != PawnType.BLACK).collect(Collectors.toList());
		long count2 = pawns2.stream().count();
		long scorePosition2 = pawns2.stream().mapToInt(this::scoreForPosition).sum();
		long scoreForKing2 = pawns2.stream().mapToInt(this::scoreForKing).sum();
		long scoreBeating2 = pawns2.stream().mapToInt(p -> hasBeating(board, p)).sum();
		long sum2 = count2 + scorePosition2 + scoreForKing2 + scoreBeating2;

		sum1 += (count2 == 0) ? 1000 : 0;
		sum2 += (count1 == 0) ? 1000 : 0;

		long sum = sum1 - sum2;
		this.rate = Optional.of(sum);
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
		MoveRate bestMove = PawnMoveRecursive.getNextMove(board, pawn);
		if (bestMove == null) {
			return 0;
		}
		return bestMove.getPawn().hasBeating() ? BEATING_SCORE : 0;
	}

	@Override
	public String toString() {
		return rate.toString();
	}
}
