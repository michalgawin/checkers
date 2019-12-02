package pl.games.checkers.ai;

import pl.games.checkers.GameStates;
import pl.games.checkers.ai.algorithm.Minimax;
import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnBoard;
import pl.games.checkers.model.PawnType;
import pl.games.checkers.ui.Checkerboard;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GameTreeTest {

	private static final int DEPTH = 3;

	@Test
	public void testDepth() {
		Board<Pawn> board = new PawnBoard(Checkerboard.HEIGHT, Checkerboard.WIDTH, GameStates.INITIAL.getInitState());

		GameTree gameTree = new GameTree(board, PawnType.BLACK, DEPTH).buildTree();

		int i = 0;
		for (Iterator<GameTree> iterator = gameTree.iterator(); iterator.hasNext(); iterator = iterator.next().iterator()) {
			i++;
		}
		Assertions.assertEquals(DEPTH, i);
	}

	@Test
	public void testFirstMoveToRow3() {
		Board<Pawn> board = new PawnBoard(Checkerboard.HEIGHT, Checkerboard.WIDTH, GameStates.INITIAL.getInitState());

		GameTree gameTree = new GameTree(board, PawnType.BLACK, DEPTH).buildTree();

		Pawn pawn = new Minimax().nextMove(gameTree);
		Assertions.assertNotNull(pawn);
		Assertions.assertEquals(3, pawn.nextPosition().row());
	}

	@Test
	public void testKingBeating() {
		List<Pawn> pawnList = GameStates.BEAT.getInitState();
		pawnList.stream().forEach(p -> p.setKing());

		Board<Pawn> board = new PawnBoard(Checkerboard.HEIGHT, Checkerboard.WIDTH, pawnList);

		GameTree gameTree = new GameTree(board, PawnType.BLACK, DEPTH).buildTree();

		Pawn pawn = new Minimax().nextMove(gameTree);
		board.move(pawn, pawn.nextPosition(), true);

		long countWhite = board.pawnsAsList().stream().filter(p -> p.getType() == PawnType.WHITE).count();
		Assertions.assertEquals(0, countWhite);
	}

}
