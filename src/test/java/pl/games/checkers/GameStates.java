package pl.games.checkers;

import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnImpl;
import pl.games.checkers.model.PawnType;
import pl.games.checkers.model.Position;
import pl.games.checkers.ui.Checkerboard;

import java.util.ArrayList;
import java.util.List;

public enum GameStates {

	INITIAL(new PawnType[] {
			null, PawnType.BLACK, null, PawnType.BLACK, null, PawnType.BLACK, null, PawnType.BLACK,
			PawnType.BLACK, null, PawnType.BLACK, null, PawnType.BLACK, null, PawnType.BLACK, null,
			null, PawnType.BLACK, null, PawnType.BLACK, null, PawnType.BLACK, null, PawnType.BLACK,
			null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null,
			PawnType.WHITE, null, PawnType.WHITE, null, PawnType.WHITE, null, PawnType.WHITE, null,
			null, PawnType.WHITE, null, PawnType.WHITE, null, PawnType.WHITE, null, PawnType.WHITE,
			PawnType.WHITE, null, PawnType.WHITE, null, PawnType.WHITE, null, PawnType.WHITE, null,
	}),

	BEAT(new PawnType[] {
			null, null, null, null, null, null, null, null,
			null, null, null, null, PawnType.WHITE, null, null, null,
			null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null,
			null, null, PawnType.WHITE, null, null, null, null, null,
			null, null, null, null, null, null, null, null,
			PawnType.BLACK, null, null, null, null, null, null, null,
	});

	private PawnType[] gameState;

	GameStates(PawnType[] gameState) {
		this.gameState = gameState;
	}

	public List<Pawn> getInitState() {
		List<Pawn> pawns = new ArrayList<>();

		for (int i = 0; i < gameState.length; i++) {
			int row = Math.floorDiv(i, Checkerboard.HEIGHT);
			int col = i % Checkerboard.WIDTH;

			Pawn pawn = createPawn(row, col, gameState[i]);
			if (pawn != null) {
				pawns.add(pawn);
			}
		}

		return pawns;
	}

	private Pawn createPawn(int row, int column, PawnType pawnType) {
		Pawn pawn = null;

		if (pawnType != null) {
			pawn = new PawnImpl(pawnType, new Position(row, column), false, null);
		}

		return pawn;
	}

}
