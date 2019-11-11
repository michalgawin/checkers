package pl.games.checkers.ai;

import pl.games.checkers.model.PawnType;
import pl.games.checkers.ui.Checkerboard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeuristicStateTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameTreeTest.class);

	private static final int INITIAL_SCORE = 0;

	@Test
	public void checkInitialStateScore() {
		Checkerboard checkerboard = new Checkerboard();
		State state = new HeuristicState(checkerboard.getBoard());
		Assertions.assertEquals(state.apply(PawnType.BLACK), INITIAL_SCORE, "Black & White scores are not equal");
	}

	@Test
	public void checkInitialStatesAreEqual() {
		Checkerboard checkerboard = new Checkerboard();
		State stateWhite = new HeuristicState(checkerboard.getBoard());
		State stateBlack = new HeuristicState(checkerboard.getBoard());
		Assertions.assertEquals(Math.abs(stateWhite.apply(PawnType.WHITE)), stateBlack.apply(PawnType.BLACK), "Black & White scores are not equal");
	}

}
