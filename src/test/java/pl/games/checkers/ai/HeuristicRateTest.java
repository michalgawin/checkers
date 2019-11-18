package pl.games.checkers.ai;

import pl.games.checkers.model.PawnType;
import pl.games.checkers.ui.Checkerboard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HeuristicRateTest {

	private static final int INITIAL_SCORE = 0;

	@Test
	public void checkInitialStateScore() {
		Checkerboard checkerboard = new Checkerboard();
		Rate rate = new HeuristicRate(checkerboard.getBoard(), PawnType.BLACK);
		Assertions.assertEquals(rate.rate(), INITIAL_SCORE, "Black & White scores are not equal");
	}

	@Test
	public void checkInitialStatesAreEqual() {
		Checkerboard checkerboard = new Checkerboard();
		Rate rateWhite = new HeuristicRate(checkerboard.getBoard(), PawnType.WHITE);
		Rate rateBlack = new HeuristicRate(checkerboard.getBoard(), PawnType.BLACK);
		Assertions.assertEquals(Math.abs(rateWhite.rate()), rateBlack.rate(), "Black & White scores are not equal");
	}

}
