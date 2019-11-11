package pl.games.checkers.ai;

import pl.games.checkers.model.PawnType;

import java.util.function.Function;

public interface State extends Function<PawnType, Integer> {
}
