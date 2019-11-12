package pl.games.checkers.ai;

import pl.games.checkers.model.PawnType;
import pl.games.checkers.ui.Checkerboard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeuristicRateTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameTreeTest.class);

	private static final int INITIAL_SCORE = 0;

	@Test
	public void checkInitialStateScore() {
		Checkerboard checkerboard = new Checkerboard();
		Rate rate = new HeuristicRate(checkerboard.getBoard(), PawnType.BLACK);
		Assertions.assertEquals(rate.get(), INITIAL_SCORE, "Black & White scores are not equal");
	}

	@Test
	public void checkInitialStatesAreEqual() {
		Checkerboard checkerboard = new Checkerboard();
		Rate rateWhite = new HeuristicRate(checkerboard.getBoard(), PawnType.WHITE);
		Rate rateBlack = new HeuristicRate(checkerboard.getBoard(), PawnType.BLACK);
		Assertions.assertEquals(Math.abs(rateWhite.get()), rateBlack.get(), "Black & White scores are not equal");
	}

}