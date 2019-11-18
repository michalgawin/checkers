package pl.games.checkers.ai;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameTree extends RecursiveAction implements Iterable<GameTree>, Rate {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameTree.class);
	private static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
	private static final int DEFAULT_DEPTH = 1;

	private final int depth;
	private final Board board;
	private final Pawn pawn; //specifies pawn move
	private final List<GameTree> nodes = new LinkedList<>(); //possible states after moves
	private final HeuristicRate heuristicRate;

	private Optional<Pawn> constraint = Optional.empty(); //build game tree with first move limited to this pawn

	public GameTree(final Board board, final PawnType pawnType) {
		this(board, pawnType, DEFAULT_DEPTH);
	}

	public GameTree(final Board board, final PawnType pawnType, int depth) {
		this(board, pawnType, null, depth);
	}

	public GameTree(final Board board, final PawnType pawnType, final Pawn pawn, int depth) {
		this.board = board.copy();
		this.pawn = pawn == null ? null : pawn;
		this.heuristicRate = new HeuristicRate(board, pawnType);
		this.depth = depth;
	}

	public GameTree constraint(Pawn constraint) {
		this.constraint = Optional.of(constraint.copy());
		return this;
	}

	public GameTree buildTree() {
		forkJoinPool.submit(this).join();
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

	@Override public Integer rate() {
		return heuristicRate.rate();
	}

	public Pawn getPawn() {
		return pawn;
	}

	public Board getBoard() {
		return board;
	}

	public PawnType getPawnType() {
		return heuristicRate.getPawnType();
	}

	private List<GameTree> build() {
		if (depth <= 0) {
			return List.of();
		}

		List<Pawn> pawnList = new ArrayList<>();
		if (constraint.isPresent()) {
			pawnList.add(constraint.get().copy());
		} else {
			for (int y = 0; y < board.getHeight(); y++) {
				for (int x = 0; x < board.getWidth(); x++) {
					Pawn pawn = board.getPawn(y, x);
					if (pawn != null && pawn.getType() == getPawnType()) {
						pawnList.add(pawn);
					}
				}
			}
		}

		boolean beating = false;
		for (Pawn pawn : pawnList) {
			beating = updateNodes(beating, this.board.copy(), pawn);
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
		List<Pawn> pawnNextMoves = PawnMoveRecursive.getNextMoves(board, pawn).stream()
				.filter(Objects::nonNull)
				.map(MoveRate::getPawn)
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
			if (!beating || (beating && pawnNext.hasBeating())) {
				Pawn copy = pawnNext.copy();
				LOGGER.debug("{}>> move: {}", "=".repeat(depth), pawnNext);
				addNode(board.move(pawnNext, pawnNext.nextPosition(), true), copy);
			}
		}
		return beating;
	}

	/**
	 * @param board Board to add as a child
	 * @return added child
	 */
	private GameTree addNode(Board board, Pawn pawn) {
		GameTree gameTree = new GameTree(board, getPawnType().negate(), pawn, depth - 1);
		this.nodes.add(gameTree);
		return gameTree;
	}

}
