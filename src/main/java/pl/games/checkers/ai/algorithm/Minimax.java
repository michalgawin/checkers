package pl.games.checkers.ai.algorithm;

import pl.games.checkers.ai.GameTree;
import pl.games.checkers.model.Pawn;

import java.util.AbstractMap;
import java.util.Map;

public class Minimax implements NextMove {

	@Override
	public Pawn nextMove(final GameTree gameTree) {
		return max(gameTree).getValue().getPawn();
	}

	private Map.Entry<Integer, GameTree> max(GameTree gameTree) {
		Map.Entry<Integer, GameTree> max = new AbstractMap.SimpleEntry<>(Integer.MIN_VALUE, null);

		for (GameTree child : gameTree) {
			Map.Entry<Integer, GameTree> current = min(child);
			if (current.getKey() > max.getKey()) {
				max = new AbstractMap.SimpleEntry<>(max.getKey(), child);
			}
		}

		if (null == max.getValue()) { // leaf
			return new AbstractMap.SimpleEntry<>(gameTree.get(), gameTree);
		}

		return max;
	}

	private Map.Entry<Integer, GameTree> min(GameTree gameTree) {
		Map.Entry<Integer, GameTree> min = new AbstractMap.SimpleEntry<>(Integer.MAX_VALUE, null);

		for (GameTree child : gameTree) {
			Map.Entry<Integer, GameTree> current = max(child);
			if (current.getKey() < min.getKey()) {
				min = new AbstractMap.SimpleEntry<>(min.getKey(), child);
			}
		}

		if (null == min.getValue()) { // leaf
			return new AbstractMap.SimpleEntry<>(gameTree.get(), gameTree);
		}

		return min;
	}

}
