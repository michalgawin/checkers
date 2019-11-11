package pl.games.checkers.ai.algorithm;

import pl.games.checkers.ai.GameTree;
import pl.games.checkers.model.Pawn;

public interface NextMove {

	Pawn nextMove(GameTree gameTree);

}
