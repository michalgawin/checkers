package pl.games.checkers.ai;

import pl.games.checkers.model.Pawn;

import java.util.Comparator;

public class MoveValue {

	private final Pawn pawn;
	private final Integer score;

	public static MoveValue create(Pawn pawn, int score) {
		return new MoveValue(pawn, score);
	}

	private MoveValue(Pawn pawn, int score) {
		assert score <= 0 || (score > 0 && pawn != null);
		this.pawn = pawn;
		this.score = score;
	}

	public Pawn getPawn() {
		return pawn;
	}

	public Integer getScore() {
		return score;
	}

	public static Comparator<MoveValue> compareByScore() {
		return Comparator.comparing(MoveValue::getScore);
	}

}
