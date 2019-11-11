package pl.games.checkers.ai;

import pl.games.checkers.ai.algorithm.Minimax;
import pl.games.checkers.ai.algorithm.NextMove;
import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnType;
import pl.games.checkers.model.Position;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameTree implements Iterable<GameTree>, Supplier<Integer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameTree.class);

	private final Board board;
	private final PawnType pawnType; //specifies turn
	private final Pawn pawn; //specifies pawn move
	private final List<GameTree> children = new LinkedList<>(); //possible states after moves
	private final HeuristicState heuristicState;

	private final NextMove algorithm = new Minimax(); //new SimplyBest();

	public GameTree(final Board board) {
		this(board, PawnType.BLACK);
	}

	public GameTree(final Board board, final PawnType pawnType) {
		this(board, pawnType, null);
	}

	public GameTree(final Board board, final PawnType pawnType, final Pawn pawn) {
		this.board = board.copy();
		this.pawnType = pawnType;
		this.pawn = pawn == null ? null : pawn;
		this.heuristicState = new HeuristicState(board);
	}

	@Override public Integer get() {
		return heuristicState.apply(this.pawnType);
	}

	@Override public Iterator<GameTree> iterator() {
		return children.iterator();
	}

	@Override public void forEach(Consumer<? super GameTree> action) {
		children.forEach(action);
	}

	public Pawn getPawn() {
		return pawn;
	}

	public Board getBoard() {
		return board;
	}

	public PawnType getPawnType() {
		return pawnType;
	}

	/**
	 * @param board Board to add as a child
	 * @return added child
	 */
	private GameTree addChild(Board board, Pawn pawn) {
		GameTree gameTree = new GameTree(board, this.pawnType.negate(), pawn);
		this.children.add(gameTree);
		return gameTree;
	}

	public void buildTree(int deep) {
		buildTree(board, deep);
	}

	protected void buildTree(Board board, int deep) {
		boolean beating = false;

		for (int y = 0; y < board.getHeight(); y++) {
			for (int x = 0; x < board.getWidth(); x++) {
				Pawn pawn = board.getPawn(y, x);
				if (pawn != null && pawn.getType() == pawnType) {
					beating = addNodes(beating, this.board.copy(), pawn);
				}
			}
		}

		if (--deep > 0) {
			for (GameTree gameTree : this) {
				gameTree.buildTree(deep);
			}
		}
	}

	public Pawn getBestMoveOfPawn(int deep) {
		buildTree(deep);
		return algorithm.nextMove(this);
	}

	/**
	 *
	 * @param beating if true than only moves with beating are taking into account
	 * @param board state of board
	 * @param pawn the pawn of which moves will be analyzed
	 */
	private boolean addNodes(boolean beating, Board board, Pawn pawn) {
		Position currentPosition = pawn.currentPosition();
		for (MoveValue moveValue : PawnMoveRecursive.getNextMoves(board, pawn)) {
			Pawn bestPawn = moveValue.getPawn();
			if (bestPawn != null) {
				if (bestPawn.hasBeating()) {
					if (!beating) { //first beating everything before is unimportant
						beating = true;
						Iterator<GameTree> gameTree = this.iterator();
						while (gameTree.hasNext()) {
							gameTree.next();
							gameTree.remove();
						}
					}
				}
				Pawn bestPawnCopy = bestPawn.copy();
				if (!beating || (beating && bestPawn.hasBeating())) {
					addChild(board.move(bestPawnCopy, currentPosition, bestPawnCopy.nextPosition()), bestPawn.copy());
				}
			}
		}
		return beating;
	}

}
