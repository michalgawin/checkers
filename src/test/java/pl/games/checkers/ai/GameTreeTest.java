package pl.games.checkers.ai;

import pl.games.checkers.model.Pawn;
import pl.games.checkers.ui.Checkerboard;

import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameTreeTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameTreeTest.class);

	private static final int DEEPNESS = 2;

	@Test
	public void testCompute() {
		Checkerboard checkerboard = new Checkerboard();
		GameTree gameTree = new GameTree(checkerboard.getBoard());
		gameTree.buildTree(DEEPNESS);

		int i = 0;
		for (Iterator<GameTree> iterator = gameTree.iterator(); iterator.hasNext(); iterator = iterator.next().iterator()) {
			i++;
		}
		Assertions.assertEquals(DEEPNESS, i);
	}

	@Test
	public void testCalculate() {
		Checkerboard checkerboard = new Checkerboard();
		GameTree gameTree = new GameTree(checkerboard.getBoard());
		Pawn pawn = gameTree.getBestMoveOfPawn(DEEPNESS);
		Assertions.assertNotNull(pawn);
		Assertions.assertEquals(3, pawn.nextPosition().row());
	}

}
