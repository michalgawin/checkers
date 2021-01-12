package pl.games.checkers.ai;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnBoard;
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

public class GameTree extends RecursiveAction implements Iterable<GameTree>, Rate {

	private static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
	private static final int DEFAULT_DEPTH = 1;

	private final int depth;
	private final Board board;
	private final Pawn pawn; //specifies pawn move
	private final List<GameTree> nodes; //possible states of move
	private final HeuristicRate heuristicRate;

	private Optional<Pawn> constraint = Optional.empty(); //build game tree with first move limited to this pawn

	public GameTree(final Board board, final PawnType pawnType) {
		this(board, pawnType, DEFAULT_DEPTH);
	}

	public GameTree(final Board board, final PawnType pawnType, int depth) {
		this(board, pawnType, null, depth);
	}

	public GameTree(final Board board, final PawnType pawnType, final Pawn pawn, int depth) {
		this(board, pawn, depth, new LinkedList<>(), new HeuristicRate(board, pawnType));
	}

	public GameTree(final Board board, final Pawn pawn, int depth, List<GameTree> nodes, HeuristicRate heuristicRate) {
		this.board = PawnBoard.create(board);
		this.pawn = pawn;
		this.depth = depth;
		this.nodes = nodes;
		this.heuristicRate = heuristicRate;
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

	@Override public Long rate() {
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

		final List<Pawn> pawnList = new ArrayList<>();
		if (constraint.isPresent()) {
			pawnList.add(constraint.get().copy());
		} else {
			board.pawnsAsList().stream()
					.filter(Objects::nonNull)
					.filter(p -> ((Pawn) p).getType() == getPawnType())
					.forEach(p -> pawnList.add((Pawn) p));
		}

		boolean beating = false;
		for (Pawn pawn : pawnList) {
			if (updateNodes(beating, PawnBoard.create(board), pawn)) {
				beating = true;
			}
		}

		return nodes;
	}

	/**
	 *
	 * @param beating if true then only moves with beating are taking into account
	 * @param board state of board
	 * @param pawn the pawn of which moves will be analyzed
	 */
	private boolean updateNodes(boolean beating, Board board, Pawn pawn) {
		List<Pawn> pawnNextMoves = PawnMoveRecursive.getNextMoves(PawnBoard.create(board), pawn).stream()
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
				addNode(PawnBoard.create(board).move(pawnNext, pawnNext.nextPosition(), true), copy);
			}
		}
		return beating && hasBeating;
	}

	/**
	 * @param board Board to add as a child
	 * @return added child
	 */
	private GameTree addNode(Board board, Pawn pawn) {
		GameTree gameTree;
		if (pawn.hasBeating()) {
			gameTree = new GameTree(board, getPawnType(), pawn, depth - 1);
		} else {
			gameTree = new GameTree(board, getPawnType().negate(), pawn, depth - 1);
		}
		gameTree.rate();
		this.nodes.add(gameTree);
		return gameTree;
	}

}
