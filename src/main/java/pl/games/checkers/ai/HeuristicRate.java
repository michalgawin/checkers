package pl.games.checkers.ai;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnBoard;
import pl.games.checkers.model.PawnType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HeuristicRate implements Rate {

	private final int BEATING_SCORE = 10;
	private final int BEATING_KING_SCORE = 15;
	private final int KING_SCORE = 15;
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
		return rate.orElseGet(() -> summarizeAll());
	}

	public PawnType getPawnType() {
		return pawnType;
	}

	private long summarizeAll() {
		List<Pawn> pawns = (List<Pawn>) board.pawnsAsList();

		List<Pawn> blackPawns = pawns.stream().filter(p -> p.getType() == PawnType.BLACK).collect(Collectors.toList());
		long sum1 = rate(blackPawns);

		List<Pawn> whitePawns = pawns.stream().filter(p -> p.getType() == PawnType.WHITE).collect(Collectors.toList());
		long sum2 = rate(whitePawns);

		sum1 += (blackPawns.stream().count() == 0) ? 1000 : 0;
		sum2 += (whitePawns.stream().count() == 0) ? 1000 : 0;

		long sum = sum1 - sum2;
		this.rate = Optional.of(sum);

		return sum;
	}

	private long rate(List<Pawn> pawns) {
		long scoreCount = pawns.stream().count() * SCORE_FOR_PAWN;
		long scorePosition = pawns.stream()
				.filter(Predicate.not(Pawn::isKing))
				.mapToInt(this::scoreForPosition).sum();
		long scoreForKing = pawns.stream().mapToInt(this::scoreForKing).sum();
		long scoreBeating = pawns.stream().mapToInt(p -> hasBeating(board, p)).sum();

		return scoreCount + scorePosition + scoreForKing + scoreBeating;
	}

	private int scoreForPosition(final Pawn pawn) {
		if (pawn == null) {
			return 0;
		}
		int scoreForColumn = (pawn.currentPosition().column() > 1 && pawn.currentPosition().column() < 6) ? 2 : 1;
		int scoreForRow = (pawn.currentPosition().row() > 1 && pawn.currentPosition().row() < 6) ? 2 : 1;
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
		if (bestMove.getPawn().hasBeating()) {
			return bestMove.getPawn().killedPawn().isKing() ? BEATING_KING_SCORE : BEATING_SCORE;
		}
		return 0;
	}

	@Override
	public String toString() {
		return rate.toString();
	}
}
