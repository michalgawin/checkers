package pl.games.checkers.ai;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnType;
import pl.games.checkers.model.Position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameTree extends RecursiveAction implements Iterable<GameTree> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameTree.class);
	private static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

	private final int deepness;
	private final Board board;
	private final PawnType pawnType; //specifies turn
	private final Pawn pawn; //specifies pawn move
	private final List<GameTree> nodes = new LinkedList<>(); //possible states after moves
	private final HeuristicState heuristicState;

	public GameTree(final Board board, final PawnType pawnType, int deepness) {
		this(board, pawnType, null, deepness);
	}

	public GameTree(final Board board, final PawnType pawnType, final Pawn pawn, int deepness) {
		this.board = board.copy();
		this.pawnType = pawnType;
		this.pawn = pawn == null ? null : pawn;
		this.heuristicState = new HeuristicState(board);
		this.deepness = deepness;
	}

	public GameTree buildTree() {
		forkJoinPool.invoke(this);
		return this;
	}

	@Override protected void compute() {
		ForkJoinTask.invokeAll(build()).stream()
				.map(ForkJoinTask::join)
				.collect(Collectors.toList());
	}

	@Override public Iterator<GameTree> iterator() {
		return nodes.iterator();
	}

	@Override public void forEach(Consumer<? super GameTree> action) {
		nodes.forEach(action);
	}

	public Integer score() {
		return heuristicState.apply(this.pawnType);
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

	private List<GameTree> build() {
		if (deepness <= 0) {
			return List.of();
		}

		List<Pawn> pawnList = new ArrayList<>();
		for (int y = 0; y < board.getHeight(); y++) {
			for (int x = 0; x < board.getWidth(); x++) {
				Pawn pawn = board.getPawn(y, x);
				if (pawn != null && pawn.getType() == pawnType) {
					pawnList.add(pawn);
				}
			}
		}

		boolean beating = false;
		for (Pawn pawn : pawnList) {
			if ((beating && pawn.hasBeating()) || !beating) {
				beating = updateNodes(beating, this.board.copy(), pawn);
			}
		}

		return nodes;
	}

	/**
	 *
	 * @param beating if true than only moves with beating are taking into account
	 * @param board state of board
	 * @param pawn the pawn of which moves will be analyzed
	 */
	private boolean updateNodes(boolean beating, Board board, Pawn pawn) {
		Position currentPosition = pawn.currentPosition();

		List<Pawn> pawnNextMoves = PawnMoveRecursive.getNextMoves(board, pawn).stream()
				.filter(Objects::nonNull)
				.map(MoveValue::getPawn)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		boolean hasBeating = pawnNextMoves.stream().filter(Pawn::hasBeating).findAny().isPresent();

		if (hasBeating && !beating) { //first beating everything before is unimportant
			beating = true;
			Iterator<GameTree> gameTree = this.iterator();
			while (gameTree.hasNext()) {
				gameTree.next();
				gameTree.remove();
			}
		}

		for (Pawn pawnNext : pawnNextMoves) {
			Pawn pawnNextCopy = pawnNext.copy();
			if (!beating || (beating && pawnNext.hasBeating())) {
				addNode(board.move(pawnNextCopy, currentPosition, pawnNextCopy.nextPosition()), pawnNext.copy());
			}
		}
		return beating;
	}

	/**
	 * @param board Board to add as a child
	 * @return added child
	 */
	private GameTree addNode(Board board, Pawn pawn) {
		GameTree gameTree = new GameTree(board, this.pawnType.negate(), pawn, deepness - 1);
		this.nodes.add(gameTree);
		return gameTree;
	}

}
