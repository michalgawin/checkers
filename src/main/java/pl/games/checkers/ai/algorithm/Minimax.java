package pl.games.checkers.ai.algorithm;

import pl.games.checkers.ai.GameTree;
import pl.games.checkers.model.Pawn;

public class Minimax implements NextMove {

	@Override
	public Pawn nextMove(final GameTree gameTree) {
		return max(gameTree).getPawn();
	}

	private GameTree max(GameTree gameTree) {
		Long max = Long.MIN_VALUE;
		GameTree maxGameTree = null;

		for (GameTree child : gameTree) {
			GameTree current;

			if (child.getPawnType() == gameTree.getPawnType()) {
				current = max(child);
			} else {
				current = min(child);
			}

			if (current.rate() > max) {
				max = current.rate();
				maxGameTree = child;
			}
		}

		if (null == maxGameTree) { // leaf
			maxGameTree = gameTree;
		}

		return maxGameTree;
	}

	private GameTree min(GameTree gameTree) {
		Long min = Long.MAX_VALUE;
		GameTree minGameTree = null;

		for (GameTree child : gameTree) {
			GameTree current;

			if (child.getPawnType() == gameTree.getPawnType()) {
				current = min(child);
			} else {
				current = max(child);
			}

			if (current.rate() < min) {
				min = current.rate();
				minGameTree = child;
			}
		}

		if (null == minGameTree) { // leaf
			minGameTree = gameTree;
		}

		return minGameTree;
	}

}
