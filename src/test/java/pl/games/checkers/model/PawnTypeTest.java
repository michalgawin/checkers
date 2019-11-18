package pl.games.checkers.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PawnTypeTest {

	@Test
	public void testNegate() {
		Assertions.assertEquals(PawnType.BLACK, PawnType.WHITE.negate());
	}

}
