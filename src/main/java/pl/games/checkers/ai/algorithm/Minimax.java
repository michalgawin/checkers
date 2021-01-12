package pl.games.checkers.ai.algorithm;

import pl.games.checkers.ai.GameTree;
import pl.games.checkers.model.Pawn;

public class Minimax implements NextMove {

	@Override
	public Pawn nextMove(final GameTree gameTree) {
		return max(gameTree, Long.MIN_VALUE, Long.MAX_VALUE).getPawn();
	}

	private GameTree max(GameTree gameTree, long alpha, long beta) {
		GameTree maxGameTree = null;

		for (GameTree child : gameTree) {
			GameTree current;

			if (child.getPawnType() == gameTree.getPawnType()) { //beating
				current = max(child, alpha, beta);
			} else {
				current = min(child, alpha, beta);
			}

			if (current.rate() > alpha) {
				alpha = current.rate();
				maxGameTree = child;

				if (alpha >= beta) { //stop analyzing this subtree
					break;
				}
			}
		}

		if (null == maxGameTree) { // leaf
			maxGameTree = gameTree;
		}

		return maxGameTree;
	}

	private GameTree min(GameTree gameTree, long alpha, long beta) {
		GameTree minGameTree = null;

		for (GameTree child : gameTree) {
			GameTree current;

			if (child.getPawnType() == gameTree.getPawnType()) { //beating
				current = min(child, alpha, beta);
			} else {
				current = max(child, alpha, beta);
			}

			if (current.rate() < beta) { //stop analyzing this subtree
				beta = current.rate();
				minGameTree = child;

				if (beta <= alpha) {
					break;
				}
			}
		}

		if (null == minGameTree) { // leaf
			minGameTree = gameTree;
		}

		return minGameTree;
	}

}
