package pl.games.checkers.ai;

import pl.games.checkers.model.Pawn;

import java.util.Comparator;

public class MoveRate implements Rate {

	private final Pawn pawn;
	private final Long rate;

	public static MoveRate create(Pawn pawn, int rate) {
		return new MoveRate(pawn, rate);
	}

	private MoveRate(Pawn pawn, long rate) {
		assert rate <= 0 || (rate > 0 && pawn != null);
		this.pawn = pawn;
		this.rate = rate;
	}

	public Pawn getPawn() {
		return pawn;
	}

	@Override public Long rate() {
		return rate;
	}

	public static Comparator<MoveRate> compareByScore() {
		return Comparator.comparing(Rate::rate);
	}

}
